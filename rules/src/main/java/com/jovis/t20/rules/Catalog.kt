package com.jovis.t20.rules

import com.jovis.t20.rules.Attribute.*
import com.jovis.t20.rules.Skill.*

/*
 * Catálogo do Tormenta 20 Jogo do Ano, limitado ao que a mesa usa.
 * Os números e características vêm do livro (com a página); os resumos das magias
 * são textos curtos escritos para o app, não cópias do livro.
 */

// ============================== Magias ==============================

data class CatalogSpell(
    val name: String,
    val type: SpellType,
    val circle: Int,
    val school: SpellSchool,
    val execution: String,
    val range: String,
    /** "Alvo", "Alvos", "Área", "Efeito"... */
    val targetLabel: String,
    val target: String,
    val duration: String,
    val resistance: String?,
    val page: Int,
    val summary: String,
) {
    val cost: Int get() = SpellCost.of(circle)

    fun toSheetSpell(notes: String = ""): SheetSpell = SheetSpell(
        name = name,
        circle = circle,
        school = school.displayName,
        notes = notes,
        type = type.displayName,
        execution = execution,
        range = range,
        target = "$targetLabel: $target",
        duration = duration,
        resistance = resistance.orEmpty(),
        summary = summary,
        page = page,
    )
}

object SpellCatalog {
    /** Magias de 1º círculo (as que a mesa alcança agora). TODO: 2º círculo quando chegarem ao 6º nível. */
    val ALL: List<CatalogSpell> = listOf(
        CatalogSpell("Abençoar Alimentos", SpellType.DIVINE, 1, SpellSchool.TRANSMUTACAO, "padrão", "curto", "Alvo", "alimento para 1 criatura", "cena", null, 178,
            "Purifica uma porção de comida ou bebida; consumida na cena, dá alguns PV ou 1 PM temporários."),
        CatalogSpell("Acalmar Animal", SpellType.DIVINE, 1, SpellSchool.ENCANTAMENTO, "padrão", "curto", "Alvo", "1 animal", "cena", "Vontade anula", 178,
            "Deixa um animal prestativo com você e facilita muito lidar com ele."),
        CatalogSpell("Adaga Mental", SpellType.ARCANE, 1, SpellSchool.ENCANTAMENTO, "padrão", "curto", "Alvo", "1 criatura", "instantânea", "Vontade parcial", 178,
            "Uma lâmina psíquica causa dano à mente do alvo e pode atordoá-lo por uma rodada."),
        CatalogSpell("Alarme", SpellType.ARCANE, 1, SpellSchool.ABJURACAO, "padrão", "curto", "Área", "esfera com 9m de raio", "1 dia", null, 178,
            "Protege uma área e avisa você (por telepatia ou som) quando alguém entra nela."),
        CatalogSpell("Amedrontar", SpellType.ARCANE, 1, SpellSchool.NECROMANCIA, "padrão", "curto", "Alvo", "1 animal ou humanoide", "cena", "Vontade parcial", 179,
            "Enche o alvo de medo, deixando-o apavorado e depois abalado."),
        CatalogSpell("Arma Espiritual", SpellType.DIVINE, 1, SpellSchool.CONVOCACAO, "padrão", "pessoal", "Alvo", "você", "cena", null, 180,
            "Invoca a arma da sua divindade, que revida automaticamente quem atacar você corpo a corpo."),
        CatalogSpell("Arma Mágica", SpellType.UNIVERSAL, 1, SpellSchool.TRANSMUTACAO, "padrão", "toque", "Alvo", "1 arma empunhada", "cena", null, 181,
            "Torna uma arma mágica, com bônus em ataque e dano; quem a empunha pode atacar com o atributo de magia."),
        CatalogSpell("Armadura Arcana", SpellType.ARCANE, 1, SpellSchool.ABJURACAO, "padrão", "pessoal", "Alvo", "você", "cena", null, 181,
            "Uma película invisível dá um bom bônus na Defesa (não acumula com armadura)."),
        CatalogSpell("Armamento da Natureza", SpellType.DIVINE, 1, SpellSchool.TRANSMUTACAO, "padrão", "toque", "Alvo", "1 arma (veja texto)", "cena", null, 181,
            "Fortalece uma arma primitiva, natural ou desarmada: mais dano e passa a contar como mágica."),
        CatalogSpell("Aviso", SpellType.UNIVERSAL, 1, SpellSchool.ADIVINHACAO, "movimento", "longo", "Alvo", "1 criatura", "instantânea", null, 182,
            "Envia ao alvo, à distância, um alerta, uma mensagem curta ou sua localização."),
        CatalogSpell("Bênção", SpellType.DIVINE, 1, SpellSchool.ENCANTAMENTO, "padrão", "curto", "Alvos", "aliados", "cena", null, 182,
            "Aliados próximos ganham bônus em testes de ataque e rolagens de dano."),
        CatalogSpell("Caminhos da Natureza", SpellType.DIVINE, 1, SpellSchool.CONVOCACAO, "padrão", "curto", "Alvo", "criaturas escolhidas", "1 dia", null, 183,
            "Espíritos da natureza aumentam o deslocamento do grupo e anulam terreno difícil natural."),
        CatalogSpell("Comando", SpellType.DIVINE, 1, SpellSchool.ENCANTAMENTO, "padrão", "curto", "Alvo", "1 humanoide", "1 rodada", "Vontade anula", 184,
            "Uma ordem curta e irresistível que o alvo obedece no turno dele."),
        CatalogSpell("Compreensão", SpellType.UNIVERSAL, 1, SpellSchool.ADIVINHACAO, "padrão", "toque", "Alvo", "1 criatura ou texto", "cena", "Vontade anula (veja descrição)", 184,
            "Entende qualquer texto ou idioma, percebe sentimentos de animais e pode ler pensamentos superficiais."),
        CatalogSpell("Concentração de Combate", SpellType.ARCANE, 1, SpellSchool.ADIVINHACAO, "livre", "pessoal", "Alvo", "você", "1 rodada", null, 185,
            "Você rola dois dados no ataque e fica com o melhor resultado."),
        CatalogSpell("Conjurar Monstro", SpellType.ARCANE, 1, SpellSchool.CONVOCACAO, "completa", "curto", "Efeito", "1 criatura conjurada", "sustentada", null, 185,
            "Cria um monstro de energia que luta ao seu lado enquanto você mantém a magia."),
        CatalogSpell("Consagrar", SpellType.DIVINE, 1, SpellSchool.EVOCACAO, "padrão", "longo", "Área", "esfera com 9m de raio", "1 dia", null, 186,
            "Enche uma área de energia positiva: curas de luz ali ficam no valor máximo."),
        CatalogSpell("Controlar Plantas", SpellType.DIVINE, 1, SpellSchool.TRANSMUTACAO, "padrão", "curto", "Área", "quadrado com 9m de lado", "cena", "Reflexos anula", 188,
            "A vegetação prende as criaturas da área e transforma o lugar em terreno difícil."),
        CatalogSpell("Criar Elementos", SpellType.DIVINE, 1, SpellSchool.CONVOCACAO, "padrão", "curto", "Efeito", "elemento escolhido", "instantânea", null, 188,
            "Cria uma pequena porção de água, ar, fogo ou terra, real e não mágica."),
        CatalogSpell("Criar Ilusão", SpellType.ARCANE, 1, SpellSchool.ILUSAO, "padrão", "médio", "Efeito", "ilusão que se estende a até 4 cubos de 1,5m", "cena", "Vontade desacredita", 189,
            "Cria uma imagem ou som ilusório simples, que pode ser desacreditado."),
        CatalogSpell("Curar Ferimentos", SpellType.DIVINE, 1, SpellSchool.EVOCACAO, "padrão", "toque", "Alvo", "1 criatura", "instantânea", null, 189,
            "Cura pontos de vida de uma criatura tocada."),
        CatalogSpell("Despedaçar", SpellType.DIVINE, 1, SpellSchool.EVOCACAO, "padrão", "curto", "Alvo", "1 criatura ou objeto mundano Pequeno", "instantânea", "Fortitude parcial", 190,
            "Um som agudo causa dano de impacto e pode atordoar; é devastador contra objetos e construtos."),
        CatalogSpell("Detectar Ameaças", SpellType.DIVINE, 1, SpellSchool.ADIVINHACAO, "padrão", "pessoal", "Área", "esfera com 18m de raio", "cena, até ser descarregada", null, 190,
            "Você percebe quando criaturas hostis ou armadilhas entram na área ao seu redor."),
        CatalogSpell("Disfarce Ilusório", SpellType.ARCANE, 1, SpellSchool.ILUSAO, "padrão", "pessoal", "Alvo", "você", "cena", "Vontade desacredita", 191,
            "Muda sua aparência e a do seu equipamento, com grande bônus para se disfarçar."),
        CatalogSpell("Enfeitiçar", SpellType.ARCANE, 1, SpellSchool.ENCANTAMENTO, "padrão", "curto", "Alvo", "1 humanoide", "cena", "Vontade anula", 191,
            "Deixa um humanoide enfeitiçado, tratando você como amigo."),
        CatalogSpell("Escudo da Fé", SpellType.DIVINE, 1, SpellSchool.ABJURACAO, "reação", "curto", "Alvo", "1 criatura", "1 turno", null, 192,
            "Como reação, dá um bônus rápido na Defesa de uma criatura contra um golpe."),
        CatalogSpell("Escuridão", SpellType.UNIVERSAL, 1, SpellSchool.NECROMANCIA, "padrão", "curto", "Alvo", "1 objeto", "cena", "Vontade anula (veja texto)", 193,
            "Um objeto passa a emanar sombras que dão camuflagem leve na área."),
        CatalogSpell("Explosão de Chamas", SpellType.ARCANE, 1, SpellSchool.EVOCACAO, "padrão", "pessoal", "Área", "cone de 6m", "instantânea", "Reflexos reduz à metade", 193,
            "Um cone de fogo sai das suas mãos e queima as criaturas à frente."),
        CatalogSpell("Imagem Espelhada", SpellType.ARCANE, 1, SpellSchool.ILUSAO, "padrão", "pessoal", "Alvo", "você", "cena", null, 195,
            "Cópias ilusórias suas confundem os inimigos e dão um bônus na Defesa que cai a cada ataque errado."),
        CatalogSpell("Infligir Ferimentos", SpellType.DIVINE, 1, SpellSchool.NECROMANCIA, "padrão", "toque", "Alvo", "1 criatura", "instantânea", "Fortitude reduz à metade", 195,
            "Energia negativa causa dano de trevas ao toque (e cura mortos-vivos)."),
        CatalogSpell("Leque Cromático", SpellType.ARCANE, 1, SpellSchool.ILUSAO, "padrão", "pessoal", "Área", "cone de 4,5m", "instantânea", "Vontade parcial", 196,
            "Um cone de luzes deixa animais e humanoides ofuscados e pode atordoá-los."),
        CatalogSpell("Luz", SpellType.UNIVERSAL, 1, SpellSchool.EVOCACAO, "padrão", "curto", "Alvo", "1 objeto", "cena", "Vontade anula (veja texto)", 197,
            "Um objeto passa a iluminar a área ao redor."),
        CatalogSpell("Névoa", SpellType.UNIVERSAL, 1, SpellSchool.CONVOCACAO, "padrão", "curto", "Efeito", "nuvem com 6m de raio e 6m de altura", "cena", null, 200,
            "Uma névoa espessa bloqueia a visão e dá camuflagem a quem está dentro dela."),
        CatalogSpell("Orientação", SpellType.DIVINE, 1, SpellSchool.ADIVINHACAO, "padrão", "curto", "Alvo", "1 criatura", "1 rodada", null, 200,
            "No próximo teste de perícia, o alvo rola dois dados e fica com o melhor."),
        CatalogSpell("Perdição", SpellType.DIVINE, 1, SpellSchool.NECROMANCIA, "padrão", "curto", "Alvos", "criaturas escolhidas", "cena", "nenhuma", 201,
            "Inimigos escolhidos sofrem penalidade em testes de ataque e rolagens de dano."),
        CatalogSpell("Primor Atlético", SpellType.ARCANE, 1, SpellSchool.TRANSMUTACAO, "padrão", "toque", "Alvo", "1 criatura", "cena", null, 201,
            "O alvo ganha muito deslocamento e um grande bônus em Atletismo."),
        CatalogSpell("Profanar", SpellType.DIVINE, 1, SpellSchool.NECROMANCIA, "padrão", "longo", "Área", "esfera com 9m de raio", "1 dia", null, 202,
            "Enche uma área de energia negativa: dano de trevas ali fica no valor máximo."),
        CatalogSpell("Proteção Divina", SpellType.DIVINE, 1, SpellSchool.ABJURACAO, "padrão", "toque", "Alvo", "1 criatura", "cena", null, 202,
            "Uma barreira invisível dá bônus nos testes de resistência do alvo."),
        CatalogSpell("Queda Suave", SpellType.ARCANE, 1, SpellSchool.TRANSMUTACAO, "reação", "curto", "Alvos", "1 criatura ou objeto Grande ou menor", "até chegar ao solo ou cena, o que vier primeiro", null, 202,
            "Como reação, faz uma criatura ou objeto cair devagar, sem sofrer dano."),
        CatalogSpell("Raio do Enfraquecimento", SpellType.ARCANE, 1, SpellSchool.NECROMANCIA, "padrão", "curto", "Alvo", "1 criatura", "cena", "Fortitude parcial", 202,
            "Um raio drena as forças do alvo, deixando-o fatigado ou vulnerável."),
        CatalogSpell("Resistência a Energia", SpellType.UNIVERSAL, 1, SpellSchool.ABJURACAO, "padrão", "toque", "Alvo", "1 criatura", "cena", null, 204,
            "Dá redução de dano contra um tipo de energia escolhido."),
        CatalogSpell("Santuário", SpellType.DIVINE, 1, SpellSchool.ABJURACAO, "padrão", "toque", "Alvo", "1 criatura", "cena", "Vontade anula", 205,
            "Quem tentar agir contra o alvo precisa vencer um teste de Vontade, mas o alvo também não pode atacar."),
        CatalogSpell("Seta Infalível de Talude", SpellType.ARCANE, 1, SpellSchool.EVOCACAO, "padrão", "médio", "Alvos", "criaturas escolhidas", "instantânea", null, 206,
            "Dispara setas de energia que acertam sem teste de ataque."),
        CatalogSpell("Sono", SpellType.ARCANE, 1, SpellSchool.ENCANTAMENTO, "padrão", "curto", "Alvo", "1 humanoide", "cena", "Vontade parcial", 207,
            "Faz um humanoide adormecer ou, em combate, ficar exausto."),
        CatalogSpell("Suporte Ambiental", SpellType.DIVINE, 1, SpellSchool.ABJURACAO, "padrão", "toque", "Alvo", "1 criatura", "1 dia", null, 207,
            "Protege contra calor e frio extremos, permite respirar na água e em fumaça."),
        CatalogSpell("Teia", SpellType.ARCANE, 1, SpellSchool.CONVOCACAO, "padrão", "curto", "Área", "cubo com 6m de lado", "cena", "Reflexos anula", 208,
            "Fios pegajosos prendem quem estiver na área e criam terreno difícil; a teia queima fácil."),
        CatalogSpell("Toque Chocante", SpellType.ARCANE, 1, SpellSchool.EVOCACAO, "padrão", "toque", "Alvo", "1 criatura", "instantânea", "Fortitude reduz à metade", 209,
            "Eletricidade ao toque causa dano, pior contra quem usa armadura de metal."),
        CatalogSpell("Tranca Arcana", SpellType.ARCANE, 1, SpellSchool.ABJURACAO, "padrão", "toque", "Alvo", "1 objeto Grande ou menor", "permanente", null, 209,
            "Tranca magicamente uma porta ou baú, dificultando abri-lo."),
        CatalogSpell("Tranquilidade", SpellType.DIVINE, 1, SpellSchool.ENCANTAMENTO, "padrão", "curto", "Alvo", "1 animal ou humanoide", "cena", "Vontade parcial", 210,
            "Acalma um animal ou humanoide, que fica indiferente e não consegue agir com agressividade."),
        CatalogSpell("Transmutar Objetos", SpellType.ARCANE, 1, SpellSchool.TRANSMUTACAO, "padrão", "toque", "Alvo", "matéria-prima, como madeira, rochas, ossos", "cena", null, 210,
            "Molda matéria-prima em um objeto simples, que volta ao normal no fim da cena."),
        CatalogSpell("Visão Mística", SpellType.UNIVERSAL, 1, SpellSchool.ADIVINHACAO, "padrão", "pessoal", "Alvo", "você", "cena", null, 211,
            "Você enxerga auras mágicas e descobre quem é capaz de lançar magias."),
        CatalogSpell("Vitalidade Fantasma", SpellType.ARCANE, 1, SpellSchool.NECROMANCIA, "padrão", "pessoal", "Alvo", "você", "instantânea", null, 211,
            "Você suga energia da terra e recebe pontos de vida temporários."),
        CatalogSpell("Área Escorregadia", SpellType.ARCANE, 1, SpellSchool.CONVOCACAO, "padrão", "curto", "Alvo ou Área", "quadrado de 3m ou 1 objeto", "cena", "Reflexos (veja texto)", 180,
            "Cobre o chão ou um objeto com uma substância escorregadia que derruba e desarma."),
    )

