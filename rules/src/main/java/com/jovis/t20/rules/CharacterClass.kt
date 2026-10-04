package com.jovis.t20.rules

import com.jovis.t20.rules.Skill.*

/** Os dados fixos de uma classe, como estão no livro. */
data class ClassDefinition(
    val name: String,
    val page: Int,
    val hpFirstLevel: Int,
    val hpPerLevel: Int,
    val mpPerLevel: Int,
    /** Atributo somado ao total de PM, se a classe tiver (ex.: Druida soma Sabedoria). */
    val mpAttribute: Attribute?,
    val fixedSkills: Set<Skill>,
    val skillChoiceCount: Int,
    val skillChoiceList: Set<Skill>,
    /** Habilidades automáticas por nível de classe. */
    val abilitiesByLevel: Map<Int, List<Ability>>,
    /** Proficiências além das que todos têm (armas simples e armaduras leves, p. 32 e 142). */
    val weaponProficiencies: Set<WeaponProficiency> = emptySet(),
    val armorProficiencies: Set<ArmorCategory> = emptySet(),
    val shieldProficiency: Boolean = false,
    val spellcasting: Spellcasting? = null,
    /** Divindades permitidas, se a classe exige devoção (null = não exige). */
    val allowedDeities: Set<String>? = null,
    /** Nível em que a classe ganha o primeiro poder de classe; depois, um por nível. */
    val firstClassPowerLevel: Int? = null,
    /** Quantos poderes concedidos a classe recebe pela devoção (Druida: 2, por Devoto Fiel). */
    val grantedPowerCount: Int = 0,
) {
    fun classPowersAt(level: Int): Int = firstClassPowerLevel?.let { maxOf(0, level - it + 1) } ?: 0
}

object Classes {
    /** Fonte: Tormenta 20 Jogo do Ano, p. 61-63. */
    val DRUIDA = ClassDefinition(
        name = "Druida",
        page = 61,
        hpFirstLevel = 16,
        hpPerLevel = 4,
        mpPerLevel = 4,
        mpAttribute = Attribute.SABEDORIA,
        fixedSkills = setOf(SOBREVIVENCIA, VONTADE),
        skillChoiceCount = 4,
        skillChoiceList = setOf(
            ADESTRAMENTO, ATLETISMO, CAVALGAR, CONHECIMENTO, CURA, FORTITUDE, INICIATIVA,
            INTUICAO, LUTA, MISTICISMO, OFICIO, PERCEPCAO, RELIGIAO,
        ),
        abilitiesByLevel = mapOf(
            1 to listOf(Ability.DEVOTO_FIEL, Ability.EMPATIA_SELVAGEM, Ability.MAGIAS),
            2 to listOf(Ability.CAMINHO_DOS_ERMOS),
        ),
        shieldProficiency = true,
        spellcasting = Spellcasting(
            attribute = Attribute.SABEDORIA,
            type = SpellType.DIVINE,
            schoolCount = 3,
            initialSpells = 2,
            spellsPerEvenLevel = 1,
            circleUnlockLevel = mapOf(1 to 1, 2 to 6, 3 to 10, 4 to 14),
        ),
        allowedDeities = setOf("Allihanna", "Megalokk", "Oceano"),
        firstClassPowerLevel = 2,
        grantedPowerCount = 2,
    )
}
