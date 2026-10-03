package com.jovis.t20.rules

/** Fonte: Tormenta 20 Jogo do Ano, Tabela 3-5, p. 153. */
data class ArmorDefinition(
    val name: String,
    val defenseBonus: Int,
    /** Penalidade de armadura (valor negativo ou zero). */
    val armorPenalty: Int,
    val slots: Int,
)

object Armors {
    val ARMADURA_DE_COURO = ArmorDefinition("Armadura de Couro", defenseBonus = 2, armorPenalty = 0, slots = 2)
}
