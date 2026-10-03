package com.jovis.t20.rules

import com.jovis.t20.rules.Attribute.*
import com.jovis.t20.rules.Skill.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Caso de validação: SEDAF (jogadora Sabrina), Dahllan, origem Selvagem, Druida nível 3.
 * Valores esperados conferidos com a ficha e com o livro.
 * Divergências da ficha original, confirmadas como esquecimento:
 *  - PV: a ficha mostrava 33, mas Vitalidade dá +1 PV por nível, então o correto é 36.
 *  - Perícias: a ficha tem 5 escolhas da lista de Druida, mas a classe permite 4.
 *    Enquanto não sabemos qual sai, o teste confirma que o Validator aponta o excesso.
 */
class SedafTest {

    private val sedaf = Character(
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
        armor = Armors.ARMADURA_DE_COURO,
    )

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
        val pm = RuleEngine.maxMp(sedaf)
        println(pm.explain("PM"))
        assertEquals(17, pm.total)
    }

    @Test
    fun `Defesa com armadura de couro e Armadura de Allihanna situacional`() {
        val defesa = RuleEngine.defense(sedaf)
        println(defesa.explain("Defesa"))
        assertEquals(13, defesa.total)
        assertEquals(1, defesa.situational.size)
        assertEquals(2, defesa.situational.single().value)
    }

    @Test
    fun `CD de magia`() {
        val cd = assertNotNull(RuleEngine.spellDc(sedaf))
        assertEquals(16, cd.total)
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
        println(adestramento.explain("Adestramento"))
        assertTrue(adestramento.contributions.any { it.label == "Empatia Selvagem (recebida novamente)" && it.value == 2 })
    }

    @Test
    fun `pericias somente treinadas nao podem ser usadas sem treino`() {
        assertFalse(RuleEngine.canUse(sedaf, JOGATINA))
        assertTrue(RuleEngine.canUse(sedaf, RELIGIAO))
        assertTrue(RuleEngine.canUse(sedaf, CURA))
    }

    @Test
    fun `validador aponta apenas a pericia de classe excedente`() {
        val issues = Validator.validate(sedaf)
        issues.forEach { println("Aviso: ${it.message}") }
        assertEquals(1, issues.size)
        assertTrue(issues.single().message.startsWith("Druida escolhe 4 perícias"))
    }
}
