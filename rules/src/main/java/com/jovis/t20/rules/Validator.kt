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

        // Compra de pontos (p. 17)
        if (character.attributeMethod == AttributeMethod.POINT_BUY) {
            val cost = runCatching { PointBuy.totalCost(character.baseAttributes) }.getOrNull()
            when {
                cost == null -> issues += ValidationIssue("Há atributos base fora da faixa da compra por pontos (–1 a 4).")
                cost > PointBuy.BUDGET -> issues += ValidationIssue(
                    "A compra de atributos custa $cost pontos; o limite é ${PointBuy.BUDGET} (p. 17)."
                )
            }
        }

        // Perícias da classe
        if (character.classSkillChoices.size != cls.skillChoiceCount) {
            issues += ValidationIssue(
                "${cls.name} escolhe ${cls.skillChoiceCount} perícias da lista da classe; " +
                    "foram escolhidas ${character.classSkillChoices.size} (p. ${cls.page})."
            )
        }
        (character.classSkillChoices - cls.skillChoiceList).forEach {
            issues += ValidationIssue("${it.displayName} não está na lista de perícias de ${cls.name}.")
        }

        // Origem: dois benefícios da lista (p. 85)
        val origin = character.origin
        val benefits = origin.skills.size + origin.powers.size
        if (benefits != 2) {
            issues += ValidationIssue(
                "A origem ${origin.origin.name} concede 2 benefícios; foram escolhidos $benefits (p. 85)."
            )
        }
        (origin.skills - origin.origin.skillOptions).forEach {
            issues += ValidationIssue("${it.displayName} não é um benefício da origem ${origin.origin.name}.")
        }
        (origin.powers - origin.origin.powerOptions).forEach {
            issues += ValidationIssue("${it.name} não é um benefício da origem ${origin.origin.name}.")
        }

        // Perícias por Inteligência: só se for positiva (p. 17)
        val allowedInt = maxOf(0, finals.getValue(Attribute.INTELIGENCIA))
        if (character.intelligenceSkills.size != allowedInt) {
            issues += ValidationIssue(
                "Inteligência ${finals.getValue(Attribute.INTELIGENCIA)} concede $allowedInt perícia(s) extra(s); " +
                    "foram escolhidas ${character.intelligenceSkills.size} (p. 17)."
            )
        }

        // Perícias treinadas em dobro não rendem nada a mais
        val sources = listOf(cls.fixedSkills, character.classSkillChoices, origin.skills, character.intelligenceSkills)
        sources.flatten().groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach {
            issues += ValidationIssue("${it.displayName} foi treinada por mais de uma fonte; a escolha repetida não rende bônus.")
        }

        // Pré-requisitos de poderes
        RuleEngine.allPowers(character).forEach { power ->
            power.unmetPrerequisite(finals)?.let {
                issues += ValidationIssue("${power.name} exige $it (p. ${power.page}).")
            }
        }

        return issues
    }
}
