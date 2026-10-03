package com.jovis.t20.rules

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

    // ---------- Defesa (p. 106) ----------

    /** 10 + Destreza + bônus de armadura e escudo. TODO: armaduras pesadas não somam Destreza. */
    fun defense(character: Character): DerivedValue {
        val parts = mutableListOf(
            Contribution("Base", 10, Source(SourceType.BASE, "Regra de Defesa")),
            Contribution("Destreza", attrValue(character, Attribute.DESTREZA), Source(SourceType.BASE, "Destreza")),
        )
        character.armor?.let {
            parts += Contribution(it.name, it.defenseBonus, Source(SourceType.ITEM, it.name))
        }
        return build(parts, modifiersFor(character, StatTarget.Defense))
    }

    // ---------- Perícias (p. 114-115) ----------

    fun trainedSkills(character: Character): Set<Skill> =
        character.characterClass.fixedSkills +
            character.classSkillChoices +
            character.origin.skills +
            character.intelligenceSkills

    fun isTrained(character: Character, skill: Skill): Boolean = skill in trainedSkills(character)

    /** Perícias "somente treinadas" não podem ser usadas sem treino, mesmo tendo valor calculado. */
    fun canUse(character: Character, skill: Skill): Boolean = !skill.trainedOnly || isTrained(character, skill)

    /** Metade do nível + atributo-chave + bônus de treinamento (se treinado). */
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
        val armor = character.armor
        if (skill.armorPenalty && armor != null && armor.armorPenalty != 0) {
            parts += Contribution("Penalidade de armadura", armor.armorPenalty, Source(SourceType.ITEM, armor.name))
        }
        return build(parts, modifiersFor(character, StatTarget.SkillTarget(skill)))
    }

    fun allSkills(character: Character): Map<Skill, DerivedValue> =
        Skill.entries.associateWith { skill(character, it) }

    // ---------- Magia (p. 227) ----------

    /** 10 + metade do nível + atributo-chave de magia. Null se a classe não lança magias. */
    fun spellDc(character: Character): DerivedValue? {
        val attribute = character.characterClass.spellAttribute ?: return null
        val parts = listOf(
            Contribution("Base", 10, Source(SourceType.BASE, "Regra de CD")),
            Contribution("Metade do nível", halfLevel(character), Source(SourceType.LEVEL, "Nível ${character.level}")),
            Contribution(attribute.displayName, attrValue(character, attribute), Source(SourceType.BASE, attribute.displayName)),
        )
        return build(parts, modifiersFor(character, StatTarget.SpellDc))
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
