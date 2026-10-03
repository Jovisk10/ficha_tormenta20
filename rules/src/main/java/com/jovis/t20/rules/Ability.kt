package com.jovis.t20.rules

/**
 * Habilidades recebidas automaticamente de raça ou classe.
 * Por enquanto só registram a existência; efeitos entram no RuleEngine quando necessário.
 */
enum class Ability(val displayName: String) {
    // Dahllan (p. 21)
    AMIGA_DAS_PLANTAS("Amiga das Plantas"),
    ARMADURA_DE_ALLIHANNA("Armadura de Allihanna"),
    EMPATIA_SELVAGEM("Empatia Selvagem"),

    // Druida (p. 61-63)
    DEVOTO_FIEL("Devoto Fiel"),
    MAGIAS("Magias"),
    CAMINHO_DOS_ERMOS("Caminho dos Ermos"),
}
