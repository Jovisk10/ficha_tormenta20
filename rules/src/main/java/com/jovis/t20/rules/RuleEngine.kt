package com.jovis.t20.rules

/** Resultado de um ataque com uma arma, pronto para exibir na ficha. */
data class AttackProfile(
    val weapon: WeaponDefinition,
    val mode: AttackMode,
    val attack: DerivedValue,
    val damageDice: String,
    val damageBonus: DerivedValue,
    val critical: String,
) {
    val damageText: String
        get() = when {
            damageBonus.total > 0 -> "$damageDice+${damageBonus.total}"
            damageBonus.total < 0 -> "$damageDice${damageBonus.total}"
            else -> damageDice
        }
}

/** Uma magia conhecida, com de onde ela veio e quanto custa. */
data class KnownSpell(
    val spell: SpellDefinition,
    val sources: List<Source>,
    val cost: DerivedValue,
)

/** O motor: recebe as escolhas do personagem e devolve valores calculados e explicados. */
object RuleEngine {

    // ---------- Atributos ----------

    fun attribute(character: Character, attribute: Attribute): DerivedValue {
        val base = Contribution("Base", character.baseAttributes.getValue(attribute), Source(SourceType.BASE, "Valor base"))
        return build(listOf(base), modifiersFor(character, StatTarget.AttributeTarget(attribute)))
    }

    fun allAttributes(character: Character): Map<Attribute, DerivedValue> =
        Attribute.entries.associateWith { attribute(character, it) }

    private fun attrValue(character: Character, attribute: Attribute): Int = attribute(character, attribute).total

    // ---------- Progressão (p. 35 e p. 114) ----------

    fun halfLevel(character: Character): Int = character.level / 2

    /** +2 do 1º ao 6º nível, +4 do 7º ao 14º, +6 do 15º em diante (p. 114). */
    fun trainingBonus(character: Character): Int = when (character.level) {
        in 1..6 -> 2
        in 7..14 -> 4
        else -> 6
    }

    // ---------- Pontos de Vida e Mana ----------

    /**
     * PV de classe no 1º nível + Con, e PV por nível + Con nos seguintes,
     * com mínimo de 1 PV por nível ao subir (p. 35).
     */
    fun maxHp(character: Character): DerivedValue {
        val cls = character.characterClass
        val con = attrValue(character, Attribute.CONSTITUICAO)
        val classSource = Source(SourceType.CLASS, cls.name)
        val conSource = Source(SourceType.BASE, Attribute.CONSTITUICAO.displayName)

        val parts = mutableListOf(
            Contribution("${cls.name} (1º nível)", cls.hpFirstLevel, classSource),
            Contribution("Constituição (1º nível)", con, conSource),
        )
        val extraLevels = character.level - 1
        if (extraLevels > 0) {
            val range = if (extraLevels == 1) "2º nível" else "níveis 2 a ${character.level}"
            if (cls.hpPerLevel + con >= 1) {
                parts += Contribution("${cls.name} ($range)", cls.hpPerLevel * extraLevels, classSource)
                parts += Contribution("Constituição ($range)", con * extraLevels, conSource)
            } else {
                parts += Contribution("Mínimo de 1 PV por nível ($range)", extraLevels, classSource)
            }
        }
        return build(parts, modifiersFor(character, StatTarget.MaxHp))
    }

    fun maxMp(character: Character): DerivedValue {
        val cls = character.characterClass
        val classSource = Source(SourceType.CLASS, cls.name)
        val parts = mutableListOf(
            Contribution("${cls.name} (${cls.mpPerLevel} × ${character.level})", cls.mpPerLevel * character.level, classSource),
        )
        cls.mpAttribute?.let {
            parts += Contribution(it.displayName, attrValue(character, it), Source(SourceType.BASE, it.displayName))
        }
        return build(parts, modifiersFor(character, StatTarget.MaxMp))
    }

    // ---------- Proficiências (p. 32, 142, 152) ----------

    fun isProficient(character: Character, weapon: WeaponDefinition): Boolean =
        weapon.proficiency == WeaponProficiency.SIMPLE ||
            weapon.proficiency in character.characterClass.weaponProficiencies

    fun isProficient(character: Character, armor: ArmorDefinition): Boolean =
        armor.category == ArmorCategory.LIGHT ||
            armor.category in character.characterClass.armorProficiencies

    // ---------- Defesa (p. 106 e p. 152) ----------

    /** 10 + Destreza + bônus de armadura. Armaduras pesadas não somam Destreza (p. 152). */
    fun defense(character: Character): DerivedValue {
        val armor = character.armor
        val parts = mutableListOf(Contribution("Base", 10, Source(SourceType.BASE, "Regra de Defesa")))
        if (armor?.category == ArmorCategory.HEAVY) {
            parts += Contribution("Destreza (não se aplica com armadura pesada)", 0, Source(SourceType.ITEM, armor.name))
        } else {
            parts += Contribution("Destreza", attrValue(character, Attribute.DESTREZA), Source(SourceType.BASE, "Destreza"))
        }
        armor?.let { parts += Contribution(it.name, it.defenseBonus, Source(SourceType.ITEM, it.name)) }
        return build(parts, modifiersFor(character, StatTarget.Defense))
    }