    fun find(name: String): CatalogSpell? = ALL.firstOrNull { it.name.equals(name, ignoreCase = true) }

    /** O que um conjurador pode aprender: tipo compatível, escolas escolhidas e círculo liberado. */
    fun available(casting: Spellcasting, schools: Set<SpellSchool>, level: Int): List<CatalogSpell> {
        val maxCircle = casting.maxCircle(level)
        return ALL.filter {
            it.type.castableBy(casting.type) && (!casting.restrictsSchools || it.school in schools) && it.circle <= maxCircle
        }
    }
}

// ============================== Armas ==============================

enum class WeaponGrip(val displayName: String) { LEVE("Leve"), UMA_MAO("Uma mão"), DUAS_MAOS("Duas mãos") }

data class CatalogWeapon(
    val name: String,
    val proficiency: WeaponProficiency,
    val grip: WeaponGrip,
    val damage: String,
    val criticalRange: Int,
    val criticalMultiplier: Int,
    /** Alcance de arremesso ou disparo; null para armas só de corpo a corpo. */
    val range: String?,
    val damageType: String,
    val slots: Double,
    val price: String,
    /** O primeiro modo é o principal; armas como a lança também podem ser arremessadas. */
    val modes: List<AttackMode>,
    val note: String = "",
) {
    val criticalText: String
        get() = if (criticalMultiplier == 2) "$criticalRange" else if (criticalRange == 20) "x$criticalMultiplier" else "$criticalRange/x$criticalMultiplier"

    fun toSheetWeapons(): List<SheetWeapon> = modes.mapIndexed { index, mode ->
        SheetWeapon(
            name = if (index == 0) name else "$name (arremessada)",
            damageDice = damage,
            mode = mode,
            criticalRange = criticalRange,
            criticalMultiplier = criticalMultiplier,
            notes = listOfNotNull(
                damageType,
                range?.takeIf { mode != AttackMode.MELEE }?.let { "alcance ${it.lowercase()}" },
                note.takeIf { it.isNotBlank() },
            ).joinToString(", "),
        )
    }

    fun toSheetItem(): SheetItem = SheetItem(name, slots = slots)
}

