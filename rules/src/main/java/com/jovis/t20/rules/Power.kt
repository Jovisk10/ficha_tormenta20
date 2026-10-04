package com.jovis.t20.rules

/** Tudo que um pré-requisito pode precisar consultar. */
data class PrerequisiteContext(
    val attributes: Map<Attribute, Int>,
    val trainedSkills: Set<Skill>,
    val powers: List<Power>,
    val classLevel: Int,
) {
    fun attribute(attribute: Attribute, min: Int): String? =
        if (attributes.getValue(attribute) >= min) null else "${attribute.displayName} $min"

    fun trained(skill: Skill): String? = if (skill in trainedSkills) null else "treinado em ${skill.displayName}"

    fun power(power: Power): String? = if (power in powers) null else power.name

    fun level(min: Int, className: String): String? = if (classLevel >= min) null else "${min}º nível de ${className.lowercase()}"
}

/** Qualquer poder: geral, de classe ou concedido. */
sealed interface Power {
    val name: String
    val page: Int

    /** Pode ser escolhido mais de uma vez? */
    val repeatable: Boolean get() = false

    fun modifiers(characterLevel: Int): List<Modifier> = emptyList()

    /** Descrições dos pré-requisitos não cumpridos (lista vazia = tudo certo). */
    fun unmetPrerequisites(context: PrerequisiteContext): List<String> = emptyList()

    /** Magias que o poder ensina. */
    val grantedSpells: List<GrantedSpell> get() = emptyList()

    /** Atributos alternativos que o poder permite usar em perícias (ex.: Sabedoria em Adestramento). */
    val skillAttributeOptions: Map<Skill, Attribute> get() = emptyMap()

    /** Efeitos que o motor não calcula, mas que a ficha deve mostrar. */
    val notes: List<String> get() = emptyList()
}

/** Poderes gerais (Capítulo 2). */
sealed interface GeneralPower : Power

/** +1 PV por nível de personagem e +2 em Fortitude. Pré-requisito: Con 1. (p. 129) */
data object Vitalidade : GeneralPower {
    override val name = "Vitalidade"
    override val page = 129

    override fun modifiers(characterLevel: Int): List<Modifier> {
        val source = Source(SourceType.POWER, name)
        return listOf(
            Modifier(StatTarget.MaxHp, characterLevel, source),
            Modifier(StatTarget.SkillTarget(Skill.FORTITUDE), +2, source),
        )
    }

    override fun unmetPrerequisites(context: PrerequisiteContext): List<String> =
        listOfNotNull(context.attribute(Attribute.CONSTITUICAO, 1))
}

/** Ainda sem efeitos modelados no motor. */
data object LoboSolitario : GeneralPower {
    override val name = "Lobo Solitário"
    override val page = 0 // a confirmar
}

/** Ainda sem efeitos modelados no motor. */
data object VidaRustica : GeneralPower {
    override val name = "Vida Rústica"
    override val page = 0 // a confirmar
}