    // ---------- Carga (p. 141) ----------

    /** 10 espaços +2 por ponto de Força (ou –1 por ponto de Força negativo). */
    fun loadLimit(character: Character): DerivedValue {
        val str = attrValue(character, Attribute.FORCA)
        val parts = listOf(
            Contribution("Base", 10, Source(SourceType.BASE, "Regra de carga")),
            Contribution(
                if (str >= 0) "Força (+2 por ponto)" else "Força (–1 por ponto negativo)",
                if (str >= 0) str * 2 else str,
                Source(SourceType.BASE, "Força"),
            ),
        )
        return build(parts, modifiersFor(character, StatTarget.LoadLimit))
    }

    /** Ninguém pode carregar mais que o dobro do limite. */
    fun maxLoad(character: Character): Int = loadLimit(character).total * 2

    fun currentLoad(character: Character): Double = character.inventory.sumOf { it.totalSlots }

    /** Acima do limite: penalidade de armadura –5 e deslocamento –3m. */
    fun isOverloaded(character: Character): Boolean = currentLoad(character) > loadLimit(character).total

    // ---------- Perícias (p. 114-115) ----------

    fun trainedSkills(character: Character): Set<Skill> =
        character.characterClass.fixedSkills +
            character.classSkillChoices +
            character.origin.skills +
            character.intelligenceSkills

    fun isTrained(character: Character, skill: Skill): Boolean = skill in trainedSkills(character)

    /** Perícias "somente treinadas" não podem ser usadas sem treino, mesmo tendo valor calculado. */
    fun canUse(character: Character, skill: Skill): Boolean = !skill.trainedOnly || isTrained(character, skill)

    /** Metade do nível + atributo-chave + bônus de treinamento (se treinado) + penalidades. */
    fun skill(character: Character, skill: Skill): DerivedValue {
        val parts = mutableListOf(
            Contribution("Metade do nível", halfLevel(character), Source(SourceType.LEVEL, "Nível ${character.level}")),
            Contribution(
                skill.keyAttribute.displayName,
                attrValue(character, skill.keyAttribute),
                Source(SourceType.BASE, skill.keyAttribute.displayName),
            ),
        )
        if (isTrained(character, skill)) {
            parts += Contribution("Treinamento", trainingBonus(character), Source(SourceType.LEVEL, "Treinamento"))
        }

        // Penalidade de armadura: perícias marcadas com "armadura" (p. 115) e, sem proficiência,
        // todas as perícias de Força e Destreza (p. 152). Aplicada uma única vez.
        val armor = character.armor
        if (armor != null && armor.armorPenalty != 0) {
            val nonProficient = !isProficient(character, armor) &&
                skill.keyAttribute in setOf(Attribute.FORCA, Attribute.DESTREZA)
            if (skill.armorPenalty || nonProficient) {
                val label = if (nonProficient && !skill.armorPenalty) "Armadura sem proficiência" else "Penalidade de armadura"
                parts += Contribution(label, armor.armorPenalty, Source(SourceType.ITEM, armor.name))
            }
        }
        // Sobrecarga: penalidade de armadura –5 (p. 141).
        if (skill.armorPenalty && isOverloaded(character)) {
            parts += Contribution("Sobrecarregado", -5, Source(SourceType.CONDITION, "Sobrecarregado"))
        }
        return build(parts, modifiersFor(character, StatTarget.SkillTarget(skill)))
    }

    fun allSkills(character: Character): Map<Skill, DerivedValue> =
        Skill.entries.associateWith { skill(character, it) }

    // ---------- Ataques (p. 142 e p. 230) ----------

    /**
     * Teste de ataque: Luta (corpo a corpo) ou Pontaria (distância), –5 sem proficiência.
     * Dano: dado da arma + Força em corpo a corpo e arremesso; disparo não soma atributo.
     */
    fun attack(character: Character, weapon: WeaponDefinition, mode: AttackMode = weapon.allowedModes.first()): AttackProfile {
        require(mode in weapon.allowedModes) { "${weapon.name} não pode ser usada no modo $mode." }

        val attackSkill = if (mode == AttackMode.MELEE) Skill.LUTA else Skill.PONTARIA
        val attackParts = mutableListOf(
            Contribution(attackSkill.displayName, skill(character, attackSkill).total, Source(SourceType.BASE, attackSkill.displayName)),
        )
        if (!isProficient(character, weapon)) {
            attackParts += Contribution("Sem proficiência", -5, Source(SourceType.ITEM, weapon.name))
        }

        val damageParts = mutableListOf<Contribution>()
        if (mode == AttackMode.MELEE || mode == AttackMode.THROWN) {
            damageParts += Contribution("Força", attrValue(character, Attribute.FORCA), Source(SourceType.BASE, "Força"))
        }

        return AttackProfile(
            weapon = weapon,
            mode = mode,
            attack = build(attackParts, modifiersFor(character, StatTarget.Attack)),
            damageDice = weapon.damageDice,
            damageBonus = build(damageParts, modifiersFor(character, StatTarget.WeaponDamage)),
            critical = weapon.criticalText,
        )
    }