/** Fonte: Tabela 3-3, p. 144-145. */
object WeaponCatalog {
    private val S = WeaponProficiency.SIMPLE
    private val M = WeaponProficiency.MARTIAL
    private val E = WeaponProficiency.EXOTIC
    private val F = WeaponProficiency.FIREARM
    private val MELEE = listOf(AttackMode.MELEE)
    private val MELEE_THROWN = listOf(AttackMode.MELEE, AttackMode.THROWN)
    private val THROWN = listOf(AttackMode.THROWN)
    private val FIRING = listOf(AttackMode.FIRING)
    private const val P = "Perfuração"
    private const val C = "Corte"
    private const val I = "Impacto"

    val ALL: List<CatalogWeapon> = listOf(
        // Simples
        CatalogWeapon("Adaga", S, WeaponGrip.LEVE, "1d4", 19, 2, "Curto", P, 1.0, "T$ 2", MELEE_THROWN),
        CatalogWeapon("Espada curta", S, WeaponGrip.LEVE, "1d6", 19, 2, null, P, 1.0, "T$ 10", MELEE),
        CatalogWeapon("Foice", S, WeaponGrip.LEVE, "1d6", 20, 3, null, C, 1.0, "T$ 4", MELEE),
        CatalogWeapon("Clava", S, WeaponGrip.UMA_MAO, "1d6", 20, 2, null, I, 1.0, "—", MELEE),
        CatalogWeapon("Lança", S, WeaponGrip.UMA_MAO, "1d6", 20, 2, "Curto", P, 1.0, "T$ 2", MELEE_THROWN),
        CatalogWeapon("Maça", S, WeaponGrip.UMA_MAO, "1d8", 20, 2, null, I, 1.0, "T$ 12", MELEE),
        CatalogWeapon("Bordão", S, WeaponGrip.DUAS_MAOS, "1d6/1d6", 20, 2, null, I, 2.0, "—", MELEE, "arma dupla"),
        CatalogWeapon("Pique", S, WeaponGrip.DUAS_MAOS, "1d8", 20, 2, null, P, 2.0, "T$ 2", MELEE),
        CatalogWeapon("Tacape", S, WeaponGrip.DUAS_MAOS, "1d10", 20, 2, null, I, 2.0, "—", MELEE),
        CatalogWeapon("Azagaia", S, WeaponGrip.UMA_MAO, "1d6", 20, 2, "Médio", P, 1.0, "T$ 1", THROWN, "–5 se usada corpo a corpo"),
        CatalogWeapon("Besta leve", S, WeaponGrip.UMA_MAO, "1d8", 19, 2, "Médio", P, 1.0, "T$ 35", FIRING, "usa virotes"),
        // A funda é de disparo, mas soma a Força no dano (p. 143); por isso entra como arremesso no cálculo.
        CatalogWeapon("Funda", S, WeaponGrip.UMA_MAO, "1d4", 20, 2, "Médio", I, 1.0, "—", THROWN, "disparo que soma Força; usa pedras"),
        CatalogWeapon("Arco curto", S, WeaponGrip.DUAS_MAOS, "1d6", 20, 3, "Médio", P, 2.0, "T$ 30", FIRING, "usa flechas"),
        // Marciais
        CatalogWeapon("Machadinha", M, WeaponGrip.LEVE, "1d6", 20, 3, "Curto", C, 1.0, "T$ 6", MELEE_THROWN),
        CatalogWeapon("Cimitarra", M, WeaponGrip.UMA_MAO, "1d6", 18, 2, null, C, 1.0, "T$ 15", MELEE),
        CatalogWeapon("Espada longa", M, WeaponGrip.UMA_MAO, "1d8", 19, 2, null, C, 1.0, "T$ 15", MELEE),
        CatalogWeapon("Florete", M, WeaponGrip.UMA_MAO, "1d6", 18, 2, null, P, 1.0, "T$ 20", MELEE),
        CatalogWeapon("Machado de batalha", M, WeaponGrip.UMA_MAO, "1d8", 20, 3, null, C, 1.0, "T$ 10", MELEE),
        CatalogWeapon("Mangual", M, WeaponGrip.UMA_MAO, "1d8", 20, 2, null, I, 1.0, "T$ 8", MELEE),
        CatalogWeapon("Martelo de guerra", M, WeaponGrip.UMA_MAO, "1d8", 20, 3, null, I, 1.0, "T$ 12", MELEE),
        CatalogWeapon("Picareta", M, WeaponGrip.UMA_MAO, "1d6", 20, 4, null, P, 1.0, "T$ 8", MELEE),
        CatalogWeapon("Tridente", M, WeaponGrip.UMA_MAO, "1d8", 20, 2, "Curto", P, 1.0, "T$ 15", MELEE_THROWN),
        CatalogWeapon("Alabarda", M, WeaponGrip.DUAS_MAOS, "1d10", 20, 3, null, "Corte/perfuração", 2.0, "T$ 10", MELEE),
        CatalogWeapon("Alfange", M, WeaponGrip.DUAS_MAOS, "2d4", 18, 2, null, C, 2.0, "T$ 75", MELEE),
        CatalogWeapon("Gadanho", M, WeaponGrip.DUAS_MAOS, "2d4", 20, 4, null, C, 2.0, "T$ 18", MELEE),
        CatalogWeapon("Lança montada", M, WeaponGrip.DUAS_MAOS, "1d8", 20, 3, null, P, 2.0, "T$ 10", MELEE),
        CatalogWeapon("Machado de guerra", M, WeaponGrip.DUAS_MAOS, "1d12", 20, 3, null, C, 2.0, "T$ 20", MELEE),
        CatalogWeapon("Marreta", M, WeaponGrip.DUAS_MAOS, "3d4", 20, 2, null, I, 2.0, "T$ 20", MELEE),
        CatalogWeapon("Montante", M, WeaponGrip.DUAS_MAOS, "2d6", 19, 2, null, C, 2.0, "T$ 50", MELEE),
        CatalogWeapon("Arco longo", M, WeaponGrip.DUAS_MAOS, "1d8", 20, 3, "Médio", P, 2.0, "T$ 100", FIRING, "usa flechas"),
        CatalogWeapon("Besta pesada", M, WeaponGrip.DUAS_MAOS, "1d12", 19, 2, "Médio", P, 2.0, "T$ 50", FIRING, "usa virotes"),
        // Exóticas
        CatalogWeapon("Chicote", E, WeaponGrip.UMA_MAO, "1d3", 20, 2, null, C, 1.0, "T$ 2", MELEE),
        CatalogWeapon("Espada bastarda", E, WeaponGrip.UMA_MAO, "1d10/1d12", 19, 2, null, C, 1.0, "T$ 35", MELEE, "segundo dano com as duas mãos"),
        CatalogWeapon("Katana", E, WeaponGrip.UMA_MAO, "1d8/1d10", 19, 2, null, C, 1.0, "T$ 100", MELEE, "segundo dano com as duas mãos"),
        CatalogWeapon("Machado anão", E, WeaponGrip.UMA_MAO, "1d10", 20, 3, null, C, 1.0, "T$ 30", MELEE),
        CatalogWeapon("Corrente de espinhos", E, WeaponGrip.DUAS_MAOS, "2d4/2d4", 19, 2, null, C, 2.0, "T$ 25", MELEE, "arma dupla"),
        CatalogWeapon("Machado táurico", E, WeaponGrip.DUAS_MAOS, "2d8", 20, 3, null, C, 2.0, "T$ 50", MELEE),
        // Armas de fogo
        CatalogWeapon("Pistola", F, WeaponGrip.LEVE, "2d6", 19, 3, "Curto", P, 1.0, "T$ 250", FIRING, "usa balas"),
        CatalogWeapon("Mosquete", F, WeaponGrip.DUAS_MAOS, "2d8", 19, 3, "Médio", P, 2.0, "T$ 500", FIRING, "usa balas"),
    )

