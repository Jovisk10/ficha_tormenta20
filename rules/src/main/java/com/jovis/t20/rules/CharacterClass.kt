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
    /** Atributo-chave para lançar magias, se a classe lançar. */
    val spellAttribute: Attribute?,
    val fixedSkills: Set<Skill>,
    val skillChoiceCount: Int,
    val skillChoiceList: Set<Skill>,
    /** Habilidades automáticas por nível de classe. */
    val abilitiesByLevel: Map<Int, List<Ability>>,
)

object Classes {
    /** Fonte: Tormenta 20 Jogo do Ano, p. 61-63. */
    val DRUIDA = ClassDefinition(
        name = "Druida",
        page = 61,
        hpFirstLevel = 16,
        hpPerLevel = 4,
        mpPerLevel = 4,
        mpAttribute = Attribute.SABEDORIA,
        spellAttribute = Attribute.SABEDORIA,
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
    )
}
