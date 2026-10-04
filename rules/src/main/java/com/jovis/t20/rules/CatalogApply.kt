package com.jovis.t20.rules

/** Grupos de escolha de perícias, cada um com suas regras. */
enum class SkillGroupId { CLASS_FIXED, CLASS_FIXED_CHOICE, CLASS, ORIGIN, RACE, INTELLIGENCE, RACE_BONUS, OTHER }

data class SkillGroup(
    val id: SkillGroupId,
    val title: String,
    val description: String,
    /** Quantas perícias o grupo permite; null = sem limite. */
    val limit: Int?,
    val allowed: Set<Skill>,
    val selected: Set<Skill>,
    /** Perícias automáticas, que o jogador não muda. */
    val locked: Boolean = false,
    /** Falso para grupos que dão bônus em vez de treinamento (Deformidade). */
    val grantsTraining: Boolean = true,
) {
    val remaining: Int? get() = limit?.let { it - selected.size }
}

/**
 * Aplica as regras do catálogo à ficha, sem apagar o que o jogador preencheu à mão.
 * Depois de qualquer mudança de raça, classe, nível, atributos ou escolhas, chame [refresh].
 */
object CatalogApply {

    // ============================== Raça, classe e origem ==============================

    fun applyClass(sheet: ManualSheet, cls: CatalogClass): ManualSheet {
        // Ao trocar de classe, as habilidades e o caminho da classe anterior deixam de valer
        val previous = ClassAbilities.of(sheet.classId).map { it.name }.toSet()
        // Trocar de classe desfaz os poderes da classe anterior
        val base = if (sheet.classId != null && sheet.classId != cls.id) {
            sheet.classPowers.distinct().fold(sheet.copy(classPowers = emptyList())) { acc, name ->
                ClassPowerCatalog.find(sheet.classId, name)?.let { removePowerEffects(acc, it.asPower()) } ?: acc
            }
        } else sheet
        return applyClassTo(base, cls, previous)
    }

    private fun applyClassTo(sheet: ManualSheet, cls: CatalogClass, previous: Set<String>): ManualSheet {
        val pathId = sheet.classPathId.takeIf { sheet.classId == cls.id && cls.pathById(it) != null }
        val casting = cls.castingFor(pathId)
        val picks = sheet.skillPicks ?: SkillPicks(other = sheet.trainedSkills - cls.fixedSkills)
        val updated = sheet.copy(
            classId = cls.id,
            classPathId = pathId,
            className = cls.name,
            hpFirstLevel = cls.hpFirstLevel,
            hpPerLevel = cls.hpPerLevel,
            mpPerLevel = cls.mpPerLevel,
            mpAttribute = cls.mpAttribute ?: casting?.attribute,
            spellAttribute = casting?.attribute,
            skillPicks = if (sheet.classId == cls.id) picks else picks.copy(classFixedChoice = null, classChoices = emptySet()),
            abilities = if (sheet.classId == cls.id) sheet.abilities else sheet.abilities.filterNot { it.name in previous },
            // Ao trocar de classe, a quantidade de poderes concedidos pode mudar
            grantedPowers = if (sheet.classId == cls.id) sheet.grantedPowers else sheet.grantedPowers.take(ClassAbilities.grantedPowerCount(cls.id)).toSet(),
        )
        return refresh(updated)
    }

    /** Escolhe o caminho da classe (ex.: arcanista mago), que define o atributo das magias e dos PM. */
    fun applyPath(sheet: ManualSheet, path: ClassPath): ManualSheet = sheet.copy(
        classPathId = path.id,
        mpAttribute = path.spellcasting.attribute,
        spellAttribute = path.spellcasting.attribute,
    )

