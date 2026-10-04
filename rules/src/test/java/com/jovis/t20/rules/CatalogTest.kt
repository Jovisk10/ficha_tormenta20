package com.jovis.t20.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CatalogTest {

    @Test
    fun `catalogo tem as magias de 1o circulo e nenhuma sem resumo`() {
        assertEquals(53, SpellCatalog.ALL.size)
        assertTrue(SpellCatalog.ALL.all { it.summary.isNotBlank() && it.page in 170..215 })
        assertEquals(SpellCatalog.ALL.size, SpellCatalog.ALL.map { it.name }.toSet().size)
    }

    @Test
    fun `bardo so ve magias arcanas ou universais das escolas escolhidas`() {
        val bardo = CatalogApply.applyClass(ManualSheet(name = "Peter", level = 3), ClassCatalog.BARDO)
            .copy(spellSchools = setOf(SpellSchool.ENCANTAMENTO, SpellSchool.ILUSAO, SpellSchool.ADIVINHACAO))
        val magias = CatalogApply.availableSpells(bardo)
        assertTrue(magias.isNotEmpty())
        assertTrue(magias.none { it.type == SpellType.DIVINE })
        assertTrue(magias.all { it.school in bardo.spellSchools })
        assertTrue(magias.any { it.name == "Enfeitiçar" })
        assertTrue(magias.any { it.name == "Compreensão" }) // universal
        assertTrue(magias.none { it.name == "Bênção" }) // divina
        assertEquals(3, CatalogApply.classSpellsKnown(bardo))
    }

    @Test
    fun `sem escolas escolhidas a lista fica vazia e guerreiro nao tem magias`() {
        val semEscolas = CatalogApply.applyClass(ManualSheet(name = "X"), ClassCatalog.BARDO)
        assertTrue(CatalogApply.availableSpells(semEscolas).isEmpty())
        val guerreira = CatalogApply.applyClass(ManualSheet(name = "Y"), ClassCatalog.GUERREIRO)
        assertTrue(CatalogApply.availableSpells(guerreira).isEmpty())
        assertEquals(null, CatalogApply.classSpellsKnown(guerreira))
    }

    @Test
    fun `aplicar classe e raca preenche a ficha sem duplicar`() {
        var ficha = ManualSheet(name = "Elfa", level = 3, attributes = Attribute.entries.associateWith { 1 })
        ficha = CatalogApply.applyClass(ficha, ClassCatalog.BARDO)
        ficha = CatalogApply.applyRace(ficha, RaceCatalog.ELFO)
        ficha = CatalogApply.applyRace(ficha, RaceCatalog.ELFO) // de novo: não duplica
        assertEquals(12 + 1 + 2 * (3 + 1), SheetEngine.maxHp(ficha).total)
        assertEquals(4 * 3 + 1 + 3, SheetEngine.maxMp(ficha).total) // 4/nível + Carisma + Sangue Mágico (1 × 3)
        assertEquals(12, SheetEngine.speed(ficha).total)
        assertEquals(1, ficha.skillBonuses[Skill.PERCEPCAO]!!.size)
        assertTrue(Skill.ATUACAO in ficha.trainedSkills && Skill.REFLEXOS in ficha.trainedSkills)
    }

    @Test
    fun `armas do catalogo viram ataques corretos`() {
        val lanca = assertNotNull(WeaponCatalog.ALL.firstOrNull { it.name == "Lança" })
        val ataques = lanca.toSheetWeapons()
        assertEquals(listOf(AttackMode.MELEE, AttackMode.THROWN), ataques.map { it.mode })
        val ficha = SampleSheets.SEDAF.copy(weapons = ataques)
        assertEquals("1d6+2", SheetEngine.damageText(ficha, ataques[0]))
        val arco = WeaponCatalog.ALL.first { it.name == "Arco curto" }.toSheetWeapons().single()
        assertEquals("1d6", SheetEngine.damageText(ficha, arco)) // disparo não soma Força
        assertEquals(3, arco.criticalMultiplier)
    }

    @Test
    fun `itens tem espacos validos`() {
        assertTrue(ItemCatalog.ALL.size > 100)
        assertEquals(0.5, ItemCatalog.ALL.first { it.name == "Bálsamo restaurador" }.slots)
        assertEquals(1.0, ItemCatalog.ALL.first { it.name == "Corda" }.slots)
    }

    @Test
    fun `bonus por nivel acompanha o nivel`() {
        val nivel5 = SampleSheets.SEDAF.copy(level = 5)
        assertTrue(SheetEngine.maxHp(nivel5).contributions.any { it.label == "Vitalidade (+1 × 5)" && it.value == 5 })
    }
}

class ArcanistaTest {

    private fun arcanista(path: String, level: Int = 3): ManualSheet {
        val ficha = CatalogApply.applyClass(ManualSheet(name = "Arcanista", level = level), ClassCatalog.ARCANISTA)
        return CatalogApply.applyPath(ficha, ClassCatalog.ARCANISTA.pathById(path)!!)
    }

    @Test
    fun `sem caminho o arcanista ainda nao tem regras de magia`() {
        val semCaminho = CatalogApply.applyClass(ManualSheet(name = "X"), ClassCatalog.ARCANISTA)
        assertEquals(null, CatalogApply.casting(semCaminho))
        assertTrue(CatalogApply.availableSpells(semCaminho).isEmpty())
    }

    @Test
    fun `arcanista ve magias arcanas de todas as escolas sem escolher escolas`() {
        val magias = CatalogApply.availableSpells(arcanista("bruxo"))
        assertTrue(magias.map { it.school }.toSet().size >= 6)
        assertTrue(magias.none { it.type == SpellType.DIVINE })
        assertTrue(magias.any { it.name == "Seta Infalível de Talude" })
    }

    @Test
    fun `quantidade de magias conhecidas por caminho`() {
        // Bruxo: 3 + 1 por nível
        assertEquals(5, CatalogApply.classSpellsKnown(arcanista("bruxo", 3)))
        // Feiticeiro: 3 + 1 nos níveis ímpares a partir do 3º
        assertEquals(4, CatalogApply.classSpellsKnown(arcanista("feiticeiro", 3)))
        assertEquals(3, CatalogApply.classSpellsKnown(arcanista("feiticeiro", 2)))
        // Mago: 4 + 1 por nível + 1 por círculo novo (2º círculo no 5º nível)
        assertEquals(6, CatalogApply.classSpellsKnown(arcanista("mago", 3)))
        assertEquals(9, CatalogApply.classSpellsKnown(arcanista("mago", 5)))
    }

    @Test
    fun `caminho define o atributo dos PM e das magias`() {
        val feiticeiro = arcanista("feiticeiro").copy(attributes = Attribute.entries.associateWith { 0 } + (Attribute.CARISMA to 4))
        assertEquals(Attribute.CARISMA, feiticeiro.mpAttribute)
        assertEquals(6 * 3 + 4, SheetEngine.maxMp(feiticeiro).total)
        assertEquals(Attribute.INTELIGENCIA, arcanista("mago").spellAttribute)
    }

    @Test
    fun `qareen e assistente de laboratorio estao no catalogo`() {
        val qareen = CatalogApply.applyRace(ManualSheet(name = "Q"), RaceCatalog.QAREEN)
        assertTrue(qareen.abilities.any { it.name == "Tatuagem Mística" })
        assertEquals("Assistente de Laboratório", OriginCatalog.byId("assistente")?.name)
    }
}
