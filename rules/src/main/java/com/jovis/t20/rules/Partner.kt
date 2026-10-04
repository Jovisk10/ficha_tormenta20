package com.jovis.t20.rules

/** Fonte: Tormenta 20 Jogo do Ano, p. 260-261. */
enum class PartnerType(val displayName: String) {
    ADEPTO("Adepto"), AJUDANTE("Ajudante"), ASSASSINO("Assassino"), ATIRADOR("Atirador"),
    COMBATENTE("Combatente"), DESTRUIDOR("Destruidor"), FORTAO("Fortão"), GUARDIAO("Guardião"),
    MAGIVOCADOR("Magivocador"), MEDICO("Médico"), PERSEGUIDOR("Perseguidor"), VIGILANTE("Vigilante"),
    MONTARIA("Montaria"),
}

enum class PartnerTier(val displayName: String) { INICIANTE("iniciante"), VETERANO("veterano"), MESTRE("mestre") }

/**
 * Companheiro animal do druida (p. 61): um parceiro iniciante, que vira veterano no 7º nível
 * e mestre no 15º.
 */
data class AnimalCompanion(
    val name: String,
    val type: PartnerType,
    /** Só para o tipo Ajudante: as perícias que recebem bônus. */
    val chosenSkills: Set<Skill> = emptySet(),
)

object Partners {
    val COMPANION_TYPES = setOf(
        PartnerType.AJUDANTE, PartnerType.ASSASSINO, PartnerType.ATIRADOR, PartnerType.COMBATENTE,
        PartnerType.FORTAO, PartnerType.GUARDIAO, PartnerType.PERSEGUIDOR, PartnerType.MONTARIA,
    )

    fun companionTier(characterLevel: Int): PartnerTier = when {
        characterLevel >= 15 -> PartnerTier.MESTRE
        characterLevel >= 7 -> PartnerTier.VETERANO
        else -> PartnerTier.INICIANTE
    }

    /** Até o 4º nível: 1 parceiro; do 5º ao 16º: 2; do 17º em diante: 3 (p. 261). */
    fun limit(characterLevel: Int): Int = when {
        characterLevel >= 17 -> 3
        characterLevel >= 5 -> 2
        else -> 1
    }

    /** Ajudante: quantas perícias recebem bônus em cada nível de poder. */
    fun helperSkillCount(tier: PartnerTier): Int = if (tier == PartnerTier.INICIANTE) 2 else 3

    fun modifiers(type: PartnerType, tier: PartnerTier, chosenSkills: Set<Skill>, sourceName: String): List<Modifier> {
        val source = Source(SourceType.PARTNER, sourceName)
        fun skill(s: Skill, v: Int) = Modifier(StatTarget.SkillTarget(s), v, source)
        val byTier = { a: Int, b: Int, c: Int -> when (tier) { PartnerTier.INICIANTE -> a; PartnerTier.VETERANO -> b; PartnerTier.MESTRE -> c } }
        return when (type) {
            PartnerType.COMBATENTE -> listOf(Modifier(StatTarget.Attack, byTier(2, 3, 4), source))
            PartnerType.GUARDIAO -> buildList {
                add(Modifier(StatTarget.Defense, byTier(2, 3, 4), source))
                if (tier == PartnerTier.MESTRE) listOf(Skill.FORTITUDE, Skill.REFLEXOS, Skill.VONTADE).forEach { add(skill(it, 2)) }
            }
            PartnerType.PERSEGUIDOR -> listOf(skill(Skill.PERCEPCAO, 2), skill(Skill.SOBREVIVENCIA, 2))
            PartnerType.VIGILANTE -> listOf(skill(Skill.PERCEPCAO, 2), skill(Skill.INICIATIVA, 2))
            PartnerType.AJUDANTE -> chosenSkills.map { skill(it, byTier(2, 2, 4)) }
            else -> emptyList()
        }
    }

    fun notes(type: PartnerType, tier: PartnerTier, sourceName: String): List<String> {
        val text = when (type) {
            PartnerType.ADEPTO -> when (tier) {
                PartnerTier.INICIANTE -> "magias de 1º círculo custam –1 PM"
                PartnerTier.VETERANO -> "magias de 1º e 2º círculos custam –1 PM"
                PartnerTier.MESTRE -> "magias de 1º e 2º círculos custam –1 PM, cumulativo com outras reduções"
            }
            PartnerType.ASSASSINO -> when (tier) {
                PartnerTier.INICIANTE -> "Ataque Furtivo +1d6"
                PartnerTier.VETERANO -> "Ataque Furtivo +1d6 e bônus por flanquear contra um inimigo por rodada"
                PartnerTier.MESTRE -> "Ataque Furtivo +2d6 e bônus por flanquear contra um inimigo por rodada"
            }
            PartnerType.ATIRADOR -> "uma vez por rodada, +${pick(tier, "1d6", "1d10", "2d8")} em uma rolagem de dano à distância"
            PartnerType.FORTAO -> "uma vez por rodada, +${pick(tier, "1d8", "1d12", "3d6")} em uma rolagem de dano corpo a corpo"
            PartnerType.COMBATENTE -> if (tier == PartnerTier.MESTRE) "uma vez por rodada, pode gastar 5 PM para um ataque extra" else null
            PartnerType.PERSEGUIDOR -> pick(tier, null, "pode usar Sentidos Aguçados", "pode usar Percepção às Cegas")
            PartnerType.VIGILANTE -> pick(tier, null, "pode usar Esquiva Sobrenatural", "pode usar Olhos nas Costas")
            PartnerType.MONTARIA -> "regras de montaria e combate montado (p. 261)"
            PartnerType.DESTRUIDOR, PartnerType.MAGIVOCADOR, PartnerType.MEDICO -> "veja p. 261"
            else -> null
        } ?: return emptyList()
        return listOf("$sourceName: $text.")
    }

    private fun <T> pick(tier: PartnerTier, a: T, b: T, c: T): T =
        when (tier) { PartnerTier.INICIANTE -> a; PartnerTier.VETERANO -> b; PartnerTier.MESTRE -> c }
}