    fun applyRace(sheet: ManualSheet, race: CatalogRace): ManualSheet {
        val previous = RaceCatalog.byId(sheet.raceId)
        // Remove o que a raça anterior tinha adicionado
        val cleaned = if (previous != null && previous.id != race.id) {
            sheet.copy(
                abilities = sheet.abilities.filterNot { a -> previous.abilities.any { it.name == a.name } },
                skillBonuses = sheet.skillBonuses.mapValues { (_, list) -> list.filterNot { b -> previous.skillBonuses.values.flatten().any { it.label == b.label } } },
                mpBonuses = sheet.mpBonuses.filterNot { b -> previous.mpBonuses.any { it.label == b.label } },
                defenseBonuses = sheet.defenseBonuses.filterNot { b -> previous.defenseBonuses.any { it.label == b.label } },
                raceAttributeChoices = emptySet(),
                skillPicks = sheet.skillPicks?.copy(race = emptySet(), raceBonus = emptySet()),
            )
        } else {
            sheet
        }
        val updated = cleaned.copy(
            raceId = race.id,
            race = race.name,
            baseSpeed = race.speed,
            abilities = mergeEntries(cleaned.abilities, race.abilities),
            skillBonuses = race.skillBonuses.entries.fold(cleaned.skillBonuses) { acc, (skill, bonuses) ->
                acc + (skill to mergeBonuses(acc[skill].orEmpty(), bonuses))
            },
            mpBonuses = mergeBonuses(cleaned.mpBonuses, race.mpBonuses),
            defenseBonuses = mergeBonuses(cleaned.defenseBonuses, race.defenseBonuses),
        )
        return refresh(updated)
    }

    fun applyOrigin(sheet: ManualSheet, origin: CatalogOrigin): ManualSheet {
        if (sheet.originId == origin.id) return sheet
        // Trocar de origem desfaz os benefícios escolhidos da anterior
        var updated = sheet.originPowers.fold(sheet) { acc, name -> PowerCatalog.any(name)?.let { removePowerEffects(acc, it) } ?: acc }
        updated = updated.copy(
            originId = origin.id,
            origin = origin.name,
            originPowers = emptySet(),
            skillPicks = updated.skillPicks?.copy(origin = emptySet()),
        )
        return refresh(updated)
    }

    // ============================== Atualização geral ==============================

    /** Recalcula atributos, habilidades de classe e perícias a partir das escolhas. */
    fun refresh(sheet: ManualSheet): ManualSheet = sheet
        .let(::recomputeAttributes)
        .let(::syncClassAbilities)
        .let(::recomputeSkills)
        .let(::syncWildEmpathy)

    // ============================== Atributos ==============================

    /** Custo da compra de pontos, ou null se algum valor estiver fora da tabela. */
    fun pointBuyCost(sheet: ManualSheet): Int? = runCatching { PointBuy.totalCost(sheet.baseAttributes) }.getOrNull()

    /** Ajuste racial de cada atributo, já com as escolhas de +1. */
    fun racialAdjustments(sheet: ManualSheet): Map<Attribute, Int> {
        val race = RaceCatalog.byId(sheet.raceId) ?: return emptyMap()
        val choices = sheet.raceAttributeChoices.filter { it !in race.attributeChoiceExcluded }.take(race.attributeChoiceCount)
        return Attribute.entries.associateWith { (race.fixedAttributes[it] ?: 0) + (if (it in choices) 1 else 0) }
    }

    fun recomputeAttributes(sheet: ManualSheet): ManualSheet {
        if (sheet.attributeMode == null) return sheet
        val racial = racialAdjustments(sheet)
        val final = Attribute.entries.associateWith {
            (sheet.baseAttributes[it] ?: 0) + (racial[it] ?: 0) + (sheet.attributeIncreases[it] ?: 0)
        }
        return sheet.copy(attributes = final)
    }

    // ============================== Habilidades de classe ==============================

    /** Adiciona as habilidades que o nível atual libera e remove as de níveis acima. */
    fun syncClassAbilities(sheet: ManualSheet): ManualSheet {
        val abilities = ClassAbilities.of(sheet.classId)
        if (abilities.isEmpty()) return sheet
        val tooHigh = abilities.filter { it.level > sheet.level }.map { it.name }.toSet()
        val earned = abilities.filter { it.level <= sheet.level }.map { SheetEntry(it.name, "${it.summary} (${it.level}º nível)") }
        return sheet.copy(abilities = mergeEntries(sheet.abilities.filterNot { it.name in tooHigh }, earned))
    }

