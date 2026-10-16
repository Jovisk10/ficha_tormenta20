package com.jovis.t20.rules

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private class DadoFixo(private val value: Int) : Random() {
    override fun nextBits(bitCount: Int): Int = error("não usado")
    override fun nextInt(from: Int, until: Int): Int = value
}

/** O teste de ataque guarda de qual arma veio, para o dano seguir a mesma arma e empunhadura (p. 142-143). */
class ArmaDoAtaqueTest {

    private val roller get() = DiceRoller(DadoFixo(10))

    @Test
    fun `duas armas com o mesmo nome`() {
        val adaga = SheetWeapon("Adaga", "1d4", AttackMode.MELEE, criticalRange = 19)
        val adagaBoa = SheetWeapon("Adaga", "1d4+1", AttackMode.MELEE, criticalRange = 19)
        val ficha = SampleSheets.SEDAF.copy(weapons = listOf(adaga, adagaBoa))

        val ataque = SheetRolls.attack(roller, ficha, ficha.weapons[1])
        assertEquals(1, ataque.weaponIndex)
        assertSame(ficha.weapons[1], SheetRolls.weaponOf(ficha, ataque))

        // Duas armas idênticas: o índice explícito decide
        val iguais = SampleSheets.SEDAF.copy(weapons = listOf(adaga, adaga.copy()))
        val segunda = SheetRolls.attack(roller, iguais, iguais.weapons[1], weaponIndex = 1)
        assertEquals(1, segunda.weaponIndex)
        // Sem índice, a identidade da instância distingue cópias iguais
        assertEquals(1, SheetRolls.attack(roller, iguais, iguais.weapons[1]).weaponIndex)
    }

    @Test
    fun `arma renomeada continua sendo achada`() {
        val ficha = SampleSheets.SEDAF.copy(weapons = listOf(SheetWeapon("Espada longa", "1d8", AttackMode.MELEE, criticalRange = 19)))
        val ataque = SheetRolls.attack(roller, ficha, ficha.weapons[0])
        val renomeada = ficha.copy(weapons = listOf(ficha.weapons[0].copy(name = "Lâmina do avô")))
        assertEquals("Lâmina do avô", SheetRolls.weaponOf(renomeada, ataque)?.name)
        // Arma apagada: o índice deixa de valer
        assertNull(SheetRolls.weaponOf(ficha.copy(weapons = emptyList()), ataque))
    }

    @Test
    fun `ataque com duas maos guarda twoHanded - p 143`() {
        val bastarda = WeaponCatalog.ALL.first { it.name == "Espada bastarda" }.toSheetWeapons().single()
        val ficha = SampleSheets.SEDAF.copy(weapons = listOf(bastarda))
        val duasMaos = SheetRolls.attack(roller, ficha, ficha.weapons[0], twoHanded = true)
        assertTrue(duasMaos.twoHanded)
        assertEquals("Ataque: Espada bastarda (duas mãos)", duasMaos.label)
        assertEquals(0, duasMaos.weaponIndex)
        assertFalse(SheetRolls.attack(roller, ficha, ficha.weapons[0]).twoHanded)
        // Arma que não é adaptável não guarda a empunhadura
        val comum = SampleSheets.SEDAF
        assertFalse(SheetRolls.attack(roller, comum, comum.weapons[0], twoHanded = true).twoHanded)
    }

    @Test
    fun `rolagem que nao e ataque nao tem arma`() {
        val ficha = SampleSheets.SEDAF
        assertNull(SheetRolls.weaponOf(ficha, SheetRolls.skill(roller, ficha, Skill.LUTA)))
        assertNull(ficha.weapons.firstOrNull()?.let { SheetRolls.weaponOf(ficha, TestRoll(1, "Ataque: ${it.name}", RollKind.ATTACK, 10, 0)) })
    }
}
