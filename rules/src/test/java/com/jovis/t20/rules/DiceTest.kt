package com.jovis.t20.rules

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Gerador que devolve uma sequência fixa de valores, para controlar os dados nos testes. */
private class FixedRandom(vararg values: Int) : Random() {
    private val queue = ArrayDeque(values.toList())
    override fun nextBits(bitCount: Int): Int = error("não usado")
    override fun nextInt(from: Int, until: Int): Int = queue.removeFirst().also { require(it in from until until) }
}

class DiceTest {

    @Test
    fun `le formulas de dados`() {
        assertEquals(DiceFormula(listOf(DiceTerm(1, 6)), 2), DiceParser.parse("1d6+2"))
        assertEquals(DiceFormula(listOf(DiceTerm(1, 20))), DiceParser.parse("d20"))
        assertEquals(DiceFormula(listOf(DiceTerm(1, 8), DiceTerm(1, 6)), -1), DiceParser.parse("1d8 + 1d6 - 1"))
        assertEquals(DiceFormula(listOf(DiceTerm(1, 10))), DiceParser.parse("1d10/1d12"))
        assertNull(DiceParser.parse("abc"))
        assertNull(DiceParser.parse("1d6+x"))
        assertNull(DiceParser.parse(""))
    }

    @Test
    fun `20 natural e 1 natural`() { // p. 221
        val roller = DiceRoller(FixedRandom(20, 1))
        val vinte = roller.test("Atuação", 7, RollKind.SKILL)
        assertTrue(vinte.isNatural20)
        assertEquals(27, vinte.total)
        val um = roller.test("Atuação", 7, RollKind.SKILL)
        assertTrue(um.isNatural1)
    }

    @Test
    fun `ameaca de critico usa a margem da arma`() { // p. 142
        val roller = DiceRoller(FixedRandom(19, 18))
        val ficha = SampleSheets.SEDAF.copy(weapons = listOf(SheetWeapon("Espada longa", "1d8", AttackMode.MELEE, criticalRange = 19)))
        assertTrue(SheetRolls.attack(roller, ficha, ficha.weapons[0]).isThreat)
        assertFalse(SheetRolls.attack(roller, ficha, ficha.weapons[0]).isThreat)
    }

    @Test
    fun `dano critico multiplica os dados mas nao o bonus`() { // p. 142
        val ficha = SampleSheets.SEDAF.copy(weapons = listOf(SheetWeapon("Machado", "1d8", AttackMode.MELEE, criticalMultiplier = 3)))
        val roller = DiceRoller(FixedRandom(5, 6, 7))
        val dano = assertNotNull(SheetRolls.damage(roller, ficha, ficha.weapons[0], critical = true))
        assertEquals("3d8+2", dano.formula.text) // Força 2 não é multiplicada
        assertEquals(5 + 6 + 7 + 2, dano.total)
        assertTrue(dano.critical)
    }

    @Test
    fun `critico multiplica so os dados da arma, nao os dados extras`() { // p. 142
        val formula = DiceParser.parse("1d8+1d6+3")!!
        assertEquals("2d8+1d6+3", formula.criticalOf(2).text)
        assertEquals("3d8+1d6+3", formula.criticalOf(3).text)
    }

    @Test
    fun `dano normal da lanca da SEDAF`() {
        val roller = DiceRoller(FixedRandom(4))
        val dano = assertNotNull(SheetRolls.damage(roller, SampleSheets.SEDAF, SampleSheets.SEDAF.weapons[0]))
        assertEquals(6, dano.total) // 4 + Força 2
        assertEquals("1d6+2: 4 + 2 = 6", dano.breakdown)
    }

    @Test
    fun `teste de pericia usa o valor da ficha`() {
        val teste = SheetRolls.skill(DiceRoller(FixedRandom(10)), SampleSheets.SEDAF, Skill.PERCEPCAO)
        assertEquals(18, teste.total) // 10 + Percepção 8
        assertEquals("d20 (10) + 8 = 18", teste.breakdown)
    }

    @Test
    fun `dados livres somam todos os dados`() {
        val rolagem = DiceRoller(FixedRandom(3, 4, 5)).roll("3d6", DiceParser.parse("3d6+1")!!, RollKind.FREE)
        assertEquals(13, rolagem.total)
    }

    @Test
    fun `dados aleatorios ficam sempre dentro da faixa`() {
        val roller = DiceRoller(Random(42))
        repeat(2000) { assertTrue(roller.die(20) in 1..20) }
    }
}
