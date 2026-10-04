package com.jovis.t20.rules

/** Fonte: Tormenta 20 Jogo do Ano, p. 142. NATURAL: todas as criaturas são proficientes. */
enum class WeaponProficiency { SIMPLE, MARTIAL, EXOTIC, FIREARM, NATURAL }

/** Propósito da arma: corpo a corpo, arremesso ou disparo (p. 142). */
enum class WeaponPurpose { MELEE, THROWN, FIRING }

/** Como o ataque está sendo feito agora. Uma lança pode atacar corpo a corpo ou ser arremessada. */
enum class AttackMode { MELEE, THROWN, FIRING }

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
