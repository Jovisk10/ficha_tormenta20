package com.jovis.t20.rules

/** Poderes concedidos por divindades. */
sealed interface GrantedPower : Power

/** Fonte: Tormenta 20 Jogo do Ano, p. 132-136. */
object AllihannaPowers {

    /** +2 em Sobrevivência e pode usar Sabedoria para Adestramento. (p. 132) */
    data object CompreenderOsErmos : GrantedPower {
        override val name = "Compreender os Ermos"
        override val page = 132
        override fun modifiers(characterLevel: Int) =
            listOf(Modifier(StatTarget.SkillTarget(Skill.SOBREVIVENCIA), +2, Source(SourceType.POWER, name)))
        override val skillAttributeOptions = mapOf(Skill.ADESTRAMENTO to Attribute.SABEDORIA)
    }

    /** Aprende Controlar Plantas; se aprender de novo, custo –1 PM. (p. 133) */
    data object DedoVerde : GrantedPower {
        override val name = "Dedo Verde"
        override val page = 133
        override val grantedSpells = listOf(
            GrantedSpell(Spells.CONTROLAR_PLANTAS, name, SourceType.POWER, relearnDiscount = 1),
        )
    }

    /** Dormir ao relento conta como descanso confortável. (p. 133) */
    data object DescansoNatural : GrantedPower {
        override val name = "Descanso Natural"
        override val page = 133
        override val notes = listOf("Dormir ao relento conta como condição de descanso confortável.")
    }

    /** Fala com animais e aprende Acalmar Animal (só contra animais); se aprender de novo, –1 PM. (p. 136) */
    data object VozDaNatureza : GrantedPower {
        override val name = "Voz da Natureza"
        override val page = 136
        override val grantedSpells = listOf(
            GrantedSpell(Spells.ACALMAR_ANIMAL, name, SourceType.POWER, relearnDiscount = 1),
        )
        override val notes = listOf("Pode falar com animais (como a magia Voz Divina). Acalmar Animal só funciona contra animais.")
    }
}