    /** Munições, que ocupam espaço mas não são ataques. */
    val AMMUNITION: List<CatalogItem> = listOf(
        CatalogItem("Flechas (20)", "T$ 1", 1.0),
        CatalogItem("Virotes (20)", "T$ 2", 1.0),
        CatalogItem("Pedras (20)", "T$ 0,5", 1.0),
        CatalogItem("Balas (20)", "T$ 20", 1.0),
    )
}

// ============================== Armaduras e escudos ==============================

object ArmorCatalog {
    val ARMORS: List<ArmorDefinition> = listOf(
        Armors.ARMADURA_ACOLCHOADA, Armors.ARMADURA_DE_COURO, Armors.COURO_BATIDO, Armors.GIBAO_DE_PELES, Armors.COURACA,
        Armors.BRUNEA, Armors.COTA_DE_MALHA, Armors.LORIGA_SEGMENTADA, Armors.MEIA_ARMADURA, Armors.ARMADURA_COMPLETA,
    )
    val SHIELDS: List<ShieldDefinition> = listOf(Shields.ESCUDO_LEVE, Shields.ESCUDO_PESADO)

    /** Preços da Tabela 3-5, p. 153. */
    val PRICES: Map<String, String> = mapOf(
        "Armadura acolchoada" to "T$ 5", "Armadura de couro" to "T$ 20", "Couro batido" to "T$ 35",
        "Gibão de peles" to "T$ 25", "Couraça" to "T$ 500", "Brunea" to "T$ 50", "Cota de malha" to "T$ 150",
        "Loriga segmentada" to "T$ 250", "Meia armadura" to "T$ 600", "Armadura completa" to "T$ 3.000",
        "Escudo leve" to "T$ 5", "Escudo pesado" to "T$ 15",
    )

