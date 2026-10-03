package com.jovis.t20.rules

import com.jovis.t20.rules.Attribute.*

/**
 * Perícias. Fonte: Tormenta 20 Jogo do Ano, Tabela 2-1, p. 115.
 * trainedOnly = "Somente Treinada"; armorPenalty = sofre penalidade de armadura.
 */
enum class Skill(
    val displayName: String,
    val keyAttribute: Attribute,
    val trainedOnly: Boolean = false,
    val armorPenalty: Boolean = false,
) {
    ACROBACIA("Acrobacia", DESTREZA, armorPenalty = true),
    ADESTRAMENTO("Adestramento", CARISMA, trainedOnly = true),
    ATLETISMO("Atletismo", FORCA),
    ATUACAO("Atuação", CARISMA, trainedOnly = true),
    CAVALGAR("Cavalgar", DESTREZA),
    CONHECIMENTO("Conhecimento", INTELIGENCIA, trainedOnly = true),
    CURA("Cura", SABEDORIA),
    DIPLOMACIA("Diplomacia", CARISMA),
    ENGANACAO("Enganação", CARISMA),
    FORTITUDE("Fortitude", CONSTITUICAO),
    FURTIVIDADE("Furtividade", DESTREZA, armorPenalty = true),
    GUERRA("Guerra", INTELIGENCIA, trainedOnly = true),
    INICIATIVA("Iniciativa", DESTREZA),
    INTIMIDACAO("Intimidação", CARISMA),
    INTUICAO("Intuição", SABEDORIA),
    INVESTIGACAO("Investigação", INTELIGENCIA),
    JOGATINA("Jogatina", CARISMA, trainedOnly = true),
    LADINAGEM("Ladinagem", DESTREZA, trainedOnly = true, armorPenalty = true),
    LUTA("Luta", FORCA),
    MISTICISMO("Misticismo", INTELIGENCIA, trainedOnly = true),
    NOBREZA("Nobreza", INTELIGENCIA, trainedOnly = true),
    OFICIO("Ofício", INTELIGENCIA, trainedOnly = true),
    PERCEPCAO("Percepção", SABEDORIA),
    PILOTAGEM("Pilotagem", DESTREZA, trainedOnly = true),
    PONTARIA("Pontaria", DESTREZA),
    REFLEXOS("Reflexos", DESTREZA),
    RELIGIAO("Religião", SABEDORIA, trainedOnly = true),
    SOBREVIVENCIA("Sobrevivência", SABEDORIA),
    VONTADE("Vontade", SABEDORIA),
}
