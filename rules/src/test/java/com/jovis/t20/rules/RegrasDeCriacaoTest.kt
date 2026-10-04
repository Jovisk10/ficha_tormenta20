package com.jovis.t20.rules

import com.jovis.t20.rules.Attribute.*
import com.jovis.t20.rules.Skill.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegrasDeCriacaoTest {

    private fun nova() = ManualSheet(name = "Teste", level = 3, attributeMode = AttributeMethod.POINT_BUY)

    // ---------- Atributos ----------

    @Test
    fun `lefou ja comeca com Carisma -1`() {
        val lefou = CatalogApply.applyRace(nova(), RaceCatalog.LEFOU)
        assertEquals(listOf(0, 0, 0, 0, 0, -1), Attribute.entries.map { lefou.attributes[it] })
    }

    @Test
    fun `escolhas de +1 respeitam limite e exclusao`() {
        val lefou = CatalogApply.refresh(
            CatalogApply.applyRace(nova(), RaceCatalog.LEFOU).copy(raceAttributeChoices = setOf(CARISMA, FORCA, DESTREZA, CONSTITUICAO, SABEDORIA)),
        )
        // Carisma não pode receber o +1 do lefou; só valem três escolhas
        assertEquals(-1, lefou.attributes[CARISMA])
        assertEquals(3, Attribute.entries.count { (lefou.attributes[it] ?: 0) == 1 })
    }

    @Test
    fun `compra de pontos soma base e raca`() {
        val dahllan = CatalogApply.applyRace(nova(), RaceCatalog.DAHLLAN).copy(
            baseAttributes = mapOf(FORCA to 2, DESTREZA to 0, CONSTITUICAO to 3, INTELIGENCIA to 0, SABEDORIA to 3, CARISMA to 1),
        )
        val ficha = CatalogApply.refresh(dahllan)
        assertEquals(5, ficha.attributes[SABEDORIA])
        assertEquals(-1, ficha.attributes[INTELIGENCIA])
        assertEquals(11, CatalogApply.pointBuyCost(ficha))
    }

    // ---------- Perícias ----------

    @Test
    fun `pericias da classe so aceitam a lista e respeitam o limite`() {
        var bardo = CatalogApply.applyClass(nova(), ClassCatalog.BARDO)
        assertTrue(ATUACAO in bardo.trainedSkills && REFLEXOS in bardo.trainedSkills)
        assertNotNull(CatalogApply.blockedReason(bardo, SkillGroupId.CLASS, RELIGIAO)) // fora da lista do bardo
        assertNull(CatalogApply.blockedReason(bardo, SkillGroupId.CLASS, DIPLOMACIA))
        bardo = CatalogApply.setSkillGroup(bardo, SkillGroupId.CLASS, setOf(ACROBACIA, CAVALGAR, CONHECIMENTO, DIPLOMACIA, ENGANACAO, FURTIVIDADE))
        assertEquals("Limite deste grupo atingido.", CatalogApply.blockedReason(bardo, SkillGroupId.CLASS, LUTA))
        assertNotNull(CatalogApply.blockedReason(bardo, SkillGroupId.OTHER, DIPLOMACIA)) // já treinada pela classe
    }

    @Test
    fun `origem divide os dois beneficios entre pericias e poderes`() {
        var ficha = CatalogApply.applyOrigin(CatalogApply.applyClass(nova(), ClassCatalog.BARDO), OriginCatalog.ARTISTA)
        ficha = CatalogApply.setOriginPower(ficha, "Dom Artístico", true)
        assertEquals(1, CatalogApply.skillGroups(ficha).first { it.id == SkillGroupId.ORIGIN }.limit)
        assertTrue(ficha.skillBonuses[ATUACAO]!!.any { it.label == "Dom Artístico" })
        ficha = CatalogApply.setSkillGroup(ficha, SkillGroupId.ORIGIN, setOf(ENGANACAO))
        assertEquals(0, CatalogApply.originPowerLimit(ficha) - ficha.originPowers.size)
    }

    @Test
    fun `inteligencia define quantas pericias extras`() {
        val ficha = CatalogApply.refresh(
            CatalogApply.applyClass(nova().copy(baseAttributes = Attribute.entries.associateWith { 0 } + (INTELIGENCIA to 2)), ClassCatalog.GUERREIRO),
        )
        assertEquals(2, CatalogApply.skillGroups(ficha).first { it.id == SkillGroupId.INTELLIGENCE }.limit)
    }

    @Test
    fun `deformidade do lefou da +2 sem treinar`() {
        var lefou = CatalogApply.applyClass(CatalogApply.applyRace(nova(), RaceCatalog.LEFOU), ClassCatalog.BARDO)
        lefou = CatalogApply.setSkillGroup(lefou, SkillGroupId.RACE_BONUS, setOf(FURTIVIDADE, INTIMIDACAO))
        assertTrue(lefou.skillBonuses[FURTIVIDADE]!!.any { it.label == "Deformidade" && it.value == 2 })
        assertTrue(FURTIVIDADE !in lefou.trainedSkills)
    }

    // ---------- Divindades e poderes ----------

    @Test
    fun `devocao depende de raca ou classe`() {
        val allihanna = DeityCatalog.byId("allihanna")!!
        assertTrue(DeityCatalog.canBeDevote(allihanna, "lefou", "druida"))
        assertTrue(!DeityCatalog.canBeDevote(allihanna, "lefou", "bardo"))
        assertTrue(DeityCatalog.canBeDevote(allihanna, "humano", "bardo")) // humanos seguem qualquer deus
        assertTrue(DeityCatalog.canBeDevote(DeityCatalog.byId("valkaria")!!, "lefou", "bardo"))
        assertEquals(20, DeityCatalog.ALL.size)
        assertTrue(DeityCatalog.ALL.all { d -> d.powers.all { PowerCatalog.granted(it) != null } })
    }

    @Test
    fun `poder concedido aplica e remove seus efeitos`() {
        var ficha = CatalogApply.setDeity(CatalogApply.applyClass(nova(), ClassCatalog.BARDO), DeityCatalog.byId("marah"))
        assertEquals(1, CatalogApply.grantedPowerLimit(ficha))
        ficha = CatalogApply.setGrantedPower(ficha, "Talento Artístico", true)
        assertTrue(ficha.skillBonuses[DIPLOMACIA]!!.any { it.label == "Talento Artístico" })
        assertTrue(ficha.abilities.any { it.name == "Talento Artístico" })
        ficha = CatalogApply.setGrantedPower(ficha, "Talento Artístico", false)
        assertTrue(ficha.skillBonuses[DIPLOMACIA].orEmpty().none { it.label == "Talento Artístico" })
        assertTrue(ficha.abilities.none { it.name == "Talento Artístico" })
    }

    @Test
    fun `poder que ensina magia adiciona e remove a magia`() {
        var ficha = CatalogApply.setDeity(CatalogApply.applyClass(nova(), ClassCatalog.BARDO), DeityCatalog.byId("hyninn"))
        ficha = CatalogApply.setGrantedPower(ficha, "Farsa do Fingidor", true)
        assertTrue(ficha.spells.any { it.name == "Criar Ilusão" })
        ficha = CatalogApply.setDeity(ficha, null)
        assertTrue(ficha.spells.none { it.name == "Criar Ilusão" })
    }

    @Test
    fun `druida recebe dois poderes concedidos`() {
        val druida = CatalogApply.setDeity(CatalogApply.applyClass(nova(), ClassCatalog.DRUIDA), DeityCatalog.byId("allihanna"))
        assertEquals(2, CatalogApply.grantedPowerLimit(druida))
    }

    // ---------- Habilidades por nível ----------

    @Test
    fun `habilidades de classe acompanham o nivel`() {
        var bucaneiro = CatalogApply.applyClass(nova().copy(level = 2), ClassCatalog.BUCANEIRO)
        assertTrue(bucaneiro.abilities.any { it.name == "Evasão" })
        assertTrue(bucaneiro.abilities.none { it.name == "Esquiva Sagaz" })
        bucaneiro = CatalogApply.refresh(bucaneiro.copy(level = 5))
        assertTrue(bucaneiro.abilities.any { it.name == "Esquiva Sagaz" } && bucaneiro.abilities.any { it.name == "Panache" })
        bucaneiro = CatalogApply.refresh(bucaneiro.copy(level = 1))
        assertTrue(bucaneiro.abilities.none { it.name == "Evasão" })
    }

    @Test
    fun `empatia selvagem de raca e classe da +2 em Adestramento`() {
        val sedaf = CatalogApply.applyClass(CatalogApply.applyRace(nova(), RaceCatalog.DAHLLAN), ClassCatalog.DRUIDA)
        assertEquals(1, sedaf.abilities.count { it.name == "Empatia Selvagem" })
        assertTrue(sedaf.skillBonuses[ADESTRAMENTO]!!.any { it.label == "Empatia Selvagem (recebida novamente)" })
    }

    @Test
    fun `trocar de classe remove as habilidades da anterior`() {
        var ficha = CatalogApply.applyClass(nova(), ClassCatalog.GUERREIRO)
        assertTrue(ficha.abilities.any { it.name == "Ataque Especial" })
        ficha = CatalogApply.applyClass(ficha, ClassCatalog.BARDO)
        assertTrue(ficha.abilities.none { it.name == "Ataque Especial" })
        assertTrue(ficha.abilities.any { it.name == "Inspiração" })
    }

    @Test
    fun `novos campos sobrevivem ao salvamento`() {
        var ficha = CatalogApply.applyClass(CatalogApply.applyRace(nova(), RaceCatalog.LEFOU), ClassCatalog.BARDO)
        ficha = CatalogApply.setDeity(ficha, DeityCatalog.byId("valkaria"))
        ficha = CatalogApply.setGrantedPower(ficha, "Coragem Total", true)
        ficha = CatalogApply.setSkillGroup(ficha, SkillGroupId.CLASS, setOf(LUTA, DIPLOMACIA))
        assertEquals(ficha, SheetCodec.decode(SheetCodec.encode(ficha)))
    }
}