    /** Empatia Selvagem recebida da raça e da classe: +2 em Adestramento (p. 21 e 61). */
    private fun syncWildEmpathy(sheet: ManualSheet): ManualSheet {
        val label = "Empatia Selvagem (recebida novamente)"
        val fromRace = RaceCatalog.byId(sheet.raceId)?.abilities?.any { it.name == "Empatia Selvagem" } == true
        val fromClass = ClassAbilities.of(sheet.classId).any { it.name == "Empatia Selvagem" && it.level <= sheet.level }
        if (sheet.raceId == null || sheet.classId == null) return sheet
        val current = sheet.skillBonuses[Skill.ADESTRAMENTO].orEmpty().filterNot { it.label == label }
        val updated = if (fromRace && fromClass) current + SheetBonus(label, 2) else current
        val bonuses = if (updated.isEmpty()) sheet.skillBonuses - Skill.ADESTRAMENTO else sheet.skillBonuses + (Skill.ADESTRAMENTO to updated)
        return sheet.copy(skillBonuses = bonuses)
    }

    // ============================== Perícias ==============================

    /** Os grupos de escolha de perícias, com limites e listas permitidas. Vazio sem classe do catálogo. */
    fun skillGroups(sheet: ManualSheet): List<SkillGroup> {
        val cls = ClassCatalog.byId(sheet.classId) ?: return emptyList()
        val picks = sheet.skillPicks ?: SkillPicks()
        val all = Skill.entries.toSet()
        val groups = mutableListOf<SkillGroup>()

        groups += SkillGroup(SkillGroupId.CLASS_FIXED, "Da classe (automáticas)", "Sempre treinadas por ${cls.name}.",
            cls.fixedSkills.size, cls.fixedSkills, cls.fixedSkills, locked = true)
        if (cls.fixedChoice.isNotEmpty()) {
            groups += SkillGroup(SkillGroupId.CLASS_FIXED_CHOICE, "Da classe: ${cls.fixedChoice.joinToString(" ou ") { it.displayName }}",
                "Escolha uma.", 1, cls.fixedChoice, setOfNotNull(picks.classFixedChoice))
        }
        groups += SkillGroup(SkillGroupId.CLASS, "Da classe: escolha ${cls.skillChoiceCount}",
            "Só perícias da lista de ${cls.name}.", cls.skillChoiceCount, cls.skillChoiceList, picks.classChoices)

        OriginCatalog.byId(sheet.originId)?.let { origin ->
            val limit = (origin.benefitCount - sheet.originPowers.size).coerceAtLeast(0)
            groups += SkillGroup(SkillGroupId.ORIGIN, "Da origem",
                "A origem dá ${origin.benefitCount} benefícios entre perícias e poderes; ${sheet.originPowers.size} já são poderes.",
                limit, origin.skillOptions, picks.origin)
        }
        RaceCatalog.byId(sheet.raceId)?.let { race ->
            if (race.trainedSkillChoices > 0) {
                groups += SkillGroup(SkillGroupId.RACE, "Da raça: ${race.name}",
                    "Treinado em ${race.trainedSkillChoices} perícias quaisquer.", race.trainedSkillChoices, all, picks.race)
            }
            if (race.bonusSkillChoices > 0) {
                groups += SkillGroup(SkillGroupId.RACE_BONUS, "Deformidade (+2)",
                    "+2 em ${race.bonusSkillChoices} perícias quaisquer (não dá treinamento).", race.bonusSkillChoices, all,
                    picks.raceBonus, grantsTraining = false)
            }
        }
        val int = sheet.attributes[Attribute.INTELIGENCIA] ?: 0
        groups += SkillGroup(SkillGroupId.INTELLIGENCE, "Por Inteligência",
            if (int > 0) "Inteligência $int: treinado em $int perícias quaisquer." else "Inteligência $int: nenhuma perícia extra.",
            maxOf(0, int), all, picks.intelligence)
        groups += SkillGroup(SkillGroupId.OTHER, "Outras fontes",
            "Poderes ou regras da casa que dão treinamento.", null, all, picks.other)
        return groups
    }

