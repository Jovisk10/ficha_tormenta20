package com.jovis.t20.rules

/** O que um modificador afeta. */
sealed interface StatTarget {
    data class AttributeTarget(val attribute: Attribute) : StatTarget
    data class SkillTarget(val skill: Skill) : StatTarget
    data object MaxHp : StatTarget
    data object MaxMp : StatTarget
    data object Defense : StatTarget
    data object SpellDc : StatTarget
    /** Bônus em todos os testes de ataque. */
    data object Attack : StatTarget
    /** Bônus em todas as rolagens de dano de armas. */
    data object WeaponDamage : StatTarget
    /** Limite de carga em espaços. */
    data object LoadLimit : StatTarget
    /** Redução de dano. */
    data object DamageReduction : StatTarget
    /** Deslocamento em metros. */
    data object Speed : StatTarget
}

enum class SourceType { BASE, RACE, ORIGIN, CLASS, ABILITY, POWER, PARTNER, ITEM, CONDITION, LEVEL, MANUAL }

/** De onde um modificador vem. É o que permite explicar "por que esse valor?". */
data class Source(val type: SourceType, val name: String)

data class Modifier(
    val target: StatTarget,
    val value: Int,
    val source: Source,
    /** Se preenchido, o modificador é situacional: aparece como nota, mas não entra na soma. */
    val situation: String? = null,
) {
    val isSituational: Boolean get() = situation != null
}