    fun toSheet(armor: ArmorDefinition) = SheetArmor(armor.name, armor.defenseBonus, armor.armorPenalty, armor.category == ArmorCategory.HEAVY)
    fun toSheet(shield: ShieldDefinition) = SheetShield(shield.name, shield.defenseBonus, shield.armorPenalty)
    fun toItem(armor: ArmorDefinition) = SheetItem(armor.name, slots = armor.slots, equipped = true)
    fun toItem(shield: ShieldDefinition) = SheetItem(shield.name, slots = shield.slots, equipped = true)
}

// ============================== Itens gerais ==============================

data class CatalogItem(val name: String, val price: String, val slots: Double) {
    fun toSheetItem(): SheetItem = SheetItem(name, slots = slots)
}

/** Fonte: Tabela 3-6, p. 156 (sem serviços). Animais e veículos ocupam 0 espaços. */
object ItemCatalog {
    val ALL: List<CatalogItem> = listOf(
        CatalogItem("Alaúde élfico", "T$ 300", 1.0),
        CatalogItem("Alforje", "T$ 30", 0.0),
        CatalogItem("Algemas", "T$ 15", 1.0),
        CatalogItem("Andrajos de aldeão", "T$ 1", 1.0),
        CatalogItem("Arpéu", "T$ 5", 1.0),
        CatalogItem("Baga-de-fogo", "T$ 30", 0.5),
        CatalogItem("Balão goblin", "T$ 200", 0.0),
        CatalogItem("Bandana", "T$ 5", 1.0),
        CatalogItem("Bandoleira de poções", "T$ 20", 1.0),
        CatalogItem("Barraca", "T$ 10", 1.0),
        CatalogItem("Batata valkariana", "T$ 2", 0.5),
        CatalogItem("Beladona", "T$ 1.500", 0.5),
        CatalogItem("Bolsa de pó", "T$ 300", 1.0),
        CatalogItem("Bomba", "T$ 50", 0.5),
        CatalogItem("Botas reforçadas", "T$ 20", 1.0),
        CatalogItem("Bruma sonolenta", "T$ 150", 0.5),
        CatalogItem("Bálsamo restaurador", "T$ 10", 0.5),
        CatalogItem("Cajado arcano", "T$ 1.000", 2.0),
        CatalogItem("Camisa bufante", "T$ 25", 1.0),
        CatalogItem("Canoa", "T$ 70", 0.0),
        CatalogItem("Capa esvoaçante", "T$ 25", 1.0),
        CatalogItem("Capa pesada", "T$ 15", 1.0),
        CatalogItem("Carroça", "T$ 150", 0.0),
        CatalogItem("Carruagem", "T$ 500", 0.0),
        CatalogItem("Casaco longo", "T$ 20", 1.0),
        CatalogItem("Cavalo de guerra", "T$ 400", 0.0),
        CatalogItem("Cavalo", "T$ 75", 0.0),
        CatalogItem("Cetro elemental", "T$ 750", 1.0),
        CatalogItem("Chapéu arcano", "T$ 50", 1.0),
        CatalogItem("Cicuta", "T$ 60", 0.5),
        CatalogItem("Coleção de livros", "T$ 75", 1.0),
        CatalogItem("Corda", "T$ 1", 1.0),
        CatalogItem("Cosmético", "T$ 30", 0.5),
        CatalogItem("Costela de lich", "T$ 300", 1.0),
        CatalogItem("Cão de caça", "T$ 150", 0.0),
        CatalogItem("Dedo de ente", "T$ 200", 1.0),
        CatalogItem("Dente-de-dragão", "T$ 45", 0.5),
        CatalogItem("Elixir do amor", "T$ 100", 0.5),
        CatalogItem("Enfeite de elmo", "T$ 15", 1.0),
        CatalogItem("Equipamento de viagem", "T$ 10", 1.0),
        CatalogItem("Espelho", "T$ 10", 1.0),
        CatalogItem("Essência abissal", "T$ 150", 0.5),
        CatalogItem("Essência de mana", "T$ 50", 0.5),
        CatalogItem("Essência de sombra", "T$ 100", 0.5),
        CatalogItem("Estojo de disfarces", "T$ 50", 1.0),
        CatalogItem("Farrapos de ermitão", "T$ 1", 1.0),
        CatalogItem("Flauta mística", "T$ 150", 1.0),
        CatalogItem("Fogo alquímico", "T$ 10", 0.5),
        CatalogItem("Gazua", "T$ 5", 1.0),
        CatalogItem("Gorad quente", "T$ 18", 0.5),
        CatalogItem("Gorro de ervas", "T$ 75", 1.0),
        CatalogItem("Instrumento musical", "T$ 35", 1.0),
        CatalogItem("Instrumentos de <ofício>", "T$ 30", 1.0),
        CatalogItem("Lampião", "T$ 7", 1.0),
        CatalogItem("Luneta", "T$ 100", 1.0),
        CatalogItem("Luva de ferro", "T$ 150", 1.0),
        CatalogItem("Luva de pelica", "T$ 5", 1.0),
        CatalogItem("Líquen lilás", "T$ 30", 0.5),
        CatalogItem("Macarrão de Yuvalin", "T$ 6", 0.5),
        CatalogItem("Maleta de medicamentos", "T$ 50", 1.0),
        CatalogItem("Manopla", "T$ 10", 1.0),
        CatalogItem("Manto camuflado", "T$ 12", 1.0),
        CatalogItem("Manto eclesiástico", "T$ 20", 1.0),
        CatalogItem("Medalhão de prata", "T$ 750", 1.0),
        CatalogItem("Mochila de aventureiro", "T$ 50", 0.0),
        CatalogItem("Mochila", "T$ 2", 0.0),
        CatalogItem("Musgo púrpura", "T$ 45", 0.5),
        CatalogItem("Névoa tóxica", "T$ 30", 0.5),
        CatalogItem("Orbe cristalino", "T$ 750", 1.0),
        CatalogItem("Organizador de pergaminhos", "T$ 25", 1.0),
        CatalogItem("Ossos de monstro", "T$ 45", 0.5),
        CatalogItem("Peçonha comum", "T$ 15", 0.5),
        CatalogItem("Peçonha concentrada", "T$ 90", 0.5),
        CatalogItem("Peçonha potente", "T$ 600", 0.5),
        CatalogItem("Prato do aventureiro", "T$ 1", 0.5),
        CatalogItem("Pé de cabra", "T$ 2", 1.0),
        CatalogItem("Pó de cristal", "T$ 30", 0.5),
        CatalogItem("Pó de giz", "T$ 30", 0.5),
        CatalogItem("Pó de lich", "T$ 3.000", 0.5),
        CatalogItem("Pó do desaparecimento", "T$ 100", 0.5),
        CatalogItem("Pônei de guerra", "T$ 30", 0.0),
        CatalogItem("Pônei", "T$ 5", 0.0),
        CatalogItem("Ramo verdejante", "T$ 45", 0.5),
        CatalogItem("Ração de viagem (por dia)", "T$ 0,5", 0.5),
        CatalogItem("Refeição comum", "T$ 0,3", 0.5),
        CatalogItem("Riso de Nimb", "T$ 150", 0.5),
        CatalogItem("Robe místico", "T$ 50", 1.0),
        CatalogItem("Saco de dormir", "T$ 1", 1.0),
        CatalogItem("Saco de sal", "T$ 45", 0.5),
        CatalogItem("Sapatos de camurça", "T$ 8", 1.0),
        CatalogItem("Seixo de âmbar", "T$ 30", 0.5),
        CatalogItem("Sela", "T$ 20", 1.0),
        CatalogItem("Sopa de peixe", "T$ 1", 0.5),
        CatalogItem("Símbolo sagrado", "T$ 5", 1.0),
        CatalogItem("Tabardo", "T$ 10", 1.0),
        CatalogItem("Tambor das profundezas", "T$ 80", 1.0),
        CatalogItem("Terra de cemitério", "T$ 30", 0.5),
        CatalogItem("Tocha", "T$ 0,1", 1.0),
        CatalogItem("Tomo hermético", "T$ 1.500", 1.0),
        CatalogItem("Traje da corte", "T$ 100", 1.0),
        CatalogItem("Traje de viajante", "T$ 10", 0.0),
        CatalogItem("Trobo", "T$ 60", 0.0),
        CatalogItem("Varinha arcana", "T$ 100", 1.0),
        CatalogItem("Veleiro", "T$ 10.000", 0.0),
        CatalogItem("Veste de seda", "T$ 25", 1.0),
        CatalogItem("Ácido", "T$ 10", 0.5),
        CatalogItem("Água benta", "T$ 10", 0.5),
        CatalogItem("Óleo", "T$ 0,1", 0.5),
    ) + WeaponCatalog.AMMUNITION
}

