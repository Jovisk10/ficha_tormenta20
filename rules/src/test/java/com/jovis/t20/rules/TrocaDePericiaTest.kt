package com.jovis.t20.rules

import com.jovis.t20.rules.Skill.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Troca de escolha nos grupos de uma vaga. É controle interno do app, não regra do livro;
 * o grupo usado é Luta ou Pontaria do guerreiro (p. 65).
 */
class TrocaDePericiaTest {

    private fun guerreiro() = CatalogApply.applyClass(
        ManualSheet(name = "Teste", level = 1, attributeMode = AttributeMethod.POINT_BUY),
        ClassCatalog.GUERREIRO,
    )

    private fun grupo(ficha: ManualSheet, id: SkillGroupId) = CatalogApply.skillGroups(ficha).first { it.id == id }

    @Test
    fun `marcar outra opcao no grupo de uma vaga troca a escolha - p 65`() {
        var ficha = CatalogApply.toggleSkill(guerreiro(), SkillGroupId.CLASS_FIXED_CHOICE, LUTA, on = true)
        assertEquals(setOf(LUTA), grupo(ficha, SkillGroupId.CLASS_FIXED_CHOICE).selected)
        assertTrue(LUTA in ficha.trainedSkills)

        ficha = CatalogApply.toggleSkill(ficha, SkillGroupId.CLASS_FIXED_CHOICE, PONTARIA, on = true)
        assertEquals(setOf(PONTARIA), grupo(ficha, SkillGroupId.CLASS_FIXED_CHOICE).selected)
        assertTrue(PONTARIA in ficha.trainedSkills)
        assertTrue(LUTA !in ficha.trainedSkills)
    }

    @Test
    fun `desmarcar libera a vaga`() {
        var ficha = CatalogApply.toggleSkill(guerreiro(), SkillGroupId.CLASS_FIXED_CHOICE, LUTA, on = true)
        ficha = CatalogApply.toggleSkill(ficha, SkillGroupId.CLASS_FIXED_CHOICE, LUTA, on = false)
        assertTrue(grupo(ficha, SkillGroupId.CLASS_FIXED_CHOICE).selected.isEmpty())
        assertNull(ficha.skillPicks!!.classFixedChoice)
        assertTrue(LUTA !in ficha.trainedSkills)
    }

    @Test
    fun `blockedReason nao bloqueia a troca, mas mantem os outros bloqueios`() {
        val ficha = CatalogApply.toggleSkill(guerreiro(), SkillGroupId.CLASS_FIXED_CHOICE, LUTA, on = true)
        assertNull(CatalogApply.blockedReason(ficha, SkillGroupId.CLASS_FIXED_CHOICE, PONTARIA))
        assertEquals("Não está na lista deste grupo.", CatalogApply.blockedReason(ficha, SkillGroupId.CLASS_FIXED_CHOICE, ATLETISMO))
        assertEquals("Automática.", CatalogApply.blockedReason(ficha, SkillGroupId.CLASS_FIXED, ATLETISMO))
        // Luta já está na escolha fixa: não pode ser marcada de novo nas perícias de classe
        assertNotNull(CatalogApply.blockedReason(ficha, SkillGroupId.CLASS, LUTA))
    }

    @Test
    fun `grupo de varias vagas cheio continua bloqueando`() {
        var ficha = guerreiro()
        ficha = CatalogApply.toggleSkill(ficha, SkillGroupId.CLASS, ATLETISMO, on = true)
        ficha = CatalogApply.toggleSkill(ficha, SkillGroupId.CLASS, INICIATIVA, on = true)
        assertEquals(setOf(ATLETISMO, INICIATIVA), grupo(ficha, SkillGroupId.CLASS).selected)
        assertEquals("Limite deste grupo atingido.", CatalogApply.blockedReason(ficha, SkillGroupId.CLASS, PERCEPCAO))
        // Marcar além do limite não troca nem acrescenta
        ficha = CatalogApply.toggleSkill(ficha, SkillGroupId.CLASS, PERCEPCAO, on = true)
        assertEquals(2, grupo(ficha, SkillGroupId.CLASS).selected.size)
        assertTrue(PERCEPCAO !in ficha.trainedSkills)
    }
}
