package com.jovis.t20.rules

import com.jovis.t20.rules.Attribute.*
import com.jovis.t20.rules.Skill.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Caso de validação: a ficha da SEDAF, conferida com o livro.
 * Divergências da ficha original, confirmadas como esquecimento:
 *  - PV: a ficha mostrava 33, mas Vitalidade dá +1 PV por nível, então o correto é 36.
 *  - Carga: a ficha conta a Armadura de Couro como 1 espaço; pelo livro são 2 (total 6, não 5).
 *  - Perícias: a ficha tem 5 escolhas da lista de Druida, mas a classe permite 4.
 *    Enquanto não sabemos qual sai, o teste confirma que o Validator aponta o excesso.
 */
class SedafTest {

    private val sedaf = Fixtures.SEDAF

    @Test
    fun `atributos finais`() {
        val finais = RuleEngine.allAttributes(sedaf).mapValues { it.value.total }
        assertEquals(
            mapOf(FORCA to 2, DESTREZA to 1, CONSTITUICAO to 3, INTELIGENCIA to -1, SABEDORIA to 5, CARISMA to 2),
            finais,
        )
    }

    @Test
    fun `PV maximo e 36 com Vitalidade`() {
        val pv = RuleEngine.maxHp(sedaf)
        println(pv.explain("PV"))
        assertEquals(36, pv.total)
    }

    @Test
    fun `PM maximo`() {
        assertEquals(17, RuleEngine.maxMp(sedaf).total)
    }

    @Test
    fun `Defesa com armadura de couro e Armadura de Allihanna situacional`() {
        val defesa = RuleEngine.defense(sedaf)
        assertEquals(13, defesa.total)
        assertEquals(2, defesa.situational.single().value)
    }

    @Test
    fun `todas as pericias batem com a ficha`() {
        val esperado = mapOf(
            ACROBACIA to 2, ADESTRAMENTO to 7, ATLETISMO to 5, ATUACAO to 3, CAVALGAR to 2,
            CONHECIMENTO to 0, CURA to 6, DIPLOMACIA to 3, ENGANACAO to 3, FORTITUDE to 6,
            FURTIVIDADE to 2, GUERRA to 0, INICIATIVA to 2, INTIMIDACAO to 3, INTUICAO to 6,
            INVESTIGACAO to 0, JOGATINA to 3, LADINAGEM to 2, LUTA to 5, MISTICISMO to 0,
            NOBREZA to 0, OFICIO to 0, PERCEPCAO to 8, PILOTAGEM to 2, PONTARIA to 2,
            REFLEXOS to 4, RELIGIAO to 8, SOBREVIVENCIA to 8, VONTADE to 8,
        )
        assertEquals(esperado, RuleEngine.allSkills(sedaf).mapValues { it.value.total })
    }

    @Test
    fun `Adestramento recebe +2 por Empatia Selvagem repetida`() {
        val adestramento = RuleEngine.skill(sedaf, ADESTRAMENTO)
        assertTrue(adestramento.contributions.any { it.label == "Empatia Selvagem (recebida novamente)" && it.value == 2 })
    }

    @Test
    fun `pericias somente treinadas nao podem ser usadas sem treino`() {
        assertFalse(RuleEngine.canUse(sedaf, JOGATINA))
        assertTrue(RuleEngine.canUse(sedaf, RELIGIAO))
    }

    @Test
    fun `lanca corpo a corpo`() {
        val ataque = RuleEngine.attack(sedaf, Weapons.LANCA, AttackMode.MELEE)
        println("Lança (corpo a corpo): ataque +${ataque.attack.total}, dano ${ataque.damageText}, crítico ${ataque.critical}")
        assertEquals(5, ataque.attack.total)
        assertEquals("1d6+2", ataque.damageText)
        assertEquals("20/x2", ataque.critical)
    }

    @Test
    fun `lanca arremessada usa Pontaria e soma Forca no dano`() {
        val ataque = RuleEngine.attack(sedaf, Weapons.LANCA, AttackMode.THROWN)
        assertEquals(2, ataque.attack.total)
        assertEquals("1d6+2", ataque.damageText)
    }

    @Test
    fun `lista de ataques tem a lanca nos dois modos`() {
        assertEquals(listOf(AttackMode.MELEE, AttackMode.THROWN), RuleEngine.allAttacks(sedaf).map { it.mode })
    }

    @Test
    fun `carga`() {
        assertEquals(14, RuleEngine.loadLimit(sedaf).total)
        assertEquals(28, RuleEngine.maxLoad(sedaf))
        assertEquals(6.0, RuleEngine.currentLoad(sedaf))
        assertFalse(RuleEngine.isOverloaded(sedaf))
    }

    @Test
    fun `CD de magia`() {
        assertEquals(16, assertNotNull(RuleEngine.spellDc(sedaf)).total)
    }

    @Test
    fun `magias conhecidas e custos`() {
        val magias = RuleEngine.knownSpells(sedaf)
        magias.forEach { println("${it.spell.name}: ${it.cost.total} PM (${it.sources.joinToString { s -> s.name }})") }
        assertEquals(
            setOf("Controlar Plantas", "Compreensão", "Curar Ferimentos", "Bênção"),
            magias.map { it.spell.name }.toSet(),
        )
        assertTrue(magias.all { it.cost.total == 1 })
        assertEquals(SourceType.RACE, magias.first { it.spell == Spells.CONTROLAR_PLANTAS }.sources.single().type)
    }

    @Test
    fun `validador aponta apenas a pericia de classe excedente`() {
        val issues = Validator.validate(sedaf)
        issues.forEach { println("Aviso: ${it.message}") }
        assertEquals(1, issues.size)
        assertTrue(issues.single().message.startsWith("Druida escolhe 4 perícias"))
    }
}
