package com.jovis.t20.rules

/** A raça escolhida pelo jogador, com as escolhas que ela exige. */
sealed interface RaceSelection {
    val raceName: String
    val abilities: List<Ability>
    val grantedSpells: List<GrantedSpell> get() = emptyList()
    fun modifiers(): List<Modifier>
}

private fun attributeMods(source: Source, vararg values: Pair<Attribute, Int>): List<Modifier> =
    values.map { (attribute, value) -> Modifier(StatTarget.AttributeTarget(attribute), value, source) }

/**
 * Dahllan: Sabedoria +2, Destreza +1, Inteligência –1.
 * Fonte: Tormenta 20 Jogo do Ano, p. 21.
 */
data object Dahllan : RaceSelection {
    override val raceName = "Dahllan"
    override val abilities = listOf(
        Ability.AMIGA_DAS_PLANTAS,
        Ability.ARMADURA_DE_ALLIHANNA,
        Ability.EMPATIA_SELVAGEM,
    )

    /** Amiga das Plantas: Controlar Plantas (Sab); se aprender de novo, custo –1 PM. */
    override val grantedSpells = listOf(
        GrantedSpell(
            spell = Spells.CONTROLAR_PLANTAS,
            sourceName = "$raceName: ${Ability.AMIGA_DAS_PLANTAS.displayName}",
            sourceType = SourceType.RACE,
            relearnDiscount = 1,
            attribute = Attribute.SABEDORIA,
        ),
    )

    override fun modifiers(): List<Modifier> {
        val source = Source(SourceType.RACE, raceName)
        return attributeMods(
            source,
            Attribute.SABEDORIA to +2,
            Attribute.DESTREZA to +1,
            Attribute.INTELIGENCIA to -1,
        ) + Modifier(
            // Armadura de Allihanna: ação de movimento + 1 PM, +2 na Defesa até o fim da cena (p. 21)
            target = StatTarget.Defense,
            value = +2,
            source = Source(SourceType.ABILITY, Ability.ARMADURA_DE_ALLIHANNA.displayName),
            situation = "quando ativada (ação de movimento + 1 PM, até o fim da cena)",
        )
    }
}

/**
 * Lefou: +1 em três atributos diferentes (exceto Carisma) e Carisma –1.
 * Fonte: Tormenta 20 Jogo do Ano, capítulo de Raças.
 */
data class Lefou(val chosenAttributes: Set<Attribute>) : RaceSelection {
    override val raceName = "Lefou"
    override val abilities = emptyList<Ability>()

    init {
        require(chosenAttributes.size == 3) {
            "Lefou escolhe exatamente 3 atributos (recebido: ${chosenAttributes.size})."
        }
        require(Attribute.CARISMA !in chosenAttributes) {
            "Lefou não pode receber o +1 racial em Carisma."
        }
    }

    override fun modifiers(): List<Modifier> {
        val source = Source(SourceType.RACE, raceName)
        return chosenAttributes.map { Modifier(StatTarget.AttributeTarget(it), +1, source) } +
            Modifier(StatTarget.AttributeTarget(Attribute.CARISMA), -1, source)
    }
}
