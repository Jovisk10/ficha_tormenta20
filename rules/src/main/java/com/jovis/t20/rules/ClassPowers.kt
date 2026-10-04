package com.jovis.t20.rules

import com.jovis.t20.rules.Attribute.*
import com.jovis.t20.rules.Skill.*

/** Pré-requisitos de um poder, numa forma que o app consegue conferir. */
data class PowerRequirement(
    val attributes: Map<Attribute, Int> = emptyMap(),
    /** Poderes de classe exigidos (todos). */
    val powers: List<String> = emptyList(),
    /** Nível mínimo na classe. */
    val level: Int = 0,
    val trained: Set<Skill> = emptySet(),
    /** Caminhos aceitos (arcanista); vazio = qualquer. */
    val paths: Set<String> = emptySet(),
    /** Magia que o personagem precisa conhecer. */
    val spell: String? = null,
    /** Como o livro descreve o pré-requisito. */
    val text: String = "",
)

data class ClassPowerDef(
    val name: String,
    val page: Int,
    val summary: String,
    val requirement: PowerRequirement = PowerRequirement(),
    /** Pode ser escolhido mais de uma vez. */
    val repeatable: Boolean = false,
    val effects: PowerEffects = PowerEffects(),
) {
    fun asPower(): CatalogPower = CatalogPower(name, page, summary, effects)
}

