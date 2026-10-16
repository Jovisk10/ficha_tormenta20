package com.jovis.t20.rules

/** Fonte: Tormenta 20 Jogo do Ano, p. 142. NATURAL: todas as criaturas são proficientes. */
enum class WeaponProficiency { SIMPLE, MARTIAL, EXOTIC, FIREARM, NATURAL }

/** Propósito da arma: corpo a corpo, arremesso ou disparo (p. 142). */
enum class WeaponPurpose { MELEE, THROWN, FIRING }

/** Como o ataque está sendo feito agora. Uma lança pode atacar corpo a corpo ou ser arremessada. */
enum class AttackMode { MELEE, THROWN, FIRING }

/**
 * Danos com barra. Armas adaptáveis (p. 143), como a espada bastarda e a katana (p. 147-148),
 * mostram "1d10/1d12": o segundo dano vale com as duas mãos. Nas armas duplas (bordão, corrente
 * de espinhos, p. 143) a barra separa o dano de cada ponta e os dois valores são iguais; por isso
 * só há dano de duas mãos quando os dois lados diferem.
 */
object WeaponDamage {
    fun hasTwoHandedDamage(damageDice: String): Boolean {
        val parts = damageDice.split('/').map { it.replace(" ", "").lowercase() }
        return parts.size == 2 && parts.all { it.isNotEmpty() } && parts[0] != parts[1]
    }

    fun forGrip(damageDice: String, twoHanded: Boolean): String =
        if (twoHanded && hasTwoHandedDamage(damageDice)) damageDice.substringAfter('/').trim()
        else damageDice.substringBefore('/').trim()
}

data class WeaponDefinition(
    override val name: String,
    val proficiency: WeaponProficiency,
    val purpose: WeaponPurpose,
    val damageDice: String,
    val damageType: String,
    /** Menor resultado natural do d20 que causa crítico (20 = só no 20). */
    val criticalRange: Int = 20,
    val criticalMultiplier: Int = 2,
    /** Armas corpo a corpo que também podem ser arremessadas (ex.: lança). */
    val canBeThrown: Boolean = false,
    override val slots: Double = 1.0,
    /** Exótica com uma mão, mas marcial com as duas (espada bastarda, katana, machado anão; p. 147-149). */
    val martialWhenTwoHanded: Boolean = false,
) : Item {
    val allowedModes: Set<AttackMode>
        get() = when (purpose) {
            WeaponPurpose.MELEE -> if (canBeThrown) setOf(AttackMode.MELEE, AttackMode.THROWN) else setOf(AttackMode.MELEE)
            WeaponPurpose.THROWN -> setOf(AttackMode.THROWN)
            WeaponPurpose.FIRING -> setOf(AttackMode.FIRING)
        }

    val criticalText: String get() = "$criticalRange/x$criticalMultiplier"
}

/** Fonte: Tormenta 20 Jogo do Ano, tabela de armas, p. 144. */
object Weapons {
    /** "Uma lança pode ser arremessada." */
    val LANCA = WeaponDefinition(
        name = "Lança", proficiency = WeaponProficiency.SIMPLE, purpose = WeaponPurpose.MELEE,
        damageDice = "1d6", damageType = "Perfuração", canBeThrown = true,
    )
    val ESPADA_LONGA = WeaponDefinition(
        name = "Espada longa", proficiency = WeaponProficiency.MARTIAL, purpose = WeaponPurpose.MELEE,
        damageDice = "1d8", damageType = "Corte", criticalRange = 19,
    )
}
