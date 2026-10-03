package com.jovis.t20.rules

/**
 * Compra de atributos por pontos.
 * Fonte: Tormenta 20 Jogo do Ano, p. 17.
 */
object PointBuy {
    const val BUDGET = 10

    private val costs = mapOf(-1 to -1, 0 to 0, 1 to 1, 2 to 2, 3 to 4, 4 to 7)

    fun cost(value: Int): Int = costs[value]
        ?: throw IllegalArgumentException("Valor $value não pode ser comprado com pontos (permitido: -1 a 4).")

    fun totalCost(base: Map<Attribute, Int>): Int = base.values.sumOf { cost(it) }

    fun remaining(base: Map<Attribute, Int>): Int = BUDGET - totalCost(base)
}
