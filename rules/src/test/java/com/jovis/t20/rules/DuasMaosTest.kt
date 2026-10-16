package com.jovis.t20.rules

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Gerador com resultados fixos, para controlar os dados. */
private class DadosFixos(vararg values: Int) : Random() {
    private val queue = ArrayDeque(values.toList())
    override fun nextBits(bitCount: Int): Int = error("não usado")
    override fun nextInt(from: Int, until: Int): Int = queue.removeFirst().also { require(it in from until until) }
}

/**
 * Armas adaptáveis (p. 143): com as duas mãos, o dano sobe um passo, que é o segundo valor da tabela
 * (p. 145). Espada bastarda (p. 147) e katana (p. 148) contam como marciais com as duas mãos.
 */
class DuasMaosTest {

    private fun arma(nome: String) = WeaponCatalog.ALL.first { it.name == nome }

    private fun fichaCom(nome: String): Pair<ManualSheet, SheetWeapon> {
        val ataque = arma(nome).toSheetWeapons().single()
        return SampleSheets.SEDAF.copy(weapons = listOf(ataque)) to ataque
    }

    @Test
    fun `espada bastarda usa 1d10 com uma mao e 1d12 com duas - p 143 e 147`() {
        val (ficha, espada) = fichaCom("Espada bastarda")
        assertTrue(espada.canUseTwoHanded)
        assertEquals("1d10+2", SheetRolls.damageFormula(ficha, espada)!!.text) // Força 2
        assertEquals("1d12+2", SheetRolls.damageFormula(ficha, espada, twoHanded = true)!!.text)
        assertEquals("1d10+2", SheetEngine.damageText(ficha, espada, twoHanded = false))
        assertEquals("1d12+2", SheetEngine.damageText(ficha, espada, twoHanded = true))

        val dano = assertNotNull(SheetRolls.damage(DiceRoller(DadosFixos(11)), ficha, espada, twoHanded = true))
        assertEquals(13, dano.total) // 11 no d12 + Força 2
        assertEquals("Dano: Espada bastarda (duas mãos)", dano.label)
    }

    @Test
    fun `katana usa 1d8 com uma mao e 1d10 com duas - p 148`() {
        val (ficha, katana) = fichaCom("Katana")
        assertTrue(katana.canUseTwoHanded)
        assertEquals("1d8+2", SheetRolls.damageFormula(ficha, katana)!!.text)
        assertEquals("1d10+2", SheetRolls.damageFormula(ficha, katana, twoHanded = true)!!.text)
    }

    @Test
    fun `critico com duas maos multiplica so o dado da arma - p 142`() {
        val (ficha, espada) = fichaCom("Espada bastarda")
        val dano = assertNotNull(SheetRolls.damage(DiceRoller(DadosFixos(12, 7)), ficha, espada, critical = true, twoHanded = true))
        assertEquals("2d12+2", dano.formula.text) // Força não é multiplicada
        assertEquals(12 + 7 + 2, dano.total)
        assertEquals("Dano crítico: Espada bastarda (duas mãos)", dano.label)
    }

    @Test
    fun `ataque com duas maos so muda o rotulo na ficha manual`() {
        val (ficha, espada) = fichaCom("Espada bastarda")
        val umaMao = SheetRolls.attack(DiceRoller(DadosFixos(10)), ficha, espada)
        val duasMaos = SheetRolls.attack(DiceRoller(DadosFixos(10)), ficha, espada, twoHanded = true)
        assertEquals(umaMao.total, duasMaos.total)
        assertEquals("Ataque: Espada bastarda (duas mãos)", duasMaos.label)
        assertEquals(19, duasMaos.threatFrom)
    }

    @Test
    fun `com duas maos a espada bastarda, a katana e o machado anao sao marciais - p 147 a 149`() {
        for (nome in listOf("Espada bastarda", "Katana", "Machado anão")) {
            assertEquals(WeaponProficiency.EXOTIC, arma(nome).proficiencyFor(twoHanded = false), nome)
            assertEquals(WeaponProficiency.MARTIAL, arma(nome).proficiencyFor(twoHanded = true), nome)
        }
        assertEquals(WeaponProficiency.EXOTIC, arma("Corrente de espinhos").proficiencyFor(twoHanded = true))
        // O machado anão não é adaptável: o dano não muda (p. 149)
        assertFalse(arma("Machado anão").toSheetWeapons().single().canUseTwoHanded)
    }

    @Test
    fun `sem proficiencia exotica, a bastarda com duas maos perde o -5 se a classe usa marciais - p 142 e 147`() {
        val bastarda = WeaponDefinition(
            name = "Espada bastarda", proficiency = WeaponProficiency.EXOTIC, purpose = WeaponPurpose.MELEE,
            damageDice = "1d10/1d12", damageType = "Corte", criticalRange = 19, martialWhenTwoHanded = true,
        )
        val marcial = Fixtures.SEDAF.copy(
            characterClass = Classes.DRUIDA.copy(weaponProficiencies = setOf(WeaponProficiency.MARTIAL)),
        )
        val umaMao = RuleEngine.attack(marcial, bastarda)
        val duasMaos = RuleEngine.attack(marcial, bastarda, twoHanded = true)
        assertEquals(umaMao.attack.total + 5, duasMaos.attack.total)
        assertEquals("1d10", umaMao.damageDice)
        assertEquals("1d12", duasMaos.damageDice)
        // A SEDAF (druida, só armas simples) sofre –5 das duas formas
        assertEquals(
            RuleEngine.attack(Fixtures.SEDAF, bastarda).attack.total,
            RuleEngine.attack(Fixtures.SEDAF, bastarda, twoHanded = true).attack.total,
        )
    }

    @Test
    fun `arma dupla nao tem dano de duas maos - p 143`() {
        val (ficha, corrente) = fichaCom("Corrente de espinhos")
        assertFalse(corrente.canUseTwoHanded)
        assertEquals("2d4+2", SheetRolls.damageFormula(ficha, corrente)!!.text)
        assertEquals("2d4+2", SheetRolls.damageFormula(ficha, corrente, twoHanded = true)!!.text)
        val dano = assertNotNull(SheetRolls.damage(DiceRoller(DadosFixos(3, 4)), ficha, corrente, twoHanded = true))
        assertEquals("Dano: Corrente de espinhos", dano.label)
        assertFalse(arma("Bordão").toSheetWeapons().single().canUseTwoHanded)
        // Armas comuns também não oferecem a escolha
        assertFalse(SampleSheets.SEDAF.weapons[0].canUseTwoHanded)
    }
}