// ============================== Classes ==============================

data class CatalogClass(
    val id: String,
    val name: String,
    val page: Int,
    val hpFirstLevel: Int,
    val hpPerLevel: Int,
    val mpPerLevel: Int,
    val mpAttribute: Attribute?,
    val spellcasting: Spellcasting?,
    /** Perícias que a classe sempre treina. */
    val fixedSkills: Set<Skill>,
    /** Perícias em que a classe treina uma, à escolha (ex.: Luta ou Pontaria). */
    val fixedChoice: Set<Skill>,
    val skillChoiceCount: Int,
    val skillChoiceList: Set<Skill>,
    val proficiencies: String,
    /** Caminhos que mudam as regras de magia (ex.: Bruxo, Feiticeiro e Mago do arcanista). */
    val paths: List<ClassPath> = emptyList(),
) {
    /** As regras de magia valendo: as do caminho escolhido ou, sem caminhos, as da classe. */
    fun castingFor(pathId: String?): Spellcasting? =
        if (paths.isEmpty()) spellcasting else paths.firstOrNull { it.id == pathId }?.spellcasting

    fun pathById(pathId: String?): ClassPath? = paths.firstOrNull { it.id == pathId }
}

data class ClassPath(
    val id: String,
    val name: String,
    val summary: String,
    val spellcasting: Spellcasting,
)