/** Poderes de classe do Tormenta 20 Jogo do Ano. Resumos escritos para o app; regra completa na página. */
object ClassPowerCatalog {
    val BY_CLASS: Map<String, List<ClassPowerDef>> = mapOf(
        "arcanista" to listOf(
            ClassPowerDef("Arcano de Batalha", 38, "Soma seu atributo-chave ao dano das suas magias."),
            ClassPowerDef("Aumento de Atributo", 38, "Recebe +1 em um atributo (registre em Atributos, nos aumentos por poderes). Uma vez por patamar para cada atributo.", repeatable = true),
            ClassPowerDef("Caldeirão do Bruxo", 38, "Cria poções como com Preparar Poção; com os dois, até 5º círculo.", PowerRequirement(paths = setOf("bruxo"), trained = setOf(OFICIO), text = "Bruxo, treinado em Ofício (alquimista)")),
            ClassPowerDef("Conhecimento Mágico", 38, "Aprende duas magias de qualquer círculo que possa lançar. Pode ser escolhido várias vezes.", repeatable = true),
            ClassPowerDef("Contramágica Aprimorada", 38, "Uma vez por rodada, faz contramágica como reação.", PowerRequirement(spell = "Dissipar Magia", text = "Dissipar Magia")),
            ClassPowerDef("Envolto em Mistério", 38, "+5 em Enganação e Intimidação contra quem não é treinado em Conhecimento ou Misticismo (a critério do mestre)."),
            ClassPowerDef("Escriba Arcano", 38, "Aprende magias copiando pergaminhos e grimórios, com tempo e custo por PM da magia.", PowerRequirement(paths = setOf("mago"), trained = setOf(OFICIO), text = "Mago, treinado em Ofício (escriba)")),
            ClassPowerDef("Especialista em Escola", 38, "Escolha uma escola: a CD das suas magias dela aumenta em +2.", PowerRequirement(paths = setOf("bruxo", "mago"), text = "Bruxo ou Mago"), repeatable = true),
            ClassPowerDef("Familiar", 38, "Um animal mágico com quem fala por telepatia; cada espécie dá um benefício (ex.: gato, visão no escuro e +2 em Furtividade)."),
            ClassPowerDef("Fluxo de Mana", 38, "Mantém dois efeitos sustentados com uma única ação livre.", PowerRequirement(level = 10, text = "10º nível de arcanista")),
            ClassPowerDef("Foco Vital", 38, "Segurando o foco, um dano que levaria você a 0 PV deixa você com 1 PV e o foco absorve o resto.", PowerRequirement(paths = setOf("bruxo"), text = "Bruxo")),
            ClassPowerDef("Fortalecimento Arcano", 38, "+1 na CD das suas magias (+2 se já lança magias de 4º círculo).", PowerRequirement(level = 5, text = "5º nível de arcanista")),
            ClassPowerDef("Herança Aprimorada", 38, "Recebe a herança aprimorada da sua linhagem.", PowerRequirement(paths = setOf("feiticeiro"), level = 6, text = "Feiticeiro, 6º nível de arcanista")),
            ClassPowerDef("Herança Superior", 38, "Recebe a herança superior da sua linhagem.", PowerRequirement(powers = listOf("Herança Aprimorada"), level = 11, text = "Herança Aprimorada, 11º nível de arcanista")),
            ClassPowerDef("Magia Pungente", 38, "Por 1 PM, aumenta em +2 a CD de uma magia."),
            ClassPowerDef("Mestre em Escola", 38, "Magias da escola escolhida custam –1 PM.", PowerRequirement(powers = listOf("Especialista em Escola"), level = 8, text = "Especialista em Escola (mesma escola), 8º nível de arcanista"), repeatable = true),
            ClassPowerDef("Poder Mágico", 38, "+1 PM por nível de arcanista.", effects = PowerEffects(mpPerLevel = 1)),
            ClassPowerDef("Raio Arcano", 39, "Ação padrão: um raio causa 1d8 de essência em alcance curto (+1d8 por círculo acima do 1º); Reflexos reduz à metade."),
            ClassPowerDef("Raio Elemental", 39, "Por 1 PM, o Raio Arcano causa ácido, eletricidade, fogo, frio ou trevas, com uma condição se o alvo falhar.", PowerRequirement(powers = listOf("Raio Arcano"), text = "Raio Arcano")),
            ClassPowerDef("Raio Poderoso", 39, "Os dados do Raio Arcano viram d12 e o alcance passa a médio.", PowerRequirement(powers = listOf("Raio Arcano"), text = "Raio Arcano")),
            ClassPowerDef("Tinta do Mago", 39, "Cria pergaminhos como com Escrever Pergaminho; com os dois, pela metade do custo.", PowerRequirement(paths = setOf("mago"), trained = setOf(OFICIO), text = "Mago, treinado em Ofício (escriba)")),
        ),
        "bardo" to listOf(
            ClassPowerDef("Arte Mágica", 44, "Sob Inspiração, a CD das suas habilidades de bardo aumenta em +2."),
            ClassPowerDef("Aumentar Repertório", 44, "Aprende duas magias que possa lançar, das suas escolas, arcanas ou divinas. Pode ser escolhido várias vezes.", repeatable = true),
            ClassPowerDef("Aumento de Atributo", 44, "Recebe +1 em um atributo (registre em Atributos, nos aumentos por poderes). Uma vez por patamar para cada atributo.", repeatable = true),
            ClassPowerDef("Dança das Lâminas", 44, "Ao lançar magia de ação padrão, por 1 PM faz um ataque corpo a corpo como ação livre.", PowerRequirement(powers = listOf("Esgrima Mágica"), level = 10, text = "Esgrima Mágica, 10º nível de bardo")),
            ClassPowerDef("Esgrima Mágica", 44, "Sob Inspiração, usa Atuação no lugar de Luta com armas leves ou de uma mão."),
            ClassPowerDef("Estrelato", 44, "É famoso: o bônus ao impressionar uma plateia com Atuação sobe para +5, mas é difícil passar despercebido.", PowerRequirement(level = 6, text = "6º nível de bardo")),
            ClassPowerDef("Fascinar em Massa", 44, "Por +2 PM, a Balada Fascinante afeta todas as criaturas escolhidas no alcance.", PowerRequirement(powers = listOf("Música: Balada Fascinante"), text = "Música: Balada Fascinante")),
            ClassPowerDef("Golpe Elemental", 45, "Sob Inspiração, por 1 PM um acerto corpo a corpo causa +1d6 de ácido, eletricidade, fogo ou frio (melhora com o nível).", PowerRequirement(powers = listOf("Golpe Mágico"), text = "Golpe Mágico")),
            ClassPowerDef("Golpe Mágico", 45, "Sob Inspiração, cada acerto corpo a corpo dá 2 PM temporários (máximo por cena igual ao nível).", PowerRequirement(powers = listOf("Esgrima Mágica"), text = "Esgrima Mágica")),
            ClassPowerDef("Inspiração Marcial", 45, "O bônus da Inspiração também vale nas rolagens de dano."),
            ClassPowerDef("Lendas e Histórias", 45, "Por 1 PM, rola de novo testes de Conhecimento, Misticismo, Nobreza ou Religião para informação e identificação.", PowerRequirement(attributes = mapOf(INTELIGENCIA to 1), text = "Int 1")),
            ClassPowerDef("Manipular", 45, "Por 1 PM, deixa enfeitiçada uma criatura fascinada por você (Vontade anula).", PowerRequirement(powers = listOf("Música: Balada Fascinante"), text = "Música: Balada Fascinante")),
            ClassPowerDef("Manipular em Massa", 45, "Por +2 PM, Manipular afeta todas as criaturas escolhidas em alcance curto.", PowerRequirement(powers = listOf("Fascinar em Massa", "Manipular"), level = 10, text = "Fascinar em Massa, Manipular, 10º nível de bardo")),
            ClassPowerDef("Melodia Restauradora", 45, "Por +2 PM, a Melodia Curativa também remove uma condição (como abalado, cego ou fatigado).", PowerRequirement(powers = listOf("Música: Melodia Curativa"), text = "Música: Melodia Curativa")),
            ClassPowerDef("Mestre dos Sussurros", 45, "Rola dois dados em Investigação para interrogar e Enganação para intriga, rápido e sem custo em ambientes sociais.", PowerRequirement(attributes = mapOf(CARISMA to 1), trained = setOf(ENGANACAO, INVESTIGACAO), text = "Car 1, treinado em Enganação e Investigação")),
            ClassPowerDef("Música: Balada Fascinante", 45, "Música (1 PM): Atuação contra Vontade deixa uma criatura fascinada enquanto você se concentra."),
            ClassPowerDef("Música: Canção Assustadora", 45, "Música (1 PM): Atuação contra Vontade deixa as criaturas escolhidas abaladas na cena."),
            ClassPowerDef("Música: Melodia Curativa", 45, "Música (1 PM): criaturas escolhidas recuperam 1d6 PV, +1d6 por PM extra."),
            ClassPowerDef("Paródia", 45, "Por 1 PM e um teste de Atuação, copia uma magia que viu ser lançada e pode lançá-la até o próximo turno."),
            ClassPowerDef("Prestidigitação", 45, "Com um teste de Atuação, lança uma magia como ação livre aproveitando os gestos de uma ação padrão.", PowerRequirement(level = 6, text = "6º nível de bardo")),
        ),
        "bucaneiro" to listOf(
            ClassPowerDef("Abusar dos Fracos", 47, "Contra criaturas com condição de medo, seu dano aumenta um passo.", PowerRequirement(powers = listOf("Flagelo dos Mares"), text = "Flagelo dos Mares")),
            ClassPowerDef("Amigos no Porto", 47, "Em cidades portuárias, com um teste de Carisma, encontra um amigo que faz um favor ou ajuda por um dia.", PowerRequirement(attributes = mapOf(CARISMA to 1), level = 6, text = "Car 1, 6º nível de bucaneiro")),
            ClassPowerDef("Aparar", 47, "Uma vez por rodada, por 1 PM, tenta evitar um ataque com um teste de ataque (arma leve ou ágil).", PowerRequirement(powers = listOf("Esgrimista"), text = "Esgrimista")),
            ClassPowerDef("Apostador", 47, "Gasta um dia em jogos de azar e aposta um valor com um teste de Jogatina: ganha ou perde a quantia.", PowerRequirement(trained = setOf(JOGATINA), text = "treinado em Jogatina")),
            ClassPowerDef("Ataque Acrobático", 47, "Atacar após um salto ou pirueta dá +2 no ataque e no dano."),
            ClassPowerDef("Aumento de Atributo", 47, "Recebe +1 em um atributo (registre em Atributos, nos aumentos por poderes). Uma vez por patamar para cada atributo.", repeatable = true),
            ClassPowerDef("Aventureiro Ávido", 47, "Uma vez por rodada, por 5 PM, ganha uma ação padrão ou de movimento extra."),
            ClassPowerDef("Bravata Audaz", 48, "Jura uma façanha; se cumprir, ganha +2 PM por nível de bucaneiro até o fim da aventura (falhar custa todos os PM)."),
            ClassPowerDef("Bravata Imprudente", 48, "Jura vencer um combate com uma restrição; se vencer, +2 em ataque e margem de ameaça até o fim da aventura."),
            ClassPowerDef("En Garde", 48, "Ação de movimento e 1 PM: +2 na margem de ameaça (armas leves ou ágeis) e +2 na Defesa na cena.", PowerRequirement(powers = listOf("Esgrimista"), text = "Esgrimista")),
            ClassPowerDef("Esgrimista", 48, "Com armas leves ou ágeis, soma sua Inteligência ao dano (limitado pelo nível).", PowerRequirement(attributes = mapOf(INTELIGENCIA to 1), text = "Int 1")),
            ClassPowerDef("Flagelo dos Mares", 48, "Aprende Amedrontar (Carisma), como habilidade não mágica.", PowerRequirement(trained = setOf(INTIMIDACAO), text = "treinado em Intimidação"), effects = PowerEffects(spell = "Amedrontar")),
            ClassPowerDef("Folião", 48, "Em festas, +2 em perícias de Carisma e as pessoas ficam mais amigáveis com você.", PowerRequirement(attributes = mapOf(CARISMA to 1), text = "Car 1")),
            ClassPowerDef("Grudar o Cano", 48, "Atirando com arma de fogo num inimigo adjacente, não sofre –5 e o dano aumenta um passo.", PowerRequirement(powers = listOf("Pistoleiro"), trained = setOf(LUTA), text = "treinado em Luta, Pistoleiro")),
            ClassPowerDef("Pernas do Mar", 48, "+2 em Acrobacia e Atletismo; equilibrando-se ou escalando, não fica desprevenido nem mais lento.", effects = PowerEffects(skills = mapOf(ACROBACIA to 2, ATLETISMO to 2))),
            ClassPowerDef("Pistoleiro", 48, "Proficiência com armas de fogo e +2 no dano com elas."),
            ClassPowerDef("Presença Paralisante", 48, "Soma Carisma na Iniciativa e, agindo primeiro, ganha uma ação padrão extra na primeira rodada.", PowerRequirement(attributes = mapOf(CARISMA to 1), level = 4, text = "Car 1, 4º nível de bucaneiro")),
            ClassPowerDef("Ripostar", 48, "Depois de Aparar com sucesso, por 1 PM contra-ataca quem o atacou.", PowerRequirement(powers = listOf("Aparar"), level = 12, text = "Aparar, 12º nível de bucaneiro")),
            ClassPowerDef("Touché", 48, "Investindo com arma leve ou ágil, por 2 PM aumenta o dano um passo e ganha +5 na margem de ameaça.", PowerRequirement(powers = listOf("Esgrimista"), level = 10, text = "Esgrimista, 10º nível de bucaneiro")),
        ),
        "druida" to listOf(
            ClassPowerDef("Aspecto da Primavera", 61, "Aprende uma magia de encantamento ou ilusão; algumas magias (igual ao Carisma) custam –1 PM."),
            ClassPowerDef("Aspecto do Inverno", 61, "Aprende uma magia de convocação ou evocação; redução de frio 5 e +1 de dano por dado em magias de frio."),
            ClassPowerDef("Aspecto do Outono", 61, "Aprende uma magia de necromancia; por 1 PM, inimigos próximos sofrem –2 em resistências até seu próximo turno."),
            ClassPowerDef("Aspecto do Verão", 61, "Aprende uma magia de transmutação; por 1 PM, sua arma pega fogo (+1d6) e acertos dão PM temporários."),
            ClassPowerDef("Aumento de Atributo", 61, "Recebe +1 em um atributo (registre em Atributos, nos aumentos por poderes). Uma vez por patamar para cada atributo.", repeatable = true),
            ClassPowerDef("Companheiro Animal", 61, "Recebe um companheiro animal (parceiro) de um tipo à escolha. Pode ser escolhido de novo para outro companheiro.", PowerRequirement(attributes = mapOf(CARISMA to 1), trained = setOf(ADESTRAMENTO), text = "Car 1, treinado em Adestramento"), repeatable = true),
            ClassPowerDef("Companheiro Animal Aprimorado", 62, "Um companheiro ganha um segundo tipo, com os bônus do seu nível.", PowerRequirement(powers = listOf("Companheiro Animal"), level = 6, text = "Companheiro Animal, 6º nível de druida")),
            ClassPowerDef("Companheiro Animal Lendário", 62, "Um companheiro dobra os bônus do tipo original.", PowerRequirement(powers = listOf("Companheiro Animal"), level = 18, text = "Companheiro Animal, 18º nível de druida")),
            ClassPowerDef("Companheiro Animal Mágico", 62, "Um companheiro ganha um segundo tipo entre adepto, destruidor, magivocador ou médico.", PowerRequirement(powers = listOf("Companheiro Animal"), level = 8, text = "Companheiro Animal, 8º nível de druida")),
            ClassPowerDef("Coração da Selva", 62, "Seus venenos ficam +2 mais difíceis de resistir e causam +1 de perda de vida por dado."),
            ClassPowerDef("Espírito dos Equinócios", 62, "Por 4 PM, até o fim da cena, rola de novo qualquer resultado 1 nos dados.", PowerRequirement(powers = listOf("Aspecto da Primavera", "Aspecto do Outono"), level = 10, text = "Aspecto da Primavera, Aspecto do Outono, 10º nível de druida")),
            ClassPowerDef("Espírito dos Solstícios", 62, "Por +4 PM, maximiza os efeitos numéricos de uma magia.", PowerRequirement(powers = listOf("Aspecto do Inverno", "Aspecto do Verão"), level = 10, text = "Aspecto do Inverno, Aspecto do Verão, 10º nível de druida")),
            ClassPowerDef("Forma Primal", 62, "Na Forma Selvagem, combina os benefícios de dois tipos de animal.", PowerRequirement(level = 18, text = "18º nível de druida")),
            ClassPowerDef("Forma Selvagem", 62, "Ação completa e 3 PM: transforma-se em animal (ágil, feroz, resistente, sorrateira ou veloz)."),
            ClassPowerDef("Forma Selvagem Aprimorada", 62, "Por 6 PM no total, assume a forma aprimorada da Forma Selvagem.", PowerRequirement(powers = listOf("Forma Selvagem"), level = 6, text = "Forma Selvagem, 6º nível de druida")),
            ClassPowerDef("Forma Selvagem Superior", 62, "Por 10 PM no total, assume a forma superior da Forma Selvagem.", PowerRequirement(powers = listOf("Forma Selvagem Aprimorada"), level = 12, text = "Forma Selvagem Aprimorada, 12º nível de druida")),
            ClassPowerDef("Força dos Penhascos", 62, "+2 em Fortitude; em contato com solo ou pedra, gasta PM para reduzir dano.", PowerRequirement(level = 4, text = "4º nível de druida"), effects = PowerEffects(skills = mapOf(FORTITUDE to 2))),
            ClassPowerDef("Liberdade da Pradaria", 62, "+2 em Reflexos; ao ar livre, 1 PM aumenta o alcance de uma magia em um passo.", effects = PowerEffects(skills = mapOf(REFLEXOS to 2))),
            ClassPowerDef("Magia Natural", 62, "Em forma selvagem, pode lançar magias e empunhar catalisadores e esotéricos.", PowerRequirement(powers = listOf("Forma Selvagem"), text = "Forma Selvagem")),
            ClassPowerDef("Presas Afiadas", 63, "A margem de ameaça das suas armas naturais aumenta em +2."),
            ClassPowerDef("Segredos da Natureza", 63, "Aprende duas magias que possa lançar, das suas escolas, arcanas ou divinas. Pode ser escolhido várias vezes.", repeatable = true),
            ClassPowerDef("Tranquilidade dos Lagos", 63, "+2 em Vontade; com um recipiente de água, 1 PM para refazer um teste de resistência por rodada.", effects = PowerEffects(skills = mapOf(VONTADE to 2))),
        ),
        "guerreiro" to listOf(
            ClassPowerDef("Ambidestria", 65, "Com duas armas (uma leve), ataca com as duas ao agredir, mas sofre –2 nos ataques até o próximo turno.", PowerRequirement(attributes = mapOf(DESTREZA to 2), text = "Des 2")),
            ClassPowerDef("Arqueiro", 65, "Com armas de ataque à distância, soma Sabedoria ao dano (limitado pelo nível).", PowerRequirement(attributes = mapOf(SABEDORIA to 1), text = "Sab 1")),
            ClassPowerDef("Ataque Reflexo", 65, "Por 1 PM, ataca um alvo próximo que fique desprevenido ou saia do seu alcance.", PowerRequirement(attributes = mapOf(DESTREZA to 1), text = "Des 1")),
            ClassPowerDef("Aumento de Atributo", 65, "Recebe +1 em um atributo (registre em Atributos, nos aumentos por poderes). Uma vez por patamar para cada atributo.", repeatable = true),
            ClassPowerDef("Bater e Correr", 65, "Pode continuar se movendo depois de uma investida; por 2 PM, investe em terreno difícil sem perder Defesa."),
            ClassPowerDef("Destruidor", 65, "Com armas corpo a corpo de duas mãos, rola de novo resultados 1 e 2 no dano.", PowerRequirement(attributes = mapOf(FORCA to 1), text = "For 1")),
            ClassPowerDef("Esgrimista", 65, "Com armas leves ou ágeis, soma sua Inteligência ao dano (limitado pelo nível).", PowerRequirement(attributes = mapOf(INTELIGENCIA to 1), text = "Int 1")),
            ClassPowerDef("Especialização em Arma", 65, "+2 no dano com uma arma escolhida. Pode ser escolhido para armas diferentes.", repeatable = true),
            ClassPowerDef("Especialização em Armadura", 65, "Redução de dano 5 usando armadura pesada.", PowerRequirement(level = 12, text = "12º nível de guerreiro")),
            ClassPowerDef("Golpe Demolidor", 65, "Ao quebrar ou atacar objetos, por 2 PM ignora a redução de dano deles."),
            ClassPowerDef("Golpe Pessoal", 65, "Cria uma técnica de ataque própria, montando efeitos com custos em PM. Pode ser escolhido para golpes diferentes.", PowerRequirement(level = 5, text = "5º nível de guerreiro"), repeatable = true),
            ClassPowerDef("Golpe de Raspão", 65, "Uma vez por rodada, ao errar um ataque, por 2 PM causa metade do dano."),
            ClassPowerDef("Mestre em Arma", 66, "Com a arma escolhida, o dano aumenta um passo e, por 2 PM, rola o ataque de novo.", PowerRequirement(powers = listOf("Especialização em Arma"), level = 12, text = "Especialização em Arma (mesma arma), 12º nível de guerreiro"), repeatable = true),
            ClassPowerDef("Planejamento Marcial", 66, "Uma vez por dia, por uma hora e 3 PM, ganha temporariamente um poder de guerreiro ou de combate.", PowerRequirement(trained = setOf(GUERRA), level = 10, text = "treinado em Guerra, 10º nível de guerreiro")),
            ClassPowerDef("Romper Resistências", 66, "No Ataque Especial, +1 PM para ignorar 10 de redução de dano."),
            ClassPowerDef("Solidez", 66, "Com escudo, o bônus dele na Defesa também vale nos testes de resistência."),
            ClassPowerDef("Tornado de Dor", 66, "Ação padrão e 2 PM: ataca todos os inimigos ao alcance, com dano maior a cada acerto.", PowerRequirement(level = 6, text = "6º nível de guerreiro")),
            ClassPowerDef("Valentão", 66, "+2 em ataque e dano contra oponentes caídos, desprevenidos, flanqueados ou indefesos."),
            ClassPowerDef("Ímpeto", 66, "Por 1 PM, +6m de deslocamento por uma rodada."),
        ),
    )

    fun of(classId: String?): List<ClassPowerDef> = BY_CLASS[classId].orEmpty()

    fun find(classId: String?, name: String): ClassPowerDef? = of(classId).firstOrNull { it.name == name }

    /** Poderes que ensinam duas magias a mais cada vez que são escolhidos. */
    val EXTRA_SPELL_POWERS = setOf("Aumentar Repertório", "Conhecimento Mágico", "Segredos da Natureza")
}
