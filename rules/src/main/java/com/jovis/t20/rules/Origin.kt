package com.jovis.t20.rules

import com.jovis.t20.rules.Skill.*

/** Os benefícios que uma origem oferece. O jogador escolhe dois (p. 85). */
data class OriginDefinition(
    val name: String,
    val page: Int,
    val skillOptions: Set<Skill>,
    val powerOptions: Set<GeneralPower>,
)

/** O que o jogador escolheu da origem. */
data class OriginSelection(
    val origin: OriginDefinition,
    val skills: Set<Skill> = emptySet(),
    val powers: Set<GeneralPower> = emptySet(),
)

object Origins {
    /** Fonte: Tormenta 20 Jogo do Ano, p. 94. */
    val SELVAGEM = OriginDefinition(
        name = "Selvagem",
        page = 94,
        skillOptions = setOf(PERCEPCAO, REFLEXOS, SOBREVIVENCIA),
        powerOptions = setOf(LoboSolitario, VidaRustica, Vitalidade),
    )
}
