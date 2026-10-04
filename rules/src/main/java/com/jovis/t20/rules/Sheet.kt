package com.jovis.t20.rules

/*
 * Ficha preenchida pelo jogador.
 * O app não precisa conhecer raças, classes ou poderes: o jogador digita os valores e os
 * bônus com nome, e o SheetEngine faz as contas universais do livro e explica cada número.
 */

/** Um bônus digitado pelo jogador, com nome. Se tiver situação, aparece na explicação mas não soma. */
data class SheetBonus(
    val label: String,
    val value: Int,
    val situation: String? = null,
    /** Se verdadeiro, o valor é multiplicado pelo nível (ex.: Vitalidade, +1 PV por nível). */
    val perLevel: Boolean = false,
)

data class SheetArmor(
    val name: String,
    val defenseBonus: Int,
    /** Penalidade de armadura (zero ou negativa). */
    val armorPenalty: Int = 0,
    /** Armaduras pesadas não somam Destreza e reduzem o deslocamento (p. 152). */
    val heavy: Boolean = false,
)

data class SheetShield(
    val name: String,
    val defenseBonus: Int,
    val armorPenalty: Int = 0,
)

data class SheetWeapon(
    val name: String,
    val damageDice: String,
    /** Corpo a corpo e arremesso usam Luta/Pontaria e somam Força no dano; disparo não soma. */
    val mode: AttackMode,
    val criticalRange: Int = 20,
    val criticalMultiplier: Int = 2,
    val attackBonuses: List<SheetBonus> = emptyList(),
    val damageBonuses: List<SheetBonus> = emptyList(),
    val notes: String = "",
)

data class SheetItem(
    val name: String,
    val quantity: Int = 1,
    /** Espaços por unidade (alguns itens ocupam 0,5). */
    val slots: Double = 1.0,
    val equipped: Boolean = false,
)

data class SheetSpell(
    val name: String,
    val circle: Int,
    val school: String = "",
    val notes: String = "",
    // Preenchidos automaticamente quando a magia vem do catálogo
    val type: String = "",
    val execution: String = "",
    val range: String = "",
    val target: String = "",
    val duration: String = "",
    val resistance: String = "",
    val summary: String = "",
    val page: Int? = null,
)

/**
 * De onde vem cada perícia treinada, quando a ficha usa as regras do catálogo.
 * Com isso o app sabe quais escolhas ainda faltam e bloqueia as que não são permitidas.
 */
data class SkillPicks(
    /** Escolha entre duas perícias fixas da classe (ex.: Luta ou Pontaria). */
    val classFixedChoice: Skill? = null,
    val classChoices: Set<Skill> = emptySet(),
    val origin: Set<Skill> = emptySet(),
    /** Treinadas pela raça (Humano, Versátil). */
    val race: Set<Skill> = emptySet(),
    val intelligence: Set<Skill> = emptySet(),
    /** Outras fontes: poderes, regras da casa. */
    val other: Set<Skill> = emptySet(),
    /** Perícias com +2 da raça (Lefou, Deformidade); não dão treinamento. */
    val raceBonus: Set<Skill> = emptySet(),
)

/** Habilidade ou poder: nome e descrição livre. */
data class SheetEntry(
    val name: String,
    val description: String = "",
)

data class ManualSheet(
    // Identidade
    val name: String,
    val player: String = "",
    val race: String = "",
    val origin: String = "",
    val className: String = "",
    val level: Int = 1,
    val deity: String = "",
    /** Raça, classe e origem do catálogo, quando escolhidas (habilitam listas e restrições). */
    val raceId: String? = null,
    val classId: String? = null,
    /** Caminho da classe, quando ela tem (ex.: "mago" para o arcanista). */
    val classPathId: String? = null,
    val originId: String? = null,

    // Atributos: o valor final, usado em todas as contas
    val attributes: Map<Attribute, Int> = Attribute.entries.associateWith { 0 },
    /** Como os atributos são definidos; null = o jogador digita o valor final. */
    val attributeMode: AttributeMethod? = null,
    /** Valores antes da raça (compra de pontos ou rolagem). */
    val baseAttributes: Map<Attribute, Int> = Attribute.entries.associateWith { 0 },
    /** Atributos escolhidos para o +1 racial (Humano e Lefou). */
    val raceAttributeChoices: Set<Attribute> = emptySet(),
    /** Aumentos vindos de poderes (ex.: Aumento de Atributo). */
    val attributeIncreases: Map<Attribute, Int> = emptyMap(),

    // Pontos de vida e mana: os números da classe, para o app fazer a conta
    val hpFirstLevel: Int = 0,
    val hpPerLevel: Int = 0,
    val hpBonuses: List<SheetBonus> = emptyList(),
    val mpPerLevel: Int = 0,
    /** Atributo que a classe soma aos PM (ex.: Sabedoria para druidas), se houver. */
    val mpAttribute: Attribute? = null,
    val mpBonuses: List<SheetBonus> = emptyList(),

    // Perícias
    val trainedSkills: Set<Skill> = emptySet(),
    /** Origem de cada perícia treinada; null = perícias marcadas livremente. */
    val skillPicks: SkillPicks? = null,
    val skillBonuses: Map<Skill, List<SheetBonus>> = emptyMap(),

    // Defesa e deslocamento
    val armor: SheetArmor? = null,
    val shield: SheetShield? = null,
    val defenseBonuses: List<SheetBonus> = emptyList(),
    val baseSpeed: Int = 9,

    // Ataques e equipamento
    val weapons: List<SheetWeapon> = emptyList(),
    val inventory: List<SheetItem> = emptyList(),
    val money: Int = 0,

    // Magias
    val spellAttribute: Attribute? = null,
    /** Escolas de magia escolhidas (bardos e druidas escolhem três). */
    val spellSchools: Set<SpellSchool> = emptySet(),
    val spellDcBonuses: List<SheetBonus> = emptyList(),
    val spells: List<SheetSpell> = emptyList(),

    // Habilidades e poderes
    val abilities: List<SheetEntry> = emptyList(),
    /** Divindade do catálogo e poderes concedidos escolhidos. */
    val deityId: String? = null,
    val grantedPowers: Set<String> = emptySet(),
    /** Poderes escolhidos como benefício da origem. */
    val originPowers: Set<String> = emptySet(),
    /** Poderes de classe, na ordem escolhida (repetíveis aparecem mais de uma vez). */
    val classPowers: List<String> = emptyList(),

    // Estado de jogo (null = cheio)
    val currentHp: Int? = null,
    val currentMp: Int? = null,
    val tempHp: Int = 0,
    val tempMp: Int = 0,

    val notes: String = "",
) {
    init {
        require(level in 1..20) { "Nível deve estar entre 1 e 20 (recebido: $level)." }
    }
}