    /** Por que uma perícia não pode ser marcada num grupo, ou null se pode. */
    fun blockedReason(sheet: ManualSheet, groupId: SkillGroupId, skill: Skill): String? {
        val groups = skillGroups(sheet)
        val group = groups.firstOrNull { it.id == groupId } ?: return "Grupo indisponível."
        if (skill in group.selected) return null
        if (group.locked) return "Automática."
        if (skill !in group.allowed) return "Não está na lista deste grupo."
        if (group.grantsTraining) {
            groups.firstOrNull { it.grantsTraining && it.id != groupId && skill in it.selected }?.let { return "Já treinada: ${it.title.lowercase()}." }
        }
        if (group.remaining != null && group.remaining!! <= 0) return "Limite deste grupo atingido."
        return null
    }

    fun setSkillGroup(sheet: ManualSheet, groupId: SkillGroupId, selected: Set<Skill>): ManualSheet {
        val picks = sheet.skillPicks ?: SkillPicks()
        val updated = when (groupId) {
            SkillGroupId.CLASS_FIXED -> picks
            SkillGroupId.CLASS_FIXED_CHOICE -> picks.copy(classFixedChoice = selected.firstOrNull())
            SkillGroupId.CLASS -> picks.copy(classChoices = selected)
            SkillGroupId.ORIGIN -> picks.copy(origin = selected)
            SkillGroupId.RACE -> picks.copy(race = selected)
            SkillGroupId.INTELLIGENCE -> picks.copy(intelligence = selected)
            SkillGroupId.RACE_BONUS -> picks.copy(raceBonus = selected)
            SkillGroupId.OTHER -> picks.copy(other = selected)
        }
        return refresh(sheet.copy(skillPicks = updated))
    }

    /** Corrige escolhas que deixaram de valer e recalcula as perícias treinadas. */
    fun recomputeSkills(sheet: ManualSheet): ManualSheet {
        val cls = ClassCatalog.byId(sheet.classId) ?: return sheet
        val p = sheet.skillPicks ?: return sheet
        val origin = OriginCatalog.byId(sheet.originId)
        val race = RaceCatalog.byId(sheet.raceId)
        val int = maxOf(0, sheet.attributes[Attribute.INTELIGENCIA] ?: 0)
        val picks = p.copy(
            classFixedChoice = p.classFixedChoice?.takeIf { it in cls.fixedChoice },
            classChoices = p.classChoices.filter { it in cls.skillChoiceList }.take(cls.skillChoiceCount).toSet(),
            origin = if (origin == null) emptySet() else p.origin.filter { it in origin.skillOptions }
                .take((origin.benefitCount - sheet.originPowers.size).coerceAtLeast(0)).toSet(),
            race = p.race.take(race?.trainedSkillChoices ?: 0).toSet(),
            raceBonus = p.raceBonus.take(race?.bonusSkillChoices ?: 0).toSet(),
            intelligence = p.intelligence.take(int).toSet(),
        )
        val trained = cls.fixedSkills + setOfNotNull(picks.classFixedChoice) + picks.classChoices + picks.origin +
            picks.race + picks.intelligence + picks.other
        // Deformidade: +2 nas perícias escolhidas
        val deformity = "Deformidade"
        val bonuses = Skill.entries.associateWith { skill ->
            sheet.skillBonuses[skill].orEmpty().filterNot { it.label == deformity } +
                (if (skill in picks.raceBonus) listOf(SheetBonus(deformity, 2)) else emptyList())
        }.filterValues { it.isNotEmpty() }
        return sheet.copy(skillPicks = picks, trainedSkills = trained, skillBonuses = bonuses)
    }

