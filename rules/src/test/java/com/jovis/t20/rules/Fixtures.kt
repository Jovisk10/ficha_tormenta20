package com.jovis.t20.rules

import com.jovis.t20.rules.Attribute.*
import com.jovis.t20.rules.Skill.*

/**
 * SEDAF (jogadora Sabrina): Dahllan, origem Selvagem, Druida nível 3, devota de Allihanna.
 * Poderes de druida: Forma Selvagem (2º nível) e Companheiro Animal (3º nível).
 * Usada como caso de validação e como base para testes de regras (via copy()).
 */
object Fixtures {
    val SEDAF = Character(
        name = "SEDAF",
        level = 3,
        attributeMethod = AttributeMethod.ROLLED, // custaria 12 pontos na compra, então foi rolagem
        baseAttributes = mapOf(
            FORCA to 2,
            DESTREZA to 0,
            CONSTITUICAO to 3,
            INTELIGENCIA to 0,
            SABEDORIA to 3,
            CARISMA to 2,
        ),
        race = Dahllan,
        characterClass = Classes.DRUIDA,
        classSkillChoices = setOf(ADESTRAMENTO, ATLETISMO, LUTA, PERCEPCAO, RELIGIAO),
        origin = OriginSelection(Origins.SELVAGEM, skills = setOf(REFLEXOS), powers = setOf(Vitalidade)),
        deity = Deities.ALLIHANNA,
        classPowers = listOf(DruidPowers.FormaSelvagem, DruidPowers.CompanheiroAnimal),
        // Ainda não sabemos: os 2 poderes concedidos de Allihanna e o tipo do companheiro animal.
        inventory = listOf(
            InventoryEntry(Armors.ARMADURA_DE_COURO),
            InventoryEntry(Items.CORDA),
            InventoryEntry(Items.SACO_DE_DORMIR),
            InventoryEntry(Items.MALETA_DE_MEDICAMENTOS),
            InventoryEntry(Weapons.LANCA),
        ),
        armor = Armors.ARMADURA_DE_COURO,
        spellSchools = setOf(SpellSchool.ADIVINHACAO, SpellSchool.EVOCACAO, SpellSchool.ENCANTAMENTO),
        classSpells = setOf(Spells.COMPREENSAO, Spells.CURAR_FERIMENTOS, Spells.BENCAO),
    )
}
