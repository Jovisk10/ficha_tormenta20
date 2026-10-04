package com.jovis.t20.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SheetCodecTest {

    @Test
    fun `a ficha da SEDAF volta identica depois de salva e lida`() {
        val texto = SheetCodec.encode(SampleSheets.SEDAF)
        assertEquals(SampleSheets.SEDAF, SheetCodec.decode(texto))
    }

    @Test
    fun `estado de jogo e textos especiais sobrevivem ao salvamento`() {
        val ficha = SampleSheets.SEDAF.copy(
            currentHp = -3,
            currentMp = 0,
            notes = "Aspas \"duplas\", barra \\ e\nquebra de linha. Ação, coração: ç ã é.",
            inventory = SampleSheets.SEDAF.inventory + SheetItem("Bálsamo restaurador", quantity = 2, slots = 0.5),
        )
        assertEquals(ficha, SheetCodec.decode(SheetCodec.encode(ficha)))
    }

    @Test
    fun `campos ausentes recebem valores padrao`() {
        val ficha = SheetCodec.decode("""{"name": "Nova", "attributes": {"FORCA": 3}}""")
        assertEquals("Nova", ficha.name)
        assertEquals(1, ficha.level)
        assertEquals(3, ficha.attributes[Attribute.FORCA])
        assertEquals(0, ficha.attributes[Attribute.CARISMA])
        assertEquals(9, ficha.baseSpeed)
        assertTrue(ficha.weapons.isEmpty())
    }

    @Test
    fun `nomes desconhecidos sao ignorados sem quebrar`() {
        val ficha = SheetCodec.decode("""{"name": "X", "trainedSkills": ["LUTA", "PERICIA_DO_FUTURO"], "campoNovo": 1}""")
        assertEquals(setOf(Skill.LUTA), ficha.trainedSkills)
    }

    @Test
    fun `nivel fora da faixa e corrigido`() {
        assertEquals(20, SheetCodec.decode("""{"name": "X", "level": 99}""").level)
    }

    @Test
    fun `JSON invalido gera erro claro`() {
        assertFailsWith<JsonParseException> { SheetCodec.decode("""{"name": "X" """) }
    }
}
