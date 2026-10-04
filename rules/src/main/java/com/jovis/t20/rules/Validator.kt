package com.jovis.t20.rules

data class ValidationIssue(val message: String)

/**
 * Verifica se a montagem do personagem segue o livro.
 * Não impede o cálculo: uma ficha com erros continua sendo exibida, mas com avisos.
 */
object Validator {

    fun validate(character: Character): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        fun issue(message: String) { issues += ValidationIssue(message) }

        val context = RuleEngine.prerequisiteContext(character)
        val finals = context.attributes
        val cls = character.characterClass

        // ---------- Atributos (p. 17) ----------
        if (character.attributeMethod == AttributeMethod.POINT_BUY) {
            val cost = runCatching { PointBuy.totalCost(character.baseAttributes) }.getOrNull()
            when {
                cost == null -> issue("Há atributos base fora da faixa da compra por pontos (–1 a 4).")
                cost > PointBuy.BUDGET -> issue("A compra de atributos custa $cost pontos; o limite é ${PointBuy.BUDGET} (p. 17).")
            }
        }

        // ---------- Perícias ----------
        if (character.classSkillChoices.size != cls.skillChoiceCount) {
            issue(
                "${cls.name} escolhe ${cls.skillChoiceCount} perícias da lista da classe; " +
                    "foram escolhidas ${character.classSkillChoices.size} (p. ${cls.page})."
            )
        }
        (character.classSkillChoices - cls.skillChoiceList).forEach {
            issue("${it.displayName} não está na lista de perícias de ${cls.name}.")
        }

        val origin = character.origin
        val benefits = origin.skills.size + origin.powers.size
        if (benefits != 2) {
            issue("A origem ${origin.origin.name} concede 2 benefícios; foram escolhidos $benefits (p. 85).")
        }
        (origin.skills - origin.origin.skillOptions).forEach {
            issue("${it.displayName} não é um benefício da origem ${origin.origin.name}.")
        }
        (origin.powers - origin.origin.powerOptions).forEach {
            issue("${it.name} não é um benefício da origem ${origin.origin.name}.")
        }

        val allowedInt = maxOf(0, finals.getValue(Attribute.INTELIGENCIA))
        if (character.intelligenceSkills.size != allowedInt) {
            issue(
                "Inteligência ${finals.getValue(Attribute.INTELIGENCIA)} concede $allowedInt perícia(s) extra(s); " +
                    "foram escolhidas ${character.intelligenceSkills.size} (p. 17)."
            )
        }

        val skillSources = listOf(cls.fixedSkills, character.classSkillChoices, origin.skills, character.intelligenceSkills)
        skillSources.flatten().groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach {
            issue("${it.displayName} foi treinada por mais de uma fonte; a escolha repetida não rende bônus.")
        }

        // ---------- Poderes ----------
        val powers = RuleEngine.allPowers(character)
        powers.distinct().forEach { power ->
            val unmet = power.unmetPrerequisites(context)
            if (unmet.isNotEmpty()) issue("${power.name} exige ${unmet.joinToString(", ")} (p. ${power.page}).")
            if (!power.repeatable && powers.count { it == power } > 1) issue("${power.name} não pode ser escolhido mais de uma vez.")
        }

        val expectedClassPowers = cls.classPowersAt(character.level)
        if (character.classPowers.size != expectedClassPowers) {
            issue(
                "${cls.name} de ${character.level}º nível tem $expectedClassPowers poder(es) de classe; " +
                    "há ${character.classPowers.size} (p. ${cls.page})."
            )
        }
        character.classPowers.filter { it.className != cls.name }.distinct().forEach {
            issue("${it.name} é um poder de ${it.className}, não de ${cls.name}.")
        }

        // ---------- Divindade ----------
        val deity = character.deity
        cls.allowedDeities?.let { allowed ->
            if (deity == null || deity.name !in allowed) {
                issue("${cls.name} precisa ser devoto de: ${allowed.joinToString(", ")} (p. ${cls.page}).")
            }
        }
        if (cls.grantedPowerCount > 0 && deity != null && character.grantedPowers.size != cls.grantedPowerCount) {
            issue(
                "${cls.name} recebe ${cls.grantedPowerCount} poderes concedidos de ${deity.name}; " +
                    "há ${character.grantedPowers.size} (p. ${cls.page})."
            )
        }
        character.grantedPowers.forEach { power ->
            if (deity == null || power !in deity.grantedPowers) {
                issue("${power.name} não é um poder concedido de ${deity?.name ?: "nenhuma divindade escolhida"}.")
            }
        }

