package com.jovis.t20.rules

import com.jovis.t20.rules.Skill.*

/*
 * Poderes, divindades e habilidades de classe do catálogo.
 * Os resumos são textos curtos escritos para o app; a regra completa está na página indicada.
 */

/** Efeitos que o app aplica sozinho na ficha quando o poder é escolhido. */
data class PowerEffects(
    val skills: Map<Skill, Int> = emptyMap(),
    val defense: Int = 0,
    /** PV extras por nível de personagem (ex.: Vitalidade). */
    val hpPerLevel: Int = 0,
    /** PM extras por nível (ex.: Poder Mágico). */
    val mpPerLevel: Int = 0,
    /** Magia do catálogo que o poder ensina. */
    val spell: String? = null,
)

data class CatalogPower(
    val name: String,
    val page: Int,
    val summary: String,
    val effects: PowerEffects = PowerEffects(),
)

object PowerCatalog {
    /** Poderes concedidos pelas divindades (p. 132-136). */
    val GRANTED: List<CatalogPower> = listOf(
        CatalogPower("Afinidade com a Tormenta", 132, "+10 nos testes de resistência contra a Tormenta, suas criaturas e devotos de Aharadak; o primeiro poder da Tormenta não reduz Carisma."),
        CatalogPower("Almejar o Impossível", 132, "Em testes de perícia, tirar 19 ou 20 no dado é sempre um sucesso."),
        CatalogPower("Anfíbio", 132, "Respira debaixo d'água e nada com o mesmo deslocamento que anda."),
        CatalogPower("Apostar com o Trapaceiro", 132, "Por 1 PM, você e o mestre rolam um d20 num teste; você escolhe ficar com o seu ou com o dele, ainda oculto."),
        CatalogPower("Armas da Ambição", 132, "+1 nos ataques e na margem de ameaça com armas em que é proficiente."),
        CatalogPower("Arsenal das Profundezas", 132, "+2 no dano e +1 no multiplicador de crítico com azagaia, lança e tridente."),
        CatalogPower("Astúcia da Serpente", 132, "+2 em Enganação, Furtividade e Intuição.", PowerEffects(skills = mapOf(ENGANACAO to 2, FURTIVIDADE to 2, INTUICAO to 2))),
        CatalogPower("Ataque Piedoso", 132, "Causa dano não letal com armas corpo a corpo sem a penalidade de –5."),
        CatalogPower("Aura Restauradora", 132, "Curas suas e de aliados a até 9m recuperam +1 PV por dado."),
        CatalogPower("Aura de Medo", 132, "Por 2 PM, cria uma aura de 9m que deixa inimigos abalados (Vontade evita)."),
        CatalogPower("Aura de Paz", 132, "Por 2 PM, cria uma aura de 9m; inimigos que tentarem agir contra você podem perder a ação (Vontade evita)."),
        CatalogPower("Bênção do Mana", 132, "+1 PM a cada nível ímpar."),
        CatalogPower("Carícia Sombria", 132, "Por 1 PM, um toque de trevas causa 2d6 de dano e você recupera metade em PV."),
        CatalogPower("Centelha Mágica", 132, "Aprende uma magia arcana ou divina de 1º círculo à escolha."),
        CatalogPower("Compreender os Ermos", 132, "+2 em Sobrevivência e pode usar Sabedoria em Adestramento.", PowerEffects(skills = mapOf(SOBREVIVENCIA to 2))),
        CatalogPower("Conhecimento Enciclopédico", 132, "Treinado em duas perícias de Inteligência à escolha."),
        CatalogPower("Conjurar Arma", 132, "Por 1 PM, invoca uma arma mágica (+1 em ataque e dano) que dura a cena."),
        CatalogPower("Coragem Total", 132, "Imune a efeitos de medo, mágicos ou não."),
        CatalogPower("Cura Gentil", 133, "Soma seu Carisma aos PV curados por suas magias."),
        CatalogPower("Curandeira Perfeita", 133, "Pode escolher 10 em Cura, não precisa de maleta e ganha bônus se tiver uma."),
        CatalogPower("Dedo Verde", 132, "Aprende Controlar Plantas; se aprender de novo, custa –1 PM.", PowerEffects(spell = "Controlar Plantas")),
        CatalogPower("Descanso Natural", 133, "Dormir ao relento conta como descanso confortável."),
        CatalogPower("Dom da Esperança", 133, "Soma Sabedoria (no lugar de Constituição) nos PV e é imune a alquebrado, esmorecido e frustrado."),
        CatalogPower("Dom da Imortalidade", 133, "Ao morrer, volta à vida após 3d6 dias. Só para paladinos."),
        CatalogPower("Dom da Profecia", 133, "Pode lançar Augúrio e, por 2 PM, ganhar +2 em um teste."),
        CatalogPower("Dom da Ressurreição", 133, "Gastando todos os PM, ressuscita uma criatura morta há menos de um ano. Só para clérigos."),
        CatalogPower("Dom da Verdade", 133, "Por 2 PM, +5 em Intuição e em Percepção contra mentiras e furtividade até o fim da cena."),
        CatalogPower("Escamas Dracônicas", 133, "+2 na Defesa e em Fortitude.", PowerEffects(skills = mapOf(FORTITUDE to 2), defense = 2)),
        CatalogPower("Escudo Mágico", 133, "Ao lançar uma magia, ganha bônus na Defesa igual ao círculo dela até o próximo turno."),
        CatalogPower("Espada Justiceira", 133, "Por 1 PM, o dano da sua arma de corte aumenta um passo até o fim da cena."),
        CatalogPower("Espada Solar", 133, "Por 1 PM, sua arma de corte causa +1d6 de fogo até o fim da cena."),
        CatalogPower("Familiar Ofídico", 133, "Ganha uma cobra como familiar, que não conta no limite de parceiros."),
        CatalogPower("Farsa do Fingidor", 133, "Aprende Criar Ilusão; se aprender de novo, custa –1 PM.", PowerEffects(spell = "Criar Ilusão")),
        CatalogPower("Forma de Macaco", 133, "Por 2 PM, vira um macaco minúsculo (+5 em Furtividade, escala), até atacar ou sofrer dano."),
        CatalogPower("Fulgor Solar", 134, "Redução de frio e trevas 5; por 1 PM, ofusca quem atacar você."),
        CatalogPower("Fé Guerreira", 133, "Usa Sabedoria em Guerra e, por 2 PM, troca um teste em combate por Guerra."),
        CatalogPower("Fúria Divina", 134, "Por 2 PM, +2 em ataque e dano corpo a corpo na cena, mas sem ações de concentração."),
        CatalogPower("Golpista Divino", 133, "+2 em Enganação, Jogatina e Ladinagem.", PowerEffects(skills = mapOf(ENGANACAO to 2, JOGATINA to 2, LADINAGEM to 2))),
        CatalogPower("Habitante do Deserto", 134, "Redução de fogo 10; por 1 PM, cria água potável."),
        CatalogPower("Inimigo de Tenebra", 133, "+1d6 de dano contra mortos-vivos e dobra o alcance de efeitos de luz."),
        CatalogPower("Kiai Divino", 133, "Uma vez por rodada, por 3 PM, um ataque corpo a corpo que acerte causa dano máximo."),
        CatalogPower("Liberdade Divina", 134, "Por 2 PM, fica imune a efeitos de movimento por uma rodada."),
        CatalogPower("Manto da Penumbra", 133, "Aprende Escuridão; se aprender de novo, custa –1 PM.", PowerEffects(spell = "Escuridão")),
        CatalogPower("Mente Analítica", 134, "+2 em Intuição, Investigação e Vontade.", PowerEffects(skills = mapOf(INTUICAO to 2, INVESTIGACAO to 2, VONTADE to 2))),
        CatalogPower("Mente Vazia", 133, "+2 em Iniciativa, Percepção e Vontade.", PowerEffects(skills = mapOf(INICIATIVA to 2, PERCEPCAO to 2, VONTADE to 2))),
        CatalogPower("Mestre dos Mares", 134, "Fala com animais aquáticos e aprende Acalmar Animal (só contra eles).", PowerEffects(spell = "Acalmar Animal")),
        CatalogPower("Olhar Amedrontador", 133, "Aprende Amedrontar; se aprender de novo, custa –1 PM.", PowerEffects(spell = "Amedrontar")),
        CatalogPower("Palavras de Bondade", 133, "Aprende Enfeitiçar; se aprender de novo, custa –1 PM.", PowerEffects(spell = "Enfeitiçar")),
        CatalogPower("Percepção Temporal", 134, "Por 3 PM, soma Sabedoria aos ataques, à Defesa e a Reflexos até o fim da cena."),
        CatalogPower("Pesquisa Abençoada", 134, "Com uma hora de pesquisa, refaz um teste recente de Inteligência ou Sabedoria."),
        CatalogPower("Poder Oculto", 134, "Por 2 PM, ganha +2 aleatório em Força, Destreza ou Constituição até o fim da cena."),
        CatalogPower("Presas Primordiais", 135, "Por 1 PM, ganha uma mordida (1d6) e pode atacar com ela além de outra arma."),
        CatalogPower("Presas Venenosas", 135, "Por 1 PM, envenena sua arma: o próximo acerto causa perda de 1d12 PV."),
        CatalogPower("Rejeição Divina", 135, "Resistência a magia divina +5."),
        CatalogPower("Reparar Injustiça", 135, "Uma vez por rodada, por 2 PM, faz um inimigo próximo repetir um ataque que acertou e ficar com o pior."),
        CatalogPower("Sangue Ofídico", 134, "Resistência a veneno +5 e seus venenos ficam +2 mais difíceis de resistir."),
        CatalogPower("Sangue de Ferro", 134, "Por 3 PM, +2 no dano e redução de dano 5 até o fim da cena."),
        CatalogPower("Servos do Dragão", 135, "Por 2 PM, invoca kobolds capangas que lutam por você na cena."),
        CatalogPower("Sopro do Mar", 135, "Por 1 PM, um cone de vento marinho causa 2d6 de frio."),
        CatalogPower("Sorte dos Loucos", 135, "Por 1 PM, rola um teste de novo (várias vezes), mas perde PM se ainda falhar."),
        CatalogPower("Talento Artístico", 135, "+2 em Acrobacia, Atuação e Diplomacia.", PowerEffects(skills = mapOf(ACROBACIA to 2, ATUACAO to 2, DIPLOMACIA to 2))),
        CatalogPower("Teurgista Místico", 135, "Uma magia por círculo pode ser do outro tipo (arcana ou divina). Requer a habilidade Magias."),
        CatalogPower("Tradição de Lin-Wu", 135, "A katana conta como arma simples; com proficiência marcial, +1 na margem de ameaça dela."),
        CatalogPower("Transmissão da Loucura", 135, "Pode lançar Sussurros Insanos."),
        CatalogPower("Tropas Duyshidakk", 135, "Por 2 PM, invoca goblinoides capangas que lutam por você na cena."),
        CatalogPower("Urro Divino", 136, "Por 1 PM, soma sua Constituição ao dano de um ataque ou magia."),
        CatalogPower("Visão nas Trevas", 136, "Enxerga perfeitamente no escuro, inclusive na escuridão mágica."),
        CatalogPower("Voz da Civilização", 136, "Está sempre sob o efeito de Compreensão."),
        CatalogPower("Voz da Natureza", 136, "Fala com animais e aprende Acalmar Animal (só contra animais).", PowerEffects(spell = "Acalmar Animal")),
        CatalogPower("Voz dos Monstros", 136, "Conhece os idiomas dos monstros e se comunica até com os não inteligentes."),
        CatalogPower("Zumbificar", 136, "Por 3 PM, reanima um cadáver como parceiro por um dia."),
        CatalogPower("Êxtase da Loucura", 133, "Ganha PM temporários quando criaturas falham em Vontade contra suas habilidades mágicas."),
    )