    // ============================== Divindade e poderes ==============================

    fun setDeity(sheet: ManualSheet, deity: CatalogDeity?): ManualSheet {
        val withoutPowers = sheet.grantedPowers.fold(sheet) { acc, name -> PowerCatalog.granted(name)?.let { removePowerEffects(acc, it) } ?: acc }
        return withoutPowers.copy(deityId = deity?.id, deity = deity?.name.orEmpty(), grantedPowers = emptySet())
    }

    fun grantedPowerLimit(sheet: ManualSheet): Int = if (sheet.deityId == null) 0 else ClassAbilities.grantedPowerCount(sheet.classId)

    fun setGrantedPower(sheet: ManualSheet, name: String, chosen: Boolean): ManualSheet {
        val power = PowerCatalog.granted(name) ?: return sheet
        return if (chosen && name !in sheet.grantedPowers) {
            applyPowerEffects(sheet.copy(grantedPowers = sheet.grantedPowers + name), power)
        } else if (!chosen && name in sheet.grantedPowers) {
            removePowerEffects(sheet.copy(grantedPowers = sheet.grantedPowers - name), power)
        } else sheet
    }

    /** Poderes de origem ocupam benefícios: no máximo 2 contando com as perícias da origem. */
    fun originPowerLimit(sheet: ManualSheet): Int {
        val origin = OriginCatalog.byId(sheet.originId) ?: return 0
        return (origin.benefitCount - (sheet.skillPicks?.origin?.size ?: 0)).coerceAtLeast(0)
    }

    fun setOriginPower(sheet: ManualSheet, name: String, chosen: Boolean): ManualSheet {
        val power = PowerCatalog.origin(name) ?: return sheet
        val updated = if (chosen && name !in sheet.originPowers) {
            applyPowerEffects(sheet.copy(originPowers = sheet.originPowers + name), power)
        } else if (!chosen && name in sheet.originPowers) {
            removePowerEffects(sheet.copy(originPowers = sheet.originPowers - name), power)
        } else sheet
        return refresh(updated)
    }

    /** Adiciona a descrição do poder e os efeitos que o app sabe aplicar. */
    fun applyPowerEffects(sheet: ManualSheet, power: CatalogPower): ManualSheet {
        val e = power.effects
        var s = sheet.copy(abilities = mergeEntries(sheet.abilities, listOf(SheetEntry(power.name, "${power.summary} (p. ${power.page})"))))
        if (e.skills.isNotEmpty()) {
            s = s.copy(skillBonuses = e.skills.entries.fold(s.skillBonuses) { acc, (skill, value) ->
                acc + (skill to mergeBonuses(acc[skill].orEmpty(), listOf(SheetBonus(power.name, value))))
            })
        }
        if (e.defense != 0) s = s.copy(defenseBonuses = mergeBonuses(s.defenseBonuses, listOf(SheetBonus(power.name, e.defense))))
        if (e.hpPerLevel != 0) s = s.copy(hpBonuses = mergeBonuses(s.hpBonuses, listOf(SheetBonus(power.name, e.hpPerLevel, perLevel = true))))
        if (e.mpPerLevel != 0) s = s.copy(mpBonuses = mergeBonuses(s.mpBonuses, listOf(SheetBonus(power.name, e.mpPerLevel, perLevel = true))))
        e.spell?.let { name ->
            val spell = SpellCatalog.find(name)
            if (spell != null && s.spells.none { it.name == spell.name }) {
                s = s.copy(spells = s.spells + spell.toSheetSpell("Por ${power.name}"))
            }
        }
        return s
    }

    fun removePowerEffects(sheet: ManualSheet, power: CatalogPower): ManualSheet = sheet.copy(
        abilities = sheet.abilities.filterNot { it.name == power.name },
        skillBonuses = sheet.skillBonuses.mapValues { (_, list) -> list.filterNot { it.label == power.name } }.filterValues { it.isNotEmpty() },
        defenseBonuses = sheet.defenseBonuses.filterNot { it.label == power.name },
        hpBonuses = sheet.hpBonuses.filterNot { it.label == power.name },
        mpBonuses = sheet.mpBonuses.filterNot { it.label == power.name },
        spells = sheet.spells.filterNot { it.notes == "Por ${power.name}" },
    )

