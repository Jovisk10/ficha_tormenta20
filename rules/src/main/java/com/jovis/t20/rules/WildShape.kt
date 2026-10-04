package com.jovis.t20.rules

/**
 * Forma Selvagem (p. 62-63): ação completa + 3 PM.
 * Na forma, não pode falar, empunhar itens nem lançar magias (exceto com Magia Natural);
 * o equipamento some, mas itens vestidos (como armadura) continuam valendo.
 * TODO: formas Aprimorada e Superior (6º e 12º níveis).
 */
enum class WildShapeForm(val displayName: String) {
    AGIL("Ágil"), FEROZ("Feroz"), RESISTENTE("Resistente"), SORRATEIRA("Sorrateira"), VELOZ("Veloz"),
}

/** Forma Veloz escolhe um destes benefícios. */
enum class SwiftOption(val displayName: String) {
    DESLOCAMENTO_15("deslocamento 15m"),
    ESCALADA_9("deslocamento de escalada 9m"),
    NATACAO_9("deslocamento de natação 9m"),
}

/** Estado de sessão: a forma em que o personagem está agora. */
data class ActiveWildShape(
    val form: WildShapeForm,
    /** O jogador escolhe o tipo de dano da arma natural: corte, impacto ou perfuração. */
    val naturalDamageType: String = "Perfuração",
    val swiftOption: SwiftOption? = null,
)

object WildShape {
    const val COST_PM = 3
    const val PAGE = 63

    fun modifiers(active: ActiveWildShape): List<Modifier> {
        val source = Source(SourceType.CONDITION, "Forma Selvagem (${active.form.displayName})")
        fun mod(target: StatTarget, value: Int) = Modifier(target, value, source)
        return when (active.form) {
            WildShapeForm.AGIL -> listOf(mod(StatTarget.AttributeTarget(Attribute.DESTREZA), +2))
            WildShapeForm.FEROZ -> listOf(mod(StatTarget.AttributeTarget(Attribute.FORCA), +3), mod(StatTarget.Defense, +2))
            WildShapeForm.RESISTENTE -> listOf(mod(StatTarget.Defense, +5), mod(StatTarget.DamageReduction, +5))
            // Tamanho Pequeno: +2 em Furtividade (e –2 em manobras, ainda não modelado)
            WildShapeForm.SORRATEIRA -> listOf(
                mod(StatTarget.AttributeTarget(Attribute.DESTREZA), +2),
                mod(StatTarget.SkillTarget(Skill.FURTIVIDADE), +2),
            )
            WildShapeForm.VELOZ -> listOf(mod(StatTarget.AttributeTarget(Attribute.DESTREZA), +2))
        }
    }

    /** Armas naturais da forma. Presas Afiadas aumenta a margem de ameaça em +2. */
    fun naturalWeapons(active: ActiveWildShape, sharpFangs: Boolean): List<WeaponDefinition> {
        fun natural(name: String, dice: String, critical: Int = 20) = WeaponDefinition(
            name = name,
            proficiency = WeaponProficiency.NATURAL,
            purpose = WeaponPurpose.MELEE,
            damageDice = dice,
            damageType = active.naturalDamageType,
            criticalRange = if (sharpFangs) critical - 2 else critical,
            slots = 0.0,
        )
        return when (active.form) {
            WildShapeForm.AGIL -> listOf(natural("Armas naturais (2)", "1d6", critical = 19))
            WildShapeForm.FEROZ -> listOf(natural("Arma natural", "1d8"))
            WildShapeForm.RESISTENTE -> listOf(natural("Arma natural", "1d6"))
            WildShapeForm.SORRATEIRA -> listOf(natural("Arma natural", "1d4"))
            WildShapeForm.VELOZ -> listOf(natural("Arma natural", "1d6"))
        }
    }

    /** Deslocamento terrestre que substitui o normal, se a forma der um. */
    fun groundSpeedOverride(active: ActiveWildShape): Int? =
        if (active.form == WildShapeForm.VELOZ && active.swiftOption == SwiftOption.DESLOCAMENTO_15) 15 else null

    fun notes(active: ActiveWildShape): List<String> = buildList {
        add("Forma Selvagem: não pode falar, empunhar itens nem lançar magias; itens vestidos continuam valendo.")
        if (active.form == WildShapeForm.AGIL) {
            add("Forma Ágil: ao usar agredir, pode atacar com as duas armas naturais, mas sofre –2 em todos os ataques até o próximo turno.")
        }
        if (active.form == WildShapeForm.RESISTENTE) add("Forma Resistente: redução de dano 5.")
        if (active.form == WildShapeForm.SORRATEIRA) add("Forma Sorrateira: tamanho Pequeno (–2 em testes de manobra).")
        if (active.form == WildShapeForm.VELOZ) {
            active.swiftOption?.takeIf { it != SwiftOption.DESLOCAMENTO_15 }?.let { add("Forma Veloz: ${it.displayName}.") }
        }
    }
}
