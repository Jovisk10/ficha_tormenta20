package com.jovis.t20.rules

data class ValidationIssue(val message: String)

/**
 * Verifica se a montagem do personagem segue o livro.
 * Não impede o cálculo: uma ficha com erros continua sendo exibida, mas com avisos.
 */
object Validator {

    fun validate(character: Character): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val finals = RuleEngine.allAttributes(character).mapValues { it.value.total }
        val cls = character.characterClass

        // ---------- Atributos (p. 17) ----------
        if (character.attributeMethod == AttributeMethod.POINT_BUY) {
            val cost = runCatching { PointBuy.totalCost(character.baseAttributes) }.getOrNull()
            when {
                cost == null -> issues += ValidationIssue("Há atributos base fora da faixa da compra por pontos (–1 a 4).")
                cost > PointBuy.BUDGET -> issues += ValidationIssue(
                    "A compra de atributos custa $cost pontos; o limite é ${PointBuy.BUDGET} (p. 17)."
                )
            }
        }

        // ---------- Perícias ----------
        if (character.classSkillChoices.size != cls.skillChoiceCount) {
            issues += ValidationIssue(
                "${cls.name} escolhe ${cls.skillChoiceCount} perícias da lista da classe; " +
                    "foram escolhidas ${character.classSkillChoices.size} (p. ${cls.page})."
            )
        }
        (character.classSkillChoices - cls.skillChoiceList).forEach {
            issues += ValidationIssue("${it.displayName} não está na lista de perícias de ${cls.name}.")
        }

        val origin = character.origin
        val benefits = origin.skills.size + origin.powers.size
        if (benefits != 2) {
            issues += ValidationIssue("A origem ${origin.origin.name} concede 2 benefícios; foram escolhidos $benefits (p. 85).")
        }
        (origin.skills - origin.origin.skillOptions).forEach {
            issues += ValidationIssue("${it.displayName} não é um benefício da origem ${origin.origin.name}.")
        }
        (origin.powers - origin.origin.powerOptions).forEach {
            issues += ValidationIssue("${it.name} não é um benefício da origem ${origin.origin.name}.")
        }

        val allowedInt = maxOf(0, finals.getValue(Attribute.INTELIGENCIA))
        if (character.intelligenceSkills.size != allowedInt) {
            issues += ValidationIssue(
                "Inteligência ${finals.getValue(Attribute.INTELIGENCIA)} concede $allowedInt perícia(s) extra(s); " +
                    "foram escolhidas ${character.intelligenceSkills.size} (p. 17)."
            )
        }

        val skillSources = listOf(cls.fixedSkills, character.classSkillChoices, origin.skills, character.intelligenceSkills)
        skillSources.flatten().groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach {
            issues += ValidationIssue("${it.displayName} foi treinada por mais de uma fonte; a escolha repetida não rende bônus.")
        }

        // ---------- Poderes ----------
        RuleEngine.allPowers(character).forEach { power ->
            power.unmetPrerequisite(finals)?.let {
                issues += ValidationIssue("${power.name} exige $it (p. ${power.page}).")
            }
        }

        // ---------- Divindade ----------
        cls.allowedDeities?.let { allowed ->
            val deity = character.deity
            if (deity == null || deity.name !in allowed) {
                issues += ValidationIssue("${cls.name} precisa ser devoto de: ${allowed.joinToString(", ")} (p. ${cls.page}).")
            }
        }

        // ---------- Equipamento ----------
        val armor = character.armor
        if (armor != null) {
            if (character.inventory.none { it.item == armor }) {
                issues += ValidationIssue("A armadura vestida (${armor.name}) não está no inventário.")
            }
            if (!RuleEngine.isProficient(character, armor)) {
                issues += ValidationIssue(
                    "${cls.name} não é proficiente em ${armor.name}: a penalidade de armadura se aplica a todas as perícias de Força e Destreza (p. 152)."
                )
            }
            val deity = character.deity
            if (deity != null && deity.forbidsMetalArmor && armor.metal) {
                issues += ValidationIssue("Devotos de ${deity.name} não podem usar armaduras de metal como ${armor.name} (p. ${deity.page}).")
            }
        }

        val load = RuleEngine.currentLoad(character)
        val maxLoad = RuleEngine.maxLoad(character)
        if (load > maxLoad) {
            issues += ValidationIssue("Carga de ${formatSlots(load)} espaços passa do máximo de $maxLoad (p. 141).")
        }

        // ---------- Magias ----------
        val casting = cls.spellcasting
        if (casting == null) {
            if (character.classSpells.isNotEmpty()) {
                issues += ValidationIssue("${cls.name} não lança magias, mas há magias de classe na ficha.")
            }
        } else {
            if (character.spellSchools.size != casting.schoolCount) {
                issues += ValidationIssue(
                    "${cls.name} escolhe ${casting.schoolCount} escolas de magia; foram escolhidas ${character.spellSchools.size} (p. ${cls.page})."
                )
            }
            val expected = casting.spellsKnown(character.level)
            if (character.classSpells.size != expected) {
                issues += ValidationIssue(
                    "${cls.name} de ${character.level}º nível conhece $expected magias de classe; há ${character.classSpells.size} (p. ${cls.page})."
                )
            }
            val maxCircle = casting.maxCircle(character.level)
            character.classSpells.forEach { spell ->
                if (!spell.type.castableBy(casting.type)) {
                    issues += ValidationIssue("${spell.name} é uma magia ${spell.type.displayName.lowercase()}; ${cls.name} lança magias ${casting.type.displayName.lowercase()}s.")
                }
                if (spell.school !in character.spellSchools) {
                    issues += ValidationIssue("${spell.name} é de ${spell.school.displayName}, que não está entre as escolas escolhidas.")
                }
                if (spell.circle > maxCircle) {
                    issues += ValidationIssue("${spell.name} é de ${spell.circle}º círculo; no ${character.level}º nível, ${cls.name} lança até o ${maxCircle}º.")
                }
            }
        }

        return issues
    }

    private fun formatSlots(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')
}
