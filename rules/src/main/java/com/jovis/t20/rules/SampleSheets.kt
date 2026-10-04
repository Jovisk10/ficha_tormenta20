package com.jovis.t20.rules

import com.jovis.t20.rules.Attribute.*
import com.jovis.t20.rules.Skill.*

/** Fichas de exemplo, preenchidas como um jogador preencheria. */
object SampleSheets {

    /** SEDAF (jogadora Sabrina), com as correções conferidas no livro (PV 36, armadura com 2 espaços). */
    val SEDAF = ManualSheet(
        name = "SEDAF",
        player = "Sabrina",
        race = "Dahllan",
        origin = "Selvagem",
        className = "Druida",
        level = 3,
        deity = "Allihanna",
        raceId = "dahllan",
        classId = "druida",
        originId = "selvagem",
        attributes = mapOf(FORCA to 2, DESTREZA to 1, CONSTITUICAO to 3, INTELIGENCIA to -1, SABEDORIA to 5, CARISMA to 2),
        hpFirstLevel = 16,
        hpPerLevel = 4,
        hpBonuses = listOf(SheetBonus("Vitalidade", 1, perLevel = true)),
        mpPerLevel = 4,
        mpAttribute = SABEDORIA,
        trainedSkills = setOf(ADESTRAMENTO, ATLETISMO, LUTA, PERCEPCAO, REFLEXOS, RELIGIAO, SOBREVIVENCIA, VONTADE),
        skillBonuses = mapOf(
            FORTITUDE to listOf(SheetBonus("Vitalidade", 2)),
            ADESTRAMENTO to listOf(SheetBonus("Empatia Selvagem (recebida novamente)", 2)),
        ),
        armor = SheetArmor("Armadura de couro", defenseBonus = 2),
        defenseBonuses = listOf(
            SheetBonus("Armadura de Allihanna", 2, situation = "quando ativada (ação de movimento + 1 PM, até o fim da cena)"),
        ),
        weapons = listOf(
            SheetWeapon("Lança", "1d6", AttackMode.MELEE),
            SheetWeapon("Lança (arremessada)", "1d6", AttackMode.THROWN, notes = "Alcance curto"),
        ),
        inventory = listOf(
            SheetItem("Armadura de couro", slots = 2.0, equipped = true),
            SheetItem("Corda"),
            SheetItem("Saco de dormir"),
            SheetItem("Maleta de medicamentos"),
            SheetItem("Lança"),
        ),
        money = 225,
        spellAttribute = SABEDORIA,
        spellSchools = setOf(SpellSchool.ADIVINHACAO, SpellSchool.EVOCACAO, SpellSchool.ENCANTAMENTO),
        spells = listOfNotNull(
            SpellCatalog.find("Controlar Plantas")?.toSheetSpell("Pela raça (Amiga das Plantas)"),
            SpellCatalog.find("Compreensão")?.toSheetSpell(),
            SpellCatalog.find("Curar Ferimentos")?.toSheetSpell(),
            SpellCatalog.find("Bênção")?.toSheetSpell(),
        ),
        abilities = listOf(
            SheetEntry("Empatia Selvagem", "Comunica-se com animais e usa Adestramento para mudar a atitude deles."),
            SheetEntry("Caminho dos Ermos", "Atravessa terreno difícil natural sem perder deslocamento; rastreá-la fica +10 mais difícil."),
            SheetEntry("Forma Selvagem", "Ação completa e 3 PM para assumir uma forma animal."),
            SheetEntry("Companheiro Animal", "Um parceiro animal; o bônus depende do tipo."),
            SheetEntry("Amiga das Plantas", "Pode lançar Controlar Plantas."),
            SheetEntry("Armadura de Allihanna", "Ação de movimento e 1 PM: +2 na Defesa até o fim da cena."),
            SheetEntry("Vitalidade", "+1 PV por nível e +2 em Fortitude."),
        ),
        notes = "Escolas de magia: Adivinhação, Evocação e Encantamento.",
    )
}
