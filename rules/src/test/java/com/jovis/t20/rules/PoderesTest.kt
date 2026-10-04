package com.jovis.t20.rules

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Escudos, forma selvagem, companheiros e poderes, testados com variações da SEDAF. */
class PoderesTest {

    private val sedaf = Fixtures.SEDAF

    private fun comEscudo(escudo: ShieldDefinition) =
        sedaf.copy(shield = escudo, inventory = sedaf.inventory + InventoryEntry(escudo))

    private fun emForma(forma: WildShapeForm, base: Character = sedaf, opcao: SwiftOption? = null) =
        base.copy(activeWildShape = ActiveWildShape(forma, swiftOption = opcao))

    // ---------- Escudos ----------

    @Test
    fun `escudo leve soma na Defesa e penaliza pericias de armadura`() {
        val comLeve = comEscudo(Shields.ESCUDO_LEVE)
        assertEquals(14, RuleEngine.defense(comLeve).total)
        assertEquals(1, RuleEngine.skill(comLeve, Skill.ACROBACIA).total) // 2 – 1
        assertEquals(5, RuleEngine.skill(comLeve, Skill.LUTA).total) // druida é proficiente em escudos
        assertTrue(Validator.validate(comLeve).none { it.message.contains("scudo") })
    }

    @Test
    fun `escudo pesado e de metal e proibido para devotos de Allihanna`() {
        val avisos = Validator.validate(comEscudo(Shields.ESCUDO_PESADO)).map { it.message }
        assertTrue(avisos.any { it.startsWith("Devotos de Allihanna não podem usar escudos de metal") })
    }

    // ---------- Forma Selvagem ----------

    @Test
    fun `Forma Feroz muda Forca, Defesa e ataques`() {
        val feroz = emForma(WildShapeForm.FEROZ)
        assertEquals(5, RuleEngine.attribute(feroz, Attribute.FORCA).total)
        println(RuleEngine.defense(feroz).explain("Defesa em Forma Feroz"))
        assertEquals(15, RuleEngine.defense(feroz).total) // a armadura vestida continua valendo
        val ataques = RuleEngine.allAttacks(feroz)
        assertEquals(1, ataques.size) // a lança some; fica só a arma natural
        assertEquals(8, ataques.single().attack.total) // Luta: 1 + 5 + 2
        assertEquals("1d8+5", ataques.single().damageText)
    }

    @Test
    fun `em forma selvagem o escudo empunhado nao conta`() {
        val ferozComEscudo = emForma(WildShapeForm.FEROZ, base = comEscudo(Shields.ESCUDO_LEVE))
        assertEquals(15, RuleEngine.defense(ferozComEscudo).total)
    }

    @Test
    fun `Forma Resistente da Defesa e reducao de dano`() {
        val resistente = emForma(WildShapeForm.RESISTENTE)
        assertEquals(18, RuleEngine.defense(resistente).total)
        assertEquals(5, RuleEngine.damageReduction(resistente).total)
        assertEquals(0, RuleEngine.damageReduction(sedaf).total)
    }

    @Test
    fun `Forma Agil com Presas Afiadas amplia a margem de ameaca`() {
        val agil = emForma(WildShapeForm.AGIL)
        assertEquals("19/x2", RuleEngine.allAttacks(agil).single().critical)
        val comPresas = agil.copy(classPowers = agil.classPowers + DruidPowers.PresasAfiadas)
        assertEquals("17/x2", RuleEngine.allAttacks(comPresas).single().critical)
    }

    @Test
    fun `Forma Veloz pode trocar o deslocamento`() {
        assertEquals(15, RuleEngine.speed(emForma(WildShapeForm.VELOZ, opcao = SwiftOption.DESLOCAMENTO_15)).total)
        assertEquals(9, RuleEngine.speed(emForma(WildShapeForm.VELOZ, opcao = SwiftOption.NATACAO_9)).total)
    }

    @Test
    fun `Forma Sorrateira melhora Furtividade`() {
        assertEquals(6, RuleEngine.skill(emForma(WildShapeForm.SORRATEIRA), Skill.FURTIVIDADE).total) // 1 + 3 + 2
    }

    @Test
    fun `magias em forma selvagem so com Magia Natural`() {
        val forma = emForma(WildShapeForm.AGIL)
        assertFalse(RuleEngine.canCastSpells(forma))
        assertTrue(RuleEngine.canCastSpells(forma.copy(classPowers = forma.classPowers + DruidPowers.MagiaNatural)))
    }

