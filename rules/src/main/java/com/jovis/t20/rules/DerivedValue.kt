package com.jovis.t20.rules

/** Uma parcela de um valor calculado, com a origem dela. */
data class Contribution(val label: String, val value: Int, val source: Source)

/**
 * Todo valor calculado pelo motor é um DerivedValue: o total MAIS a explicação.
 * É isso que o agente de IA vai ler para responder "por que minha Defesa é 13?".
 */
data class DerivedValue(
    val contributions: List<Contribution>,
    val situational: List<Modifier> = emptyList(),
) {
    val total: Int get() = contributions.sumOf { it.value }

    fun explain(name: String): String = buildString {
        appendLine("$name = $total")
        contributions.forEach { appendLine("  ${it.label}: ${signed(it.value)}") }
        situational.forEach {
            appendLine("  (situacional) ${it.source.name}: ${signed(it.value)}, ${it.situation}")
        }
    }
}

internal fun signed(value: Int): String = if (value >= 0) "+$value" else "$value"
