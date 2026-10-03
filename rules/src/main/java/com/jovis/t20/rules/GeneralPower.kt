package com.jovis.t20.rules

/** Poderes gerais (Capítulo 2). Cada um declara seus modificadores e pré-requisitos. */
sealed interface GeneralPower {
    val name: String
    val page: Int

    fun modifiers(characterLevel: Int): List<Modifier> = emptyList()

    /** Devolve a descrição do pré-requisito não cumprido, ou null se estiver tudo certo. */
    fun unmetPrerequisite(finalAttributes: Map<Attribute, Int>): String? = null
}

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

    override fun unmetPrerequisite(finalAttributes: Map<Attribute, Int>): String? =
        if (finalAttributes.getValue(Attribute.CONSTITUICAO) >= 1) null else "Constituição 1"
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