    // ============================== Magias ==============================

    /** As regras de magia que valem para a ficha (classe e caminho), ou null se não for conjurador. */
    fun casting(sheet: ManualSheet): Spellcasting? = ClassCatalog.byId(sheet.classId)?.castingFor(sheet.classPathId)

    /** Magias que o personagem pode escolher agora, pelas regras da classe. Vazio se não for conjurador. */
    fun availableSpells(sheet: ManualSheet): List<CatalogSpell> {
        val casting = casting(sheet) ?: return emptyList()
        return SpellCatalog.available(casting, sheet.spellSchools, sheet.level)
    }

    /** Quantas magias de classe o personagem conhece no nível atual, ou null se não for conjurador. */
    fun classSpellsKnown(sheet: ManualSheet): Int? = casting(sheet)?.let { casting ->
        casting.spellsKnown(sheet.level) + 2 * sheet.classPowers.count { it in ClassPowerCatalog.EXTRA_SPELL_POWERS }
    }

    // ============================== Poderes de classe ==============================

    /** Um poder de classe no 2º nível e a cada nível seguinte. */
    fun classPowerLimit(sheet: ManualSheet): Int =
        if (ClassCatalog.byId(sheet.classId) == null) 0 else (sheet.level - 1).coerceAtLeast(0)

    /** Pré-requisitos que o personagem ainda não cumpre (lista vazia = pode escolher). */
    fun unmetRequirements(sheet: ManualSheet, power: ClassPowerDef): List<String> {
        val r = power.requirement
        val missing = mutableListOf<String>()
        r.attributes.forEach { (attr, min) -> if ((sheet.attributes[attr] ?: 0) < min) missing += "${attr.displayName} $min" }
        r.powers.forEach { if (it !in sheet.classPowers) missing += it }
        if (sheet.level < r.level) missing += "${r.level}º nível"
        r.trained.forEach { if (it !in sheet.trainedSkills) missing += "treinado em ${it.displayName}" }
        if (r.paths.isNotEmpty() && sheet.classPathId !in r.paths) {
            missing += r.paths.mapNotNull { id -> ClassCatalog.byId(sheet.classId)?.pathById(id)?.name }.joinToString(" ou ")
        }
        r.spell?.let { spell -> if (sheet.spells.none { it.name.equals(spell, ignoreCase = true) }) missing += "conhecer $spell" }
        return missing
    }

    fun addClassPower(sheet: ManualSheet, name: String): ManualSheet {
        val power = ClassPowerCatalog.find(sheet.classId, name) ?: return sheet
        if (!power.repeatable && name in sheet.classPowers) return sheet
        val firstTime = name !in sheet.classPowers
        val updated = sheet.copy(classPowers = sheet.classPowers + name)
        return if (firstTime) applyPowerEffects(updated, power.asPower()) else updated
    }

    fun removeClassPower(sheet: ManualSheet, name: String): ManualSheet {
        val index = sheet.classPowers.lastIndexOf(name)
        if (index < 0) return sheet
        val remaining = sheet.classPowers.toMutableList().also { it.removeAt(index) }
        val updated = sheet.copy(classPowers = remaining)
        val power = ClassPowerCatalog.find(sheet.classId, name)
        return if (name !in remaining && power != null) removePowerEffects(updated, power.asPower()) else updated
    }

    // ============================== Utilitários ==============================

    private fun mergeEntries(current: List<SheetEntry>, new: List<SheetEntry>) =
        current + new.filter { n -> current.none { it.name.equals(n.name, ignoreCase = true) } }

    private fun mergeBonuses(current: List<SheetBonus>, new: List<SheetBonus>) =
        current + new.filter { n -> current.none { it.label.equals(n.label, ignoreCase = true) } }
}