class PoderesDeClasseTest {

    private fun personagem(cls: CatalogClass, level: Int = 3) =
        CatalogApply.applyClass(ManualSheet(name = "P", level = level, attributeMode = AttributeMethod.POINT_BUY), cls)

    @Test
    fun `todas as classes tem poderes com pagina e resumo`() {
        ClassCatalog.ALL.forEach { cls ->
            val poderes = ClassPowerCatalog.of(cls.id)
            assertTrue(poderes.size >= 18, "${cls.name} tem poucos poderes")
            assertTrue(poderes.all { it.summary.isNotBlank() && it.page in 36..70 })
            // Todo poder exigido existe na mesma classe
            poderes.flatMap { it.requirement.powers }.forEach { req -> assertNotNull(ClassPowerCatalog.find(cls.id, req), req) }
        }
    }

    @Test
    fun `limite de poderes e um por nivel a partir do 2o`() {
        assertEquals(0, CatalogApply.classPowerLimit(personagem(ClassCatalog.BARDO, 1)))
        assertEquals(2, CatalogApply.classPowerLimit(personagem(ClassCatalog.BARDO, 3)))
    }

    @Test
    fun `pre-requisitos de nivel, poder, atributo e caminho`() {
        val bardo = personagem(ClassCatalog.BARDO)
        val danca = ClassPowerCatalog.find("bardo", "Dança das Lâminas")!!
        assertEquals(listOf("Esgrima Mágica", "10º nível"), CatalogApply.unmetRequirements(bardo, danca))
        val lendas = ClassPowerCatalog.find("bardo", "Lendas e Histórias")!!
        assertEquals(listOf("Inteligência 1"), CatalogApply.unmetRequirements(bardo, lendas))

        val arcanista = CatalogApply.applyPath(personagem(ClassCatalog.ARCANISTA), ClassCatalog.ARCANISTA.pathById("feiticeiro")!!)
        val foco = ClassPowerCatalog.find("arcanista", "Foco Vital")!!
        assertEquals(listOf("Bruxo"), CatalogApply.unmetRequirements(arcanista, foco))
    }