object ClassCatalog {
    private fun casting(attribute: Attribute, type: SpellType) = Spellcasting(
        attribute = attribute, type = type, schoolCount = 3, initialSpells = 2, spellsPerEvenLevel = 1,
        circleUnlockLevel = mapOf(1 to 1, 2 to 6, 3 to 10, 4 to 14),
    )

    /** p. 44 */
    val BARDO = CatalogClass(
        "bardo", "Bardo", 44, 12, 3, 4, CARISMA, casting(CARISMA, SpellType.ARCANE),
        fixedSkills = setOf(ATUACAO, REFLEXOS), fixedChoice = emptySet(),
        skillChoiceCount = 6,
        skillChoiceList = setOf(ACROBACIA, CAVALGAR, CONHECIMENTO, DIPLOMACIA, ENGANACAO, FURTIVIDADE, INICIATIVA, INTUICAO,
            INVESTIGACAO, JOGATINA, LADINAGEM, LUTA, MISTICISMO, NOBREZA, PERCEPCAO, PONTARIA, VONTADE),
        proficiencies = "Armas marciais",
    )

    /** p. 47 */
    val BUCANEIRO = CatalogClass(
        "bucaneiro", "Bucaneiro", 47, 16, 4, 3, null, null,
        fixedSkills = setOf(REFLEXOS), fixedChoice = setOf(LUTA, PONTARIA),
        skillChoiceCount = 4,
        skillChoiceList = setOf(ACROBACIA, ATLETISMO, ATUACAO, ENGANACAO, FORTITUDE, FURTIVIDADE, INICIATIVA, INTIMIDACAO,
            JOGATINA, LUTA, OFICIO, PERCEPCAO, PILOTAGEM, PONTARIA),
        proficiencies = "Armas marciais",
    )

    /** p. 61 */
    val DRUIDA = CatalogClass(
        "druida", "Druida", 61, 16, 4, 4, SABEDORIA, casting(SABEDORIA, SpellType.DIVINE),
        fixedSkills = setOf(SOBREVIVENCIA, VONTADE), fixedChoice = emptySet(),
        skillChoiceCount = 4,
        skillChoiceList = setOf(ADESTRAMENTO, ATLETISMO, CAVALGAR, CONHECIMENTO, CURA, FORTITUDE, INICIATIVA, INTUICAO, LUTA,
            MISTICISMO, OFICIO, PERCEPCAO, RELIGIAO),
        proficiencies = "Escudos",
    )

    /** p. 65 */
    val GUERREIRO = CatalogClass(
        "guerreiro", "Guerreiro", 65, 20, 5, 3, null, null,
        fixedSkills = setOf(FORTITUDE), fixedChoice = setOf(LUTA, PONTARIA),
        skillChoiceCount = 2,
        skillChoiceList = setOf(ADESTRAMENTO, ATLETISMO, CAVALGAR, GUERRA, INICIATIVA, INTIMIDACAO, LUTA, OFICIO, PERCEPCAO,
            PONTARIA, REFLEXOS),
        proficiencies = "Armas marciais, armaduras pesadas e escudos",
    )

    private fun arcanistCasting(attribute: Attribute, initial: Int, progression: SpellProgression) = Spellcasting(
        attribute = attribute, type = SpellType.ARCANE, schoolCount = 0, initialSpells = initial, spellsPerEvenLevel = 0,
        circleUnlockLevel = mapOf(1 to 1, 2 to 5, 3 to 9, 4 to 13, 5 to 17), progression = progression,
    )

    /** p. 36-37. Magias arcanas de qualquer escola; o caminho define atributo e ritmo de aprendizado. */
    val ARCANISTA = CatalogClass(
        "arcanista", "Arcanista", 36, 8, 2, 6, null, null,
        fixedSkills = setOf(MISTICISMO, VONTADE), fixedChoice = emptySet(),
        skillChoiceCount = 2,
        skillChoiceList = setOf(CONHECIMENTO, DIPLOMACIA, ENGANACAO, GUERRA, INICIATIVA, INTIMIDACAO, INTUICAO, INVESTIGACAO,
            NOBREZA, OFICIO, PERCEPCAO),
        proficiencies = "Nenhuma",
        paths = listOf(
            ClassPath(
                "bruxo", "Bruxo",
                "Lança magias por meio de um foco (varinha, cajado...) empunhado; sem ele, precisa de teste de Misticismo. Inteligência.",
                arcanistCasting(INTELIGENCIA, 3, SpellProgression.EVERY_LEVEL),
            ),
            ClassPath(
                "feiticeiro", "Feiticeiro",
                "Poder inato de uma linhagem (veja p. 39), mas aprende magias só nos níveis ímpares. Carisma.",
                arcanistCasting(CARISMA, 3, SpellProgression.ODD_LEVELS),
            ),
            ClassPath(
                "mago", "Mago",
                "Estuda um grimório e só lança as magias memorizadas (metade das que conhece); começa com uma magia a mais. Inteligência.",
                arcanistCasting(INTELIGENCIA, 4, SpellProgression.EVERY_LEVEL_PLUS_CIRCLE),
            ),
        ),
    )

    val ALL = listOf(ARCANISTA, BARDO, BUCANEIRO, DRUIDA, GUERREIRO)

    fun byId(id: String?): CatalogClass? = ALL.firstOrNull { it.id == id }
}

// ============================== Raças ==============================

data class CatalogRace(
    val id: String,
    val name: String,
    val page: Int,
    /** Como a raça muda os atributos, para o jogador conferir os valores finais. */
    val attributeText: String,
    val speed: Int,
    val abilities: List<SheetEntry>,
    val skillBonuses: Map<Skill, List<SheetBonus>> = emptyMap(),
    val mpBonuses: List<SheetBonus> = emptyList(),
    val defenseBonuses: List<SheetBonus> = emptyList(),
    /** Ajustes fixos de atributo (ex.: Dahllan Sab +2, Des +1, Int –1). */
    val fixedAttributes: Map<Attribute, Int> = emptyMap(),
    /** Quantos atributos diferentes o jogador escolhe para receber +1 (Humano e Lefou: 3). */
    val attributeChoiceCount: Int = 0,
    val attributeChoiceExcluded: Set<Attribute> = emptySet(),
    /** Perícias treinadas à escolha (Humano, Versátil: 2). */
    val trainedSkillChoices: Int = 0,
    /** Perícias à escolha que recebem +2 (Lefou, Deformidade: 2). */
    val bonusSkillChoices: Int = 0,
)