    // ---------- Companheiro animal ----------

    @Test
    fun `companheiro guardiao da Defesa conforme o nivel`() {
        val comGuardiao = sedaf.copy(companions = listOf(AnimalCompanion("Urso", PartnerType.GUARDIAO)))
        println(RuleEngine.defense(comGuardiao).explain("Defesa com guardião"))
        assertEquals(15, RuleEngine.defense(comGuardiao).total) // iniciante: +2
        assertTrue(Validator.validate(comGuardiao).none { it.message.contains("Companheiro Animal") })
        assertEquals(PartnerTier.VETERANO, RuleEngine.companionTier(comGuardiao.copy(level = 7)))
    }

    @Test
    fun `ajudante nao pode dar bonus em Luta`() {
        val ajudante = sedaf.copy(companions = listOf(AnimalCompanion("Corvo", PartnerType.AJUDANTE, setOf(Skill.LUTA, Skill.PERCEPCAO))))
        assertTrue(Validator.validate(ajudante).any { it.message.contains("não pode dar bônus em Luta ou Pontaria") })
    }

    @Test
    fun `Companheiro Animal exige Carisma 1`() {
        val semCarisma = sedaf.copy(baseAttributes = sedaf.baseAttributes + (Attribute.CARISMA to 0))
        assertTrue(Validator.validate(semCarisma).any { it.message.startsWith("Companheiro Animal exige Carisma 1") })
    }

    // ---------- Poderes concedidos e de classe ----------

    @Test
    fun `Compreender os Ermos da Sobrevivencia e Sabedoria em Adestramento`() {
        val comErmos = sedaf.copy(grantedPowers = setOf(AllihannaPowers.CompreenderOsErmos))
        assertEquals(10, RuleEngine.skill(comErmos, Skill.SOBREVIVENCIA).total)
        val adestramento = RuleEngine.skill(comErmos, Skill.ADESTRAMENTO)
        println(adestramento.explain("Adestramento com Compreender os Ermos"))
        assertEquals(10, adestramento.total) // 1 + Sab 5 + 2 + 2
        assertTrue(adestramento.contributions.any { it.label == "Sabedoria (no lugar de Carisma)" })
    }

    @Test
    fun `Dedo Verde com Amiga das Plantas aprende Controlar Plantas de novo`() {
        val comDedoVerde = sedaf.copy(grantedPowers = setOf(AllihannaPowers.DedoVerde, AllihannaPowers.DescansoNatural))
        val controlar = RuleEngine.knownSpells(comDedoVerde).single { it.spell == Spells.CONTROLAR_PLANTAS }
        assertEquals(2, controlar.sources.size)
        assertEquals(1, controlar.cost.total) // –1 PM, mas nunca menos de 1
        assertTrue(Validator.validate(comDedoVerde).none { it.message.contains("poderes concedidos") })
    }

    @Test
    fun `Voz da Natureza ensina Acalmar Animal`() {
        val comVoz = sedaf.copy(grantedPowers = setOf(AllihannaPowers.VozDaNatureza))
        assertTrue(RuleEngine.knownSpells(comVoz).any { it.spell == Spells.ACALMAR_ANIMAL })
        assertTrue(RuleEngine.notes(comVoz).any { it.startsWith("Voz da Natureza:") })
    }

    @Test
    fun `Forca dos Penhascos exige 4o nivel`() {
        val cedo = sedaf.copy(classPowers = listOf(DruidPowers.FormaSelvagem, DruidPowers.ForcaDosPenhascos))
        assertTrue(Validator.validate(cedo).any { it.message.startsWith("Força dos Penhascos exige 4º nível de druida") })
        assertEquals(8, RuleEngine.skill(cedo, Skill.FORTITUDE).total) // o bônus aparece mesmo assim, com o aviso
    }

    @Test
    fun `poder nao repetivel escolhido duas vezes gera aviso`() {
        val repetido = sedaf.copy(classPowers = listOf(DruidPowers.FormaSelvagem, DruidPowers.FormaSelvagem))
        assertTrue(Validator.validate(repetido).any { it.message == "Forma Selvagem não pode ser escolhido mais de uma vez." })
    }
}