    @Test
    fun `poder com efeito aplica e remove`() {
        var druida = personagem(ClassCatalog.DRUIDA)
        druida = CatalogApply.addClassPower(druida, "Liberdade da Pradaria")
        assertEquals(1 + 0 + 2, SheetEngine.skill(druida, Skill.REFLEXOS).total)
        druida = CatalogApply.removeClassPower(druida, "Liberdade da Pradaria")
        assertTrue(druida.skillBonuses[Skill.REFLEXOS].orEmpty().isEmpty())
    }

    @Test
    fun `poder magico soma PM por nivel`() {
        var arcanista = CatalogApply.applyPath(personagem(ClassCatalog.ARCANISTA), ClassCatalog.ARCANISTA.pathById("mago")!!)
        val antes = SheetEngine.maxMp(arcanista).total
        arcanista = CatalogApply.addClassPower(arcanista, "Poder Mágico")
        assertEquals(antes + 3, SheetEngine.maxMp(arcanista).total)
    }

    @Test
    fun `poderes repetiveis e magias extras`() {
        var bardo = personagem(ClassCatalog.BARDO, 5)
        bardo = CatalogApply.addClassPower(bardo, "Aumentar Repertório")
        bardo = CatalogApply.addClassPower(bardo, "Aumentar Repertório")
        bardo = CatalogApply.addClassPower(bardo, "Arte Mágica")
        bardo = CatalogApply.addClassPower(bardo, "Arte Mágica") // não repetível: ignorado
        assertEquals(listOf("Aumentar Repertório", "Aumentar Repertório", "Arte Mágica"), bardo.classPowers)
        assertEquals(4 + 4, CatalogApply.classSpellsKnown(bardo)) // 2 + 2 (níveis pares) + 2 × 2
        bardo = CatalogApply.removeClassPower(bardo, "Aumentar Repertório")
        assertEquals(1, bardo.classPowers.count { it == "Aumentar Repertório" })
    }

    @Test
    fun `flagelo dos mares ensina Amedrontar e trocar de classe remove os poderes`() {
        var bucaneiro = personagem(ClassCatalog.BUCANEIRO)
        bucaneiro = CatalogApply.addClassPower(bucaneiro, "Flagelo dos Mares")
        assertTrue(bucaneiro.spells.any { it.name == "Amedrontar" })
        val guerreiro = CatalogApply.applyClass(bucaneiro, ClassCatalog.GUERREIRO)
        assertTrue(guerreiro.classPowers.isEmpty())
        assertTrue(guerreiro.spells.none { it.name == "Amedrontar" })
    }

    @Test
    fun `poderes de classe sobrevivem ao salvamento`() {
        val bardo = CatalogApply.addClassPower(personagem(ClassCatalog.BARDO), "Inspiração Marcial")
        assertEquals(bardo, SheetCodec.decode(SheetCodec.encode(bardo)))
    }
}