object RaceCatalog {
    /** p. 21 */
    val DAHLLAN = CatalogRace(
        "dahllan", "Dahllan", 21, "Sabedoria +2, Destreza +1, Inteligência –1.", 9,
        fixedAttributes = mapOf(SABEDORIA to 2, DESTREZA to 1, INTELIGENCIA to -1),
        abilities = listOf(
            SheetEntry("Amiga das Plantas", "Pode lançar Controlar Plantas (Sabedoria); se aprender de novo, ela custa –1 PM."),
            SheetEntry("Armadura de Allihanna", "Ação de movimento e 1 PM: +2 na Defesa até o fim da cena."),
            SheetEntry("Empatia Selvagem", "Comunica-se com animais e usa Adestramento com eles; se receber de novo, +2 em Adestramento."),
        ),
        defenseBonuses = listOf(
            SheetBonus("Armadura de Allihanna", 2, situation = "quando ativada (ação de movimento + 1 PM, até o fim da cena)"),
        ),
    )

    /** p. 22 */
    val ELFO = CatalogRace(
        "elfo", "Elfo", 22, "Inteligência +2, Destreza +1, Constituição –1.", 12,
        fixedAttributes = mapOf(INTELIGENCIA to 2, DESTREZA to 1, CONSTITUICAO to -1),
        abilities = listOf(
            SheetEntry("Graça de Glórienn", "Deslocamento de 12m."),
            SheetEntry("Sangue Mágico", "+1 PM por nível."),
            SheetEntry("Sentidos Élficos", "Visão na penumbra e +2 em Misticismo e Percepção."),
        ),
        skillBonuses = mapOf(
            MISTICISMO to listOf(SheetBonus("Sentidos Élficos", 2)),
            PERCEPCAO to listOf(SheetBonus("Sentidos Élficos", 2)),
        ),
        mpBonuses = listOf(SheetBonus("Sangue Mágico", 1, perLevel = true)),
    )

    /** p. 19 */
    val HUMANO = CatalogRace(
        "humano", "Humano", 19, "+1 em três atributos diferentes, à sua escolha.", 9,
        attributeChoiceCount = 3,
        trainedSkillChoices = 2,
        abilities = listOf(
            SheetEntry("Versátil", "Treinado em duas perícias à escolha (de qualquer classe); pode trocar uma delas por um poder geral."),
        ),
    )

    /** p. 24 */
    val LEFOU = CatalogRace(
        "lefou", "Lefou", 24, "+1 em três atributos diferentes (exceto Carisma) e Carisma –1.", 9,
        fixedAttributes = mapOf(CARISMA to -1),
        attributeChoiceCount = 3,
        attributeChoiceExcluded = setOf(CARISMA),
        bonusSkillChoices = 2,
        abilities = listOf(
            SheetEntry("Cria da Tormenta", "É do tipo monstro e recebe +5 nos testes de resistência contra efeitos de lefeu e da Tormenta."),
            SheetEntry("Deformidade", "+2 em duas perícias à escolha (cada bônus conta como poder da Tormenta); pode trocar um bônus por um poder da Tormenta."),
        ),
        skillBonuses = listOf(FORTITUDE, REFLEXOS, VONTADE).associateWith {
            listOf(SheetBonus("Cria da Tormenta", 5, situation = "contra efeitos de lefeu e da Tormenta"))
        },
    )

    /** p. 26 */
    val QAREEN = CatalogRace(
        "qareen", "Qareen", 26, "Carisma +2, Inteligência +1, Sabedoria –1.", 9,
        fixedAttributes = mapOf(CARISMA to 2, INTELIGENCIA to 1, SABEDORIA to -1),
        abilities = listOf(
            SheetEntry("Desejos", "Se lançar uma magia que alguém pediu desde o seu último turno, ela custa –1 PM. Pedir é uma ação livre."),
            SheetEntry("Resistência Elemental", "Redução 10 a um tipo de dano, conforme a ascendência: frio, eletricidade, fogo, ácido, luz ou trevas."),
            SheetEntry("Tatuagem Mística", "Pode lançar uma magia de 1º círculo à escolha (Carisma); se aprender de novo, ela custa –1 PM."),
        ),
    )

    val ALL = listOf(DAHLLAN, ELFO, HUMANO, LEFOU, QAREEN)

    fun byId(id: String?): CatalogRace? = ALL.firstOrNull { it.id == id }
}

// ============================== Origens ==============================

data class CatalogOrigin(
    val id: String,
    val name: String,
    val page: Int,
    val skillOptions: Set<Skill>,
    /** Poderes oferecidos; os que não estão no catálogo aparecem só como texto (ex.: "um poder de combate"). */
    val powerOptions: List<String>,
    val items: String,
) {
    /** Uma origem concede dois benefícios entre perícias e poderes (p. 85). */
    val benefitCount: Int get() = 2
}

object OriginCatalog {
    val ARTISTA = CatalogOrigin("artista", "Artista", 87, setOf(ATUACAO, ENGANACAO),
        listOf("Atraente", "Dom Artístico", "Sortudo", "Torcida"), "Estojo de disfarces ou um instrumento musical à escolha.")
    val MARUJO = CatalogOrigin("marujo", "Marujo", 92, setOf(ATLETISMO, JOGATINA, PILOTAGEM),
        listOf("Acrobático", "Passagem de Navio"), "T$ 2d6 (último salário) e corda.")
    val SELVAGEM = CatalogOrigin("selvagem", "Selvagem", 94, setOf(PERCEPCAO, REFLEXOS, SOBREVIVENCIA),
        listOf("Lobo Solitário", "Vida Rústica", "Vitalidade"), "Uma arma simples e um pequeno animal de estimação.")
    val SOLDADO = CatalogOrigin("soldado", "Soldado", 94, setOf(FORTITUDE, GUERRA, LUTA, PONTARIA),
        listOf("Influência Militar", "um poder de combate à escolha"), "Uma arma marcial, um uniforme militar e uma insígnia do seu exército.")

    val ASSISTENTE_DE_LABORATORIO = CatalogOrigin("assistente", "Assistente de Laboratório", 87,
        setOf(OFICIO, MISTICISMO),
        listOf("Esse Cheiro...", "Venefício", "um poder da Tormenta à escolha"),
        "Instrumentos de Ofício (alquimista).")

    val ALL = listOf(ARTISTA, ASSISTENTE_DE_LABORATORIO, MARUJO, SELVAGEM, SOLDADO)

    fun byId(id: String?): CatalogOrigin? = ALL.firstOrNull { it.id == id }
}
