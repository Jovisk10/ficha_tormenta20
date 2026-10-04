package com.jovis.t20.rules

import com.jovis.t20.rules.Skill.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** A ficha manual da SEDAF precisa dar os mesmos números que o motor automático. */
class SedafSheetTest {

    private val sheet = SampleSheets.SEDAF
    private val auto = Fixtures.SEDAF

    @Test
    fun `PV, PM e Defesa`() {
        assertEquals(36, SheetEngine.maxHp(sheet).total)
        assertEquals(17, SheetEngine.maxMp(sheet).total)
        assertEquals(13, SheetEngine.defense(sheet).total)
        assertEquals(1, SheetEngine.defense(sheet).situational.size)
        assertEquals(36, SheetEngine.currentHp(sheet))
    }

    @Test
    fun `pericias iguais as do motor automatico`() {
        assertEquals(
            RuleEngine.allSkills(auto).mapValues { it.value.total },
            SheetEngine.allSkills(sheet).mapValues { it.value.total },
        )
    }

    @Test
    fun `ataques, carga, deslocamento e magias`() {
        val lanca = sheet.weapons.first()
        assertEquals(5, SheetEngine.attackBonus(sheet, lanca).total)
        assertEquals("1d6+2", SheetEngine.damageText(sheet, lanca))
        assertEquals(2, SheetEngine.attackBonus(sheet, sheet.weapons[1]).total)
        assertEquals(14, SheetEngine.loadLimit(sheet).total)
        assertEquals(6.0, SheetEngine.currentLoad(sheet))
        assertEquals(9, SheetEngine.speed(sheet).total)
        assertEquals(16, assertNotNull(SheetEngine.spellDc(sheet)).total)
        assertTrue(sheet.spells.all { SheetEngine.spellCost(it) == 1 })
    }

    @Test
    fun `bonus digitado aparece com nome na explicacao`() {
        val fortitude = SheetEngine.skill(sheet, FORTITUDE)
        println(fortitude.explain("Fortitude"))
        assertTrue(fortitude.contributions.any { it.label == "Vitalidade" && it.value == 2 })
    }
}
