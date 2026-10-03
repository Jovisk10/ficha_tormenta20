package com.jovis.t20.rules

enum class ArmorCategory { LIGHT, HEAVY }

/** Fonte: Tormenta 20 Jogo do Ano, p. 152 e Tabela 3-5, p. 153. */
data class ArmorDefinition(
    override val name: String,
    val category: ArmorCategory,
    val defenseBonus: Int,
    /** Penalidade de armadura (valor negativo ou zero). */
    val armorPenalty: Int,
    override val slots: Double,
    /** Importa para restrições como a de Allihanna (p. 97). */
    val metal: Boolean,
) : Item

object Armors {
    val ARMADURA_ACOLCHOADA = ArmorDefinition("Armadura acolchoada", ArmorCategory.LIGHT, 1, 0, 2.0, metal = false)
    val ARMADURA_DE_COURO = ArmorDefinition("Armadura de couro", ArmorCategory.LIGHT, 2, 0, 2.0, metal = false)
    val COURO_BATIDO = ArmorDefinition("Couro batido", ArmorCategory.LIGHT, 3, -1, 2.0, metal = true)
    val GIBAO_DE_PELES = ArmorDefinition("Gibão de peles", ArmorCategory.LIGHT, 4, -3, 2.0, metal = false)
    val COURACA = ArmorDefinition("Couraça", ArmorCategory.LIGHT, 5, -4, 2.0, metal = true)
    val BRUNEA = ArmorDefinition("Brunea", ArmorCategory.HEAVY, 5, -2, 5.0, metal = true)
    val COTA_DE_MALHA = ArmorDefinition("Cota de malha", ArmorCategory.HEAVY, 6, -2, 5.0, metal = true)
    val LORIGA_SEGMENTADA = ArmorDefinition("Loriga segmentada", ArmorCategory.HEAVY, 7, -3, 5.0, metal = true)
    val MEIA_ARMADURA = ArmorDefinition("Meia armadura", ArmorCategory.HEAVY, 8, -4, 5.0, metal = true)
    val ARMADURA_COMPLETA = ArmorDefinition("Armadura completa", ArmorCategory.HEAVY, 10, -5, 5.0, metal = true)
    // TODO: escudos (bônus na Defesa, penalidade e proficiência) na próxima rodada.
}
