package com.jovis.t20.rules

data class Deity(
    val name: String,
    val page: Int,
    val grantedPowers: Set<GrantedPower>,
    val forbidsMetalArmor: Boolean = false,
)

object Deities {
    /** Devotos não podem usar armaduras e escudos de metal (p. 97). */
    val ALLIHANNA = Deity(
        name = "Allihanna",
        page = 97,
        grantedPowers = setOf(
            AllihannaPowers.CompreenderOsErmos,
            AllihannaPowers.DedoVerde,
            AllihannaPowers.DescansoNatural,
            AllihannaPowers.VozDaNatureza,
        ),
        forbidsMetalArmor = true,
    )
}