    /** Ataques de todas as armas do inventário, em todos os modos possíveis. */
    fun allAttacks(character: Character): List<AttackProfile> =
        character.inventory.map { it.item }.filterIsInstance<WeaponDefinition>().distinct()
            .flatMap { weapon -> weapon.allowedModes.map { attack(character, weapon, it) } }

    // ---------- Magia (p. 170, 226, 227) ----------

    /** 10 + metade do nível + atributo-chave de magia. Null se a classe não lança magias. */
    fun spellDc(character: Character): DerivedValue? {
        val attribute = character.characterClass.spellcasting?.attribute ?: return null
        val parts = listOf(
            Contribution("Base", 10, Source(SourceType.BASE, "Regra de CD")),
            Contribution("Metade do nível", halfLevel(character), Source(SourceType.LEVEL, "Nível ${character.level}")),
            Contribution(attribute.displayName, attrValue(character, attribute), Source(SourceType.BASE, attribute.displayName)),
        )
        return build(parts, modifiersFor(character, StatTarget.SpellDc))
    }

    /** Magias de raça e de classe, sem repetir, com o custo final de cada uma. */
    fun knownSpells(character: Character): List<KnownSpell> {
        val sources = linkedMapOf<SpellDefinition, MutableList<Source>>()
        character.race.grantedSpells.forEach {
            sources.getOrPut(it.spell) { mutableListOf() } += Source(SourceType.RACE, "${character.race.raceName}: ${it.ability.displayName}")
        }
        character.classSpells.forEach {
            sources.getOrPut(it) { mutableListOf() } += Source(SourceType.CLASS, character.characterClass.name)
        }
        return sources.map { (spell, from) -> KnownSpell(spell, from, spellCost(character, spell)) }
    }

    /**
     * Custo pelo círculo (Tabela 4-1). Reduções não se acumulam: vale só a maior.
     * Nenhuma habilidade custa menos de 1 PM (p. 226).
     */
    fun spellCost(character: Character, spell: SpellDefinition): DerivedValue {
        val parts = mutableListOf(
            Contribution("${spell.circle}º círculo", SpellCost.of(spell.circle), Source(SourceType.BASE, "Tabela 4-1")),
        )
        val bestReduction = character.race.grantedSpells
            .filter { it.spell == spell && spell in character.classSpells && it.relearnDiscount > 0 }
            .map { Contribution("${it.ability.displayName} (aprendida novamente)", -it.relearnDiscount, Source(SourceType.ABILITY, it.ability.displayName)) }
            .minByOrNull { it.value }
        bestReduction?.let { parts += it }

        val sum = parts.sumOf { it.value }
        if (sum < 1) {
            parts += Contribution("Mínimo de 1 PM", 1 - sum, Source(SourceType.BASE, "Reduções de custo"))
        }
        return DerivedValue(parts)
    }

    // ---------- Habilidades e poderes ----------

    /** Todas as habilidades recebidas, com repetição (importa para regras como Empatia Selvagem). */
    fun abilities(character: Character): List<Ability> =
        character.race.abilities +
            character.characterClass.abilitiesByLevel
                .filterKeys { it <= character.level }
                .values.flatten()

    fun allPowers(character: Character): Set<GeneralPower> = character.origin.powers + character.generalPowers

    // ---------- Infraestrutura ----------

    private fun allModifiers(character: Character): List<Modifier> =
        character.race.modifiers() +
            allPowers(character).flatMap { it.modifiers(character.level) } +
            abilityRuleModifiers(character) +
            character.manualModifiers

    /** Regras que dependem de combinações de habilidades. */
    private fun abilityRuleModifiers(character: Character): List<Modifier> {
        val result = mutableListOf<Modifier>()
        // Empatia Selvagem recebida novamente: +2 em Adestramento (p. 21)
        if (abilities(character).count { it == Ability.EMPATIA_SELVAGEM } >= 2) {
            result += Modifier(
                StatTarget.SkillTarget(Skill.ADESTRAMENTO),
                +2,
                Source(SourceType.ABILITY, "Empatia Selvagem (recebida novamente)"),
            )
        }
        return result
    }

    private fun modifiersFor(character: Character, target: StatTarget): List<Modifier> =
        allModifiers(character).filter { it.target == target }

    private fun build(parts: List<Contribution>, modifiers: List<Modifier>): DerivedValue {
        val (situational, fixed) = modifiers.partition { it.isSituational }
        return DerivedValue(
            contributions = parts + fixed.map { Contribution(it.source.name, it.value, it.source) },
            situational = situational,
        )
    }
}