    /** Poderes oferecidos pelas origens da mesa (gerais e únicos). */
    val ORIGIN: List<CatalogPower> = listOf(
        CatalogPower("Acrobático", 129, "Usa Destreza em Atletismo e terreno difícil não atrapalha seu deslocamento. Pré-requisito: Des 2."),
        CatalogPower("Atraente", 130, "+2 em perícias de Carisma contra quem se sinta atraído por você. Pré-requisito: Car 1."),
        CatalogPower("Dom Artístico", 87, "+2 em Atuação e o dobro de tibares em apresentações.", PowerEffects(skills = mapOf(ATUACAO to 2))),
        CatalogPower("Esse Cheiro...", 88, "+2 em Fortitude e percebe itens alquímicos por perto.", PowerEffects(skills = mapOf(FORTITUDE to 2))),
        CatalogPower("Influência Militar", 94, "Uma vez por aventura, um antigo comandante aparece para ajudar por uma cena (parceiro mestre)."),
        CatalogPower("Lobo Solitário", 130, "+1 em perícias e Defesa sem aliados por perto; usa Cura em si mesmo sem penalidade."),
        CatalogPower("Passagem de Navio", 92, "Consegue viagem por mar para você e o grupo em troca de trabalho a bordo."),
        CatalogPower("Sortudo", 130, "Por 3 PM, rola um teste de novo."),
        CatalogPower("Torcida", 130, "+2 em perícias e Defesa quando há uma torcida a seu favor. Pré-requisito: Car 1."),
        CatalogPower("Venefício", 131, "Não se envenena por acidente e seus venenos ficam +2 mais difíceis de resistir. Pré-requisito: treinado em Ofício (alquimista)."),
        CatalogPower("Vida Rústica", 94, "Imune a efeitos ruins de comida e bebida; ao dormir mal, recupera pelo menos o seu nível em PV e PM."),
        CatalogPower("Vitalidade", 129, "+1 PV por nível e +2 em Fortitude. Pré-requisito: Con 1.", PowerEffects(skills = mapOf(FORTITUDE to 2), hpPerLevel = 1)),
    )

