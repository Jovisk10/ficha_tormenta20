package com.jovis.t20.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Regras isoladas, testadas com variações da SEDAF. */
class RegrasTest {

    private val sedaf = Fixtures.SEDAF

    @Test
    fun `arma marcial sem proficiencia sofre -5 no ataque`() {
        val ataque = RuleEngine.attack(sedaf, Weapons.ESPADA_LONGA)
        println(ataque.attack.explain("Ataque com espada longa"))
        assertEquals(0, ataque.attack.total) // Luta 5 – 5
        assertEquals("19/x2", ataque.critical)
    }

    @Test
    fun `lanca nao pode ser usada como arma de disparo`() {
        assertFailsWith<IllegalArgumentException> { RuleEngine.attack(sedaf, Weapons.LANCA, AttackMode.FIRING) }
    }

    @Test
    fun `armadura pesada nao soma Destreza e gera avisos`() {
        val pesada = sedaf.copy(
            armor = Armors.BRUNEA,
            inventory = sedaf.inventory.filter { it.item != Armors.ARMADURA_DE_COURO } + InventoryEntry(Armors.BRUNEA),
        )
        val defesa = RuleEngine.defense(pesada)
        println(defesa.explain("Defesa com brunea"))
        assertEquals(15, defesa.total) // 10 + 0 + 5

        // Sem proficiência, a penalidade vale para todas as perícias de Força e Destreza
        assertEquals(3, RuleEngine.skill(pesada, Skill.LUTA).total) // 5 – 2

        val avisos = Validator.validate(pesada).map { it.message }
        assertTrue(avisos.any { it.contains("não é proficiente em Brunea") })
        assertTrue(avisos.any { it.contains("Devotos de Allihanna não podem usar armaduras de metal") })
    }

    @Test
    fun `sobrecarga aplica -5 nas pericias com penalidade de armadura`() {
        val carregada = sedaf.copy(inventory = sedaf.inventory + InventoryEntry(Items.CORDA, quantity = 9)) // 15 espaços
        assertTrue(RuleEngine.isOverloaded(carregada))
        assertEquals(-3, RuleEngine.skill(carregada, Skill.ACROBACIA).total) // 1 + 1 – 5
        assertEquals(5, RuleEngine.skill(carregada, Skill.LUTA).total) // Luta não sofre penalidade de armadura
    }

    @Test
    fun `carga acima do dobro do limite gera aviso`() {
        val demais = sedaf.copy(inventory = sedaf.inventory + InventoryEntry(Items.CORDA, quantity = 23)) // 29 espaços
        assertTrue(Validator.validate(demais).any { it.message.startsWith("Carga de 29 espaços") })
    }

    @Test
    fun `Forca negativa reduz o limite de carga em 1 por ponto`() {
        val fraca = sedaf.copy(baseAttributes = sedaf.baseAttributes + (Attribute.FORCA to -2))
        assertEquals(8, RuleEngine.loadLimit(fraca).total)
    }

    @Test
    fun `magia fora das escolas escolhidas gera aviso`() {
        val errada = sedaf.copy(classSpells = setOf(Spells.COMPREENSAO, Spells.CURAR_FERIMENTOS, Spells.CONTROLAR_PLANTAS))
        assertTrue(Validator.validate(errada).any { it.message.startsWith("Controlar Plantas é de Transmutação") })
    }

    @Test
    fun `Amiga das Plantas reduz o custo mas nunca abaixo de 1 PM`() {
        val transmutadora = sedaf.copy(
            spellSchools = setOf(SpellSchool.ADIVINHACAO, SpellSchool.EVOCACAO, SpellSchool.TRANSMUTACAO),
            classSpells = setOf(Spells.COMPREENSAO, Spells.CURAR_FERIMENTOS, Spells.CONTROLAR_PLANTAS),
        )
        val custo = RuleEngine.spellCost(transmutadora, Spells.CONTROLAR_PLANTAS)
        println(custo.explain("Custo de Controlar Plantas"))
        assertEquals(1, custo.total)
        assertTrue(custo.contributions.any { it.value == -1 })
        assertTrue(Validator.validate(transmutadora).none { it.message.contains("Controlar Plantas") })
    }

    @Test
    fun `druida sem divindade permitida gera aviso`() {
        assertTrue(Validator.validate(sedaf.copy(deity = null)).any { it.message.startsWith("Druida precisa ser devoto") })
    }
}
