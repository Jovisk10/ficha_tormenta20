package com.jovis.t20.rules

enum class AttributeMethod { POINT_BUY, ROLLED }

/**
 * Somente o que o jogador digita ou escolhe. Nada aqui é calculado:
 * todo valor derivado sai do RuleEngine, e os erros de montagem saem do Validator.
 * V1: uma classe só (multiclasse fica para depois).
 */
data class Character(
    val name: String,
    val level: Int,
    val attributeMethod: AttributeMethod,
    val baseAttributes: Map<Attribute, Int>,
    val race: RaceSelection,
    val characterClass: ClassDefinition,
    /** Perícias escolhidas da lista da classe (as fixas da classe entram sozinhas). */
    val classSkillChoices: Set<Skill>,
    val origin: OriginSelection,
    val deity: Deity? = null,
    /** Perícias extras por Inteligência positiva. */
    val intelligenceSkills: Set<Skill> = emptySet(),
    /** Poderes gerais escolhidos fora da origem. */
    val generalPowers: Set<GeneralPower> = emptySet(),
    /** Tudo o que o personagem carrega, inclusive a armadura vestida. */
    val inventory: List<InventoryEntry> = emptyList(),
    /** Armadura vestida (deve estar também no inventário). */
    val armor: ArmorDefinition? = null,
    val spellSchools: Set<SpellSchool> = emptySet(),
    /** Magias aprendidas pela classe (as de raça entram sozinhas). */
    val classSpells: Set<SpellDefinition> = emptySet(),
    val manualModifiers: List<Modifier> = emptyList(),
) {
    init {
        require(level in 1..20) { "Nível deve estar entre 1 e 20 (recebido: $level)." }
        require(baseAttributes.keys == Attribute.entries.toSet()) {
            "Todos os seis atributos precisam de um valor base."
        }
    }
}