    fun granted(name: String): CatalogPower? = GRANTED.firstOrNull { it.name == name }
    fun origin(name: String): CatalogPower? = ORIGIN.firstOrNull { it.name == name }
    fun any(name: String): CatalogPower? = granted(name) ?: origin(name)
}

// ============================== Divindades ==============================

data class CatalogDeity(
    val id: String,
    val name: String,
    val page: Int,
    /** Como o livro descreve quem pode ser devoto. */
    val devotesText: String,
    /** Raças e classes do catálogo que podem ser devotas (por id). */
    val devoteRaces: Set<String>,
    val devoteClasses: Set<String>,
    /** Qualquer personagem pode ser devoto (Aharadak, Valkaria, Thwor). */
    val anyone: Boolean = false,
    /** Resumo das obrigações e restrições. */
    val obligations: String,
    val powers: List<String>,
)

object DeityCatalog {
    private fun d(
        id: String, name: String, page: Int, devotesText: String, races: Set<String>, classes: Set<String>,
        obligations: String, powers: List<String>, anyone: Boolean = false,
    ) = CatalogDeity(id, name, page, devotesText, races, classes, anyone, obligations, powers)

    val ALL: List<CatalogDeity> = listOf(
        d("aharadak", "Aharadak", 96, "Quaisquer.", emptySet(), emptySet(),
            "No início de cenas de ação, rola 1d6 e, com resultado ímpar, pode ser tomado pela loucura da Tormenta.",
            listOf("Afinidade com a Tormenta", "Êxtase da Loucura", "Percepção Temporal", "Rejeição Divina"), anyone = true),
        d("allihanna", "Allihanna", 97, "Dahllan, elfos, sílfides, bárbaros, caçadores, druidas.",
            setOf("dahllan", "elfo"), setOf("druida"),
            "Não usa armaduras nem escudos de metal e não recupera PV ou PM descansando em comunidades maiores que uma aldeia.",
            listOf("Compreender os Ermos", "Dedo Verde", "Descanso Natural", "Voz da Natureza")),
        d("arsenal", "Arsenal", 98, "Anões, minotauros, bárbaros, cavaleiros, guerreiros, lutadores.",
            emptySet(), setOf("guerreiro"),
            "Não pode ser derrotado em combate ou disputa; a derrota do grupo também conta.",
            listOf("Conjurar Arma", "Coragem Total", "Fé Guerreira", "Sangue de Ferro")),
        d("azgher", "Azgher", 98, "Aggelus, qareen, arcanistas, bárbaros, caçadores, cavaleiros, guerreiros, nobres, paladinos.",
            setOf("qareen"), setOf("arcanista", "guerreiro"),
            "Mantém o rosto sempre coberto e doa à igreja 20% dos tesouros obtidos, em ouro.",
            listOf("Espada Solar", "Fulgor Solar", "Habitante do Deserto", "Inimigo de Tenebra")),
        d("hyninn", "Hyninn", 98, "Hynne, goblins, sílfides, bardos, bucaneiros, ladinos, inventores, nobres.",
            emptySet(), setOf("bardo", "bucaneiro"),
            "Não recusa golpes e trapaças (exceto contra aliados) e faz um ato ousado ou proibido por dia.",
            listOf("Apostar com o Trapaceiro", "Farsa do Fingidor", "Forma de Macaco", "Golpista Divino")),
        d("kallyadranoch", "Kallyadranoch", 99, "Elfos, medusas, sulfure, arcanistas, cavaleiros, guerreiros, lutadores, nobres.",
            setOf("elfo"), setOf("arcanista", "guerreiro"),
            "Para subir de nível, oferece um tesouro de valor ligado à riqueza do novo nível.",
            listOf("Aura de Medo", "Escamas Dracônicas", "Presas Primordiais", "Servos do Dragão")),
        d("khalmyr", "Khalmyr", 99, "Aggelus, anões, cavaleiros, guerreiros, nobres, paladinos.",
            emptySet(), setOf("guerreiro"),
            "Não recusa ajuda a inocentes, obedece aos superiores da igreja e só usa itens mágicos permanentes feitos por devotos do deus.",
            listOf("Coragem Total", "Dom da Verdade", "Espada Justiceira", "Reparar Injustiça")),
        d("lena", "Lena", 100, "Dahllan, qareen, nobres, paladinos.",
            setOf("dahllan", "qareen"), emptySet(),
            "Não causa dano letal nem perda de PV a criaturas vivas (nem dá bônus nesse tipo de dano).",
            listOf("Ataque Piedoso", "Aura Restauradora", "Cura Gentil", "Curandeira Perfeita")),
        d("lin-wu", "Lin-Wu", 101, "Anões, cavaleiros, guerreiros, nobres, paladinos.",
            emptySet(), setOf("guerreiro"),
            "Age com honra: não tenta nada que exija Enganação, Furtividade ou Ladinagem.",
            listOf("Coragem Total", "Kiai Divino", "Mente Vazia", "Tradição de Lin-Wu")),
        d("marah", "Marah", 101, "Aggelus, elfos, hynne, qareen, bardos, nobres, paladinos.",
            setOf("elfo", "qareen"), setOf("bardo"),
            "Não causa dano, perda de PV nem condições (exceto enfeitiçado, fascinado e pasmo); em combate, só protege, cura, foge ou se rende.",
            listOf("Aura de Paz", "Dom da Esperança", "Palavras de Bondade", "Talento Artístico")),
        d("megalokk", "Megalokk", 102, "Goblins, medusas, minotauros, sulfure, trogs, bárbaros, caçadores, druidas, lutadores.",
            emptySet(), setOf("druida"),
            "Não usa perícias de Inteligência ou Carisma (exceto Adestramento e Intimidação) nem prepara ações, escolhe 10/20 ou sustenta efeitos.",
            listOf("Olhar Amedrontador", "Presas Primordiais", "Urro Divino", "Voz dos Monstros")),
        d("nimb", "Nimb", 102, "Goblins, qareen, sílfides, arcanistas, bárbaros, bardos, bucaneiros, inventores, ladinos.",
            setOf("qareen"), setOf("arcanista", "bardo", "bucaneiro"),
            "Sofre –5 em perícias de Carisma e, no início de cenas de ação, rola 1d6 para efeitos de loucura.",
            listOf("Êxtase da Loucura", "Poder Oculto", "Sorte dos Loucos", "Transmissão da Loucura")),
        d("oceano", "Oceano", 103, "Dahllan, hynne, minotauros, sereias/tritões, bárbaros, bucaneiros, caçadores, druidas.",
            setOf("dahllan"), setOf("bucaneiro", "druida"),
            "Só usa azagaia, lança, tridente e rede, apenas armaduras leves, e não fica mais de um mês longe do mar.",
            listOf("Anfíbio", "Arsenal das Profundezas", "Mestre dos Mares", "Sopro do Mar")),
        d("sszzaas", "Sszzaas", 103, "Medusas, arcanistas, bardos, bucaneiros, inventores, ladinos, nobres.",
            emptySet(), setOf("arcanista", "bardo", "bucaneiro"),
            "Faz um ato de traição, intriga ou corrupção por dia.",
            listOf("Astúcia da Serpente", "Familiar Ofídico", "Presas Venenosas", "Sangue Ofídico")),
        d("tanna-toh", "Tanna-Toh", 103, "Golens, kliren, arcanistas, bardos, inventores, nobres, paladinos.",
            emptySet(), setOf("arcanista", "bardo"),
            "Não recusa missões em busca de conhecimento, sempre diz a verdade e responde a perguntas diretas.",
            listOf("Conhecimento Enciclopédico", "Mente Analítica", "Pesquisa Abençoada", "Voz da Civilização")),
        d("tenebra", "Tenebra", 104, "Anões, medusas, qareen, osteon, sulfure, trogs, arcanistas, bardos, ladinos.",
            setOf("qareen"), setOf("arcanista", "bardo"),
            "Durante o dia, cobre o corpo inteiro para não ser tocado pelo sol.",
            listOf("Carícia Sombria", "Manto da Penumbra", "Visão nas Trevas", "Zumbificar")),
        d("thwor", "Thwor", 104, "Qualquer duyshidakk (aceito pelo povo goblinoide).", emptySet(), emptySet(),
            "Precisa ser aceito pelo povo goblinoide, apoiar seu projeto para o continente e só lutar contra goblinoides em último caso.",
            listOf("Almejar o Impossível", "Fúria Divina", "Olhar Amedrontador", "Tropas Duyshidakk"), anyone = true),
        d("thyatis", "Thyatis", 105, "Aggelus, cavaleiros, inventores, lutadores, paladinos.",
            emptySet(), emptySet(),
            "Não mata criaturas inteligentes; pode feri-las, mas nunca levá-las à morte.",
            listOf("Ataque Piedoso", "Dom da Imortalidade", "Dom da Profecia", "Dom da Ressurreição")),
        d("valkaria", "Valkaria", 105, "Aventureiros: membros de todas as classes.", emptySet(), emptySet(),
            "Não fixa moradia (pouco tempo em cada cidade e reino) nem se casa ou forma união estável.",
            listOf("Almejar o Impossível", "Armas da Ambição", "Coragem Total", "Liberdade Divina"), anyone = true),
        d("wynna", "Wynna", 105, "Elfos, golens, qareen, sílfides, arcanistas, bardos.",
            setOf("elfo", "qareen"), setOf("arcanista", "bardo"),
            "Não recusa ajuda a inocentes nem mata seres mágicos (elfos, qareen, sílfides...) ou conjuradores arcanos.",
            listOf("Bênção do Mana", "Centelha Mágica", "Escudo Mágico", "Teurgista Místico")),
    )

