package com.jovis.t20.rules

/** Poderes de classe. */
sealed interface ClassPower : Power {
    val className: String
}

private const val DRUIDA = "Druida"

private fun druidMod(power: Power, target: StatTarget, value: Int) =
    Modifier(target, value, Source(SourceType.POWER, power.name))

/** Fonte: Tormenta 20 Jogo do Ano, p. 61-63. TODO: demais poderes de druida. */
object DruidPowers {

    /** Transformação em animal (ação completa + 3 PM). As formas estão em WildShape. (p. 62-63) */
    data object FormaSelvagem : ClassPower {
        override val name = "Forma Selvagem"
        override val page = 62
        override val className = DRUIDA
    }

    data object FormaSelvagemAprimorada : ClassPower {
        override val name = "Forma Selvagem Aprimorada"
        override val page = 62
        override val className = DRUIDA
        override fun unmetPrerequisites(context: PrerequisiteContext) =
            listOfNotNull(context.power(FormaSelvagem), context.level(6, DRUIDA))
    }

    /** Recebe um companheiro animal (parceiro). Pode ser escolhido várias vezes, com companheiros diferentes. (p. 61) */
    data object CompanheiroAnimal : ClassPower {
        override val name = "Companheiro Animal"
        override val page = 61
        override val className = DRUIDA
        override val repeatable = true
        override fun unmetPrerequisites(context: PrerequisiteContext) =
            listOfNotNull(context.attribute(Attribute.CARISMA, 1), context.trained(Skill.ADESTRAMENTO))
    }

    /** +2 em Reflexos; ao ar livre, 1 PM aumenta o alcance de uma magia em um passo. (p. 62) */
    data object LiberdadeDaPradaria : ClassPower {
        override val name = "Liberdade da Pradaria"
        override val page = 62
        override val className = DRUIDA
        override fun modifiers(characterLevel: Int) = listOf(druidMod(this, StatTarget.SkillTarget(Skill.REFLEXOS), +2))
        override val notes = listOf("Ao ar livre, pode gastar 1 PM ao lançar uma magia para aumentar o alcance dela em um passo.")
    }

    /** +2 em Fortitude; reduz dano em contato com solo ou pedra gastando PM. Pré-requisito: 4º nível. (p. 62) */
    data object ForcaDosPenhascos : ClassPower {
        override val name = "Força dos Penhascos"
        override val page = 62
        override val className = DRUIDA
        override fun modifiers(characterLevel: Int) = listOf(druidMod(this, StatTarget.SkillTarget(Skill.FORTITUDE), +2))
        override fun unmetPrerequisites(context: PrerequisiteContext) = listOfNotNull(context.level(4, DRUIDA))
        override val notes = listOf("Ao sofrer dano em contato com o solo ou pedra, pode gastar PM (limitado pela Sabedoria); cada PM reduz o dano em 10.")
    }

    /** Pode lançar magias e empunhar catalisadores e esotéricos em forma selvagem. (p. 62) */
    data object MagiaNatural : ClassPower {
        override val name = "Magia Natural"
        override val page = 62
        override val className = DRUIDA
        override fun unmetPrerequisites(context: PrerequisiteContext) = listOfNotNull(context.power(FormaSelvagem))
    }

    /** +2 em Vontade; com água por perto, 1 PM para refazer um teste de resistência por rodada. (p. 63) */
    data object TranquilidadeDosLagos : ClassPower {
        override val name = "Tranquilidade dos Lagos"
        override val page = 63
        override val className = DRUIDA
        override fun modifiers(characterLevel: Int) = listOf(druidMod(this, StatTarget.SkillTarget(Skill.VONTADE), +2))
        override val notes = listOf("Portando um recipiente com água, uma vez por rodada pode pagar 1 PM para refazer um teste de resistência.")
    }

    /** A margem de ameaça das armas naturais aumenta em +2. (p. 63) */
    data object PresasAfiadas : ClassPower {
        override val name = "Presas Afiadas"
        override val page = 63
        override val className = DRUIDA
    }
}
