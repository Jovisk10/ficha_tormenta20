package com.jovis.t20.rules

/** Qualquer coisa que ocupa espaço no inventário. */
sealed interface Item {
    val name: String
    /** Espaços ocupados (alguns itens ocupam 0,5). */
    val slots: Double
}

data class GeneralItem(override val name: String, override val slots: Double) : Item

data class InventoryEntry(val item: Item, val quantity: Int = 1) {
    init { require(quantity >= 1) { "Quantidade deve ser pelo menos 1." } }
    val totalSlots: Double get() = item.slots * quantity
}

/** Fonte: Tormenta 20 Jogo do Ano, Tabela 3-6, p. 156. */
object Items {
    val CORDA = GeneralItem("Corda", 1.0)
    val SACO_DE_DORMIR = GeneralItem("Saco de dormir", 1.0)
    val MALETA_DE_MEDICAMENTOS = GeneralItem("Maleta de medicamentos", 1.0)
    val BALSAMO_RESTAURADOR = GeneralItem("Bálsamo restaurador", 0.5)
}