        // ---------- Companheiros animais (p. 61 e 260-261) ----------
        val companionPowers = character.classPowers.count { it == DruidPowers.CompanheiroAnimal }
        if (character.companions.size != companionPowers) {
            issue(
                "Há $companionPowers poder(es) Companheiro Animal e ${character.companions.size} companheiro(s) " +
                    "definido(s); defina o tipo de cada companheiro (p. 61)."
            )
        }
        val tier = RuleEngine.companionTier(character)
        character.companions.forEach { companion ->
            if (companion.type !in Partners.COMPANION_TYPES) {
                issue("${companion.name}: ${companion.type.displayName} não é um tipo permitido para companheiro animal (p. 61).")
            }
            if (companion.type == PartnerType.AJUDANTE) {
                val expected = Partners.helperSkillCount(tier)
                if (companion.chosenSkills.size != expected) {
                    issue("${companion.name}: um ajudante ${tier.displayName} dá bônus em $expected perícias; há ${companion.chosenSkills.size} (p. 260).")
                }
                if (companion.chosenSkills.any { it == Skill.LUTA || it == Skill.PONTARIA }) {
                    issue("${companion.name}: um ajudante não pode dar bônus em Luta ou Pontaria (p. 260).")
                }
            } else if (companion.chosenSkills.isNotEmpty()) {
                issue("${companion.name}: só ajudantes escolhem perícias.")
            }
        }
        if (character.companions.distinctBy { it.type }.size < character.companions.size) {
            issue("Cada Companheiro Animal deve ser um companheiro diferente (p. 61).")
        }
        val limit = Partners.limit(character.level)
        if (character.companions.size > limit) {
            issue("No ${character.level}º nível, o limite é de $limit parceiro(s); há ${character.companions.size} (p. 261).")
        }

        // ---------- Forma selvagem ----------
        if (character.activeWildShape != null && DruidPowers.FormaSelvagem !in powers) {
            issue("Forma selvagem ativa sem o poder Forma Selvagem (p. 62).")
        }

        // ---------- Equipamento ----------
        val armor = character.armor
        if (armor != null) {
            if (character.inventory.none { it.item == armor }) issue("A armadura vestida (${armor.name}) não está no inventário.")
            if (!RuleEngine.isProficient(character, armor)) {
                issue("${cls.name} não é proficiente em ${armor.name}: a penalidade de armadura se aplica a todas as perícias de Força e Destreza (p. 152).")
            }
            if (deity != null && deity.forbidsMetalArmor && armor.metal) {
                issue("Devotos de ${deity.name} não podem usar armaduras de metal como ${armor.name} (p. ${deity.page}).")
            }
        }
        val shield = character.shield
        if (shield != null) {
            if (character.inventory.none { it.item == shield }) issue("O escudo empunhado (${shield.name}) não está no inventário.")
            if (!RuleEngine.isProficientWithShields(character)) {
                issue("${cls.name} não é proficiente em escudos: a penalidade se aplica a todas as perícias de Força e Destreza (p. 152).")
            }
            if (deity != null && deity.forbidsMetalArmor && shield.metal) {
                issue("Devotos de ${deity.name} não podem usar escudos de metal como ${shield.name} (p. ${deity.page}).")
            }
        }

        val load = RuleEngine.currentLoad(character)
        val maxLoad = RuleEngine.maxLoad(character)
        if (load > maxLoad) issue("Carga de ${formatSlots(load)} espaços passa do máximo de $maxLoad (p. 141).")

        // ---------- Magias ----------
        val casting = cls.spellcasting
        if (casting == null) {
            if (character.classSpells.isNotEmpty()) issue("${cls.name} não lança magias, mas há magias de classe na ficha.")
        } else {
            if (casting.restrictsSchools && character.spellSchools.size != casting.schoolCount) {
                issue("${cls.name} escolhe ${casting.schoolCount} escolas de magia; foram escolhidas ${character.spellSchools.size} (p. ${cls.page}).")
            }
            val expected = casting.spellsKnown(character.level)
            if (character.classSpells.size != expected) {
                issue("${cls.name} de ${character.level}º nível conhece $expected magias de classe; há ${character.classSpells.size} (p. ${cls.page}).")
            }
            val maxCircle = casting.maxCircle(character.level)
            character.classSpells.forEach { spell ->
                if (!spell.type.castableBy(casting.type)) {
                    issue("${spell.name} é uma magia ${spell.type.displayName.lowercase()}; ${cls.name} lança magias ${casting.type.displayName.lowercase()}s.")
                }
                if (casting.restrictsSchools && spell.school !in character.spellSchools) {
                    issue("${spell.name} é de ${spell.school.displayName}, que não está entre as escolas escolhidas.")
                }
                if (spell.circle > maxCircle) {
                    issue("${spell.name} é de ${spell.circle}º círculo; no ${character.level}º nível, ${cls.name} lança até o ${maxCircle}º.")
                }
            }
        }

        return issues
    }

    private fun formatSlots(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')
}