    fun byId(id: String?): CatalogDeity? = ALL.firstOrNull { it.id == id }

    /**
     * Pode ser devoto se a raça ou a classe estiver na lista do deus; humanos e clérigos podem seguir qualquer um (p. 96).
     * Raças ou classes fora do catálogo não são bloqueadas, porque o app não conhece a regra delas.
     */
    fun canBeDevote(deity: CatalogDeity, raceId: String?, classId: String?): Boolean {
        if (deity.anyone || raceId == "humano" || classId == "clerigo") return true
        if (raceId == null || classId == null) return true
        return raceId in deity.devoteRaces || classId in deity.devoteClasses
    }
}

// ============================== Habilidades de classe ==============================

data class ClassAbility(val level: Int, val name: String, val summary: String)

object ClassAbilities {
    private fun a(level: Int, name: String, summary: String) = ClassAbility(level, name, summary)

    val BY_CLASS: Map<String, List<ClassAbility>> = mapOf(
        "arcanista" to listOf(
            a(1, "Caminho do Arcanista", "Bruxo, feiticeiro ou mago: define como você lança magias e seu atributo-chave."),
            a(1, "Magias", "Lança magias arcanas de qualquer escola; novos círculos no 5º, 9º, 13º e 17º níveis."),
            a(20, "Alta Arcana", "Suas magias arcanas custam metade dos PM."),
        ),
        "bardo" to listOf(
            a(1, "Inspiração", "Ação padrão e 2 PM: você e aliados próximos ganham +1 em testes de perícia na cena; melhora a cada quatro níveis."),
            a(1, "Magias", "Lança magias arcanas de três escolas escolhidas; novos círculos no 6º, 10º e 14º níveis."),
            a(2, "Eclético", "Por 1 PM, age como treinado em qualquer perícia num teste."),
            a(20, "Artista Completo", "Inspiração vira ação livre e, sob ela, suas habilidades de bardo custam metade dos PM."),
        ),
        "bucaneiro" to listOf(
            a(1, "Audácia", "Por 2 PM, soma seu Carisma a um teste de perícia (exceto ataques)."),
            a(1, "Insolência", "Soma seu Carisma na Defesa, limitado pelo nível, sem armadura pesada."),
            a(2, "Evasão", "Passando em Reflexos contra dano reduzido à metade, não sofre dano nenhum."),
            a(3, "Esquiva Sagaz", "+1 na Defesa e em Reflexos, aumentando a cada quatro níveis, sem armadura pesada."),
            a(5, "Panache", "Recupera 1 PM ao fazer um acerto crítico ou derrubar um inimigo a 0 PV."),
            a(10, "Evasão Aprimorada", "Falhando em Reflexos contra esses efeitos, sofre só metade do dano."),
            a(20, "Sorte de Nimb", "Por 5 PM, rola um teste de novo; 11 ou mais conta como 20 natural."),
        ),
        "druida" to listOf(
            a(1, "Devoto Fiel", "Devoto de um deus maior, com dois poderes concedidos em vez de um."),
            a(1, "Empatia Selvagem", "Comunica-se com animais e usa Adestramento com eles; se receber de novo, +2 em Adestramento."),
            a(1, "Magias", "Lança magias divinas de três escolas escolhidas; novos círculos no 6º, 10º e 14º níveis."),
            a(2, "Caminho dos Ermos", "Atravessa terreno difícil natural sem perder deslocamento; rastreá-lo fica +10 mais difícil."),
            a(20, "Força da Natureza", "Suas magias custam –2 PM e têm CD +2 (o dobro em terrenos naturais)."),
        ),
        "guerreiro" to listOf(
            a(1, "Ataque Especial", "Por 1 PM, +4 no ataque ou no dano; melhora a cada quatro níveis."),
            a(3, "Durão", "Por 3 PM, reduz à metade um dano sofrido."),
            a(6, "Ataque Extra", "Ao agredir, por 2 PM faz um ataque adicional uma vez por rodada."),
            a(20, "Campeão", "O dano dos seus ataques aumenta um passo e acertos com Ataque Especial devolvem metade dos PM."),
        ),
    )

    fun of(classId: String?): List<ClassAbility> = BY_CLASS[classId].orEmpty()

    /** Quantos poderes concedidos a classe recebe ao ser devota. */
    fun grantedPowerCount(classId: String?): Int = if (classId == "druida" || classId == "clerigo") 2 else 1
}
