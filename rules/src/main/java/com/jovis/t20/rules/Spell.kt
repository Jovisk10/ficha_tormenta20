package com.jovis.t20.rules

enum class SpellType(val displayName: String) {
    ARCANE("Arcana"), DIVINE("Divina"), UNIVERSAL("Universal");

    /** Magias universais podem ser lançadas por conjuradores arcanos e divinos. */
    fun castableBy(casterType: SpellType): Boolean = this == UNIVERSAL || this == casterType
}

enum class SpellSchool(val displayName: String) {
    ABJURACAO("Abjuração"), ADIVINHACAO("Adivinhação"), CONVOCACAO("Convocação"),
    ENCANTAMENTO("Encantamento"), EVOCACAO("Evocação"), ILUSAO("Ilusão"),
    NECROMANCIA("Necromancia"), TRANSMUTACAO("Transmutação"),
}

data class SpellDefinition(
    val name: String,
    val type: SpellType,
    val circle: Int,
    val school: SpellSchool,
    val page: Int,
)

/** Custo por círculo. Fonte: Tabela 4-1, p. 170. */
object SpellCost {
    private val costs = mapOf(1 to 1, 2 to 3, 3 to 6, 4 to 10, 5 to 15)
    fun of(circle: Int): Int = costs[circle] ?: throw IllegalArgumentException("Círculo inválido: $circle.")
}

object Spells {
    val ACALMAR_ANIMAL = SpellDefinition("Acalmar Animal", SpellType.DIVINE, 1, SpellSchool.ENCANTAMENTO, 178)
    val BENCAO = SpellDefinition("Bênção", SpellType.DIVINE, 1, SpellSchool.ENCANTAMENTO, 182)
    val COMPREENSAO = SpellDefinition("Compreensão", SpellType.UNIVERSAL, 1, SpellSchool.ADIVINHACAO, 184)
    val CONTROLAR_PLANTAS = SpellDefinition("Controlar Plantas", SpellType.DIVINE, 1, SpellSchool.TRANSMUTACAO, 178)
    val CURAR_FERIMENTOS = SpellDefinition("Curar Ferimentos", SpellType.DIVINE, 1, SpellSchool.EVOCACAO, 189)
}

/** Ritmo em que uma classe aprende magias novas. */
enum class SpellProgression {
    /** Uma a cada nível par (bardo, druida). */
    EVEN_LEVELS,
    /** Uma a cada nível (arcanista bruxo). */
    EVERY_LEVEL,
    /** Uma a cada nível ímpar a partir do 3º (arcanista feiticeiro). */
    ODD_LEVELS,
    /** Uma a cada nível, mais uma ao liberar cada círculo novo (arcanista mago). */
    EVERY_LEVEL_PLUS_CIRCLE,
}

/** Como uma classe lança magias. */
data class Spellcasting(
    val attribute: Attribute,
    val type: SpellType,
    /** Quantas escolas a classe escolhe; 0 = sem restrição de escola (arcanista). */
    val schoolCount: Int,
    val initialSpells: Int,
    /** Magias aprendidas a cada nível par (usado na progressão EVEN_LEVELS). */
    val spellsPerEvenLevel: Int,
    /** Círculo -> nível de classe em que é liberado. */
    val circleUnlockLevel: Map<Int, Int>,
    val progression: SpellProgression = SpellProgression.EVEN_LEVELS,
) {
    val restrictsSchools: Boolean get() = schoolCount > 0

    fun spellsKnown(classLevel: Int): Int = when (progression) {
        SpellProgression.EVEN_LEVELS -> initialSpells + spellsPerEvenLevel * (classLevel / 2)
        SpellProgression.EVERY_LEVEL -> initialSpells + (classLevel - 1)
        SpellProgression.ODD_LEVELS -> initialSpells + (classLevel - 1) / 2
        SpellProgression.EVERY_LEVEL_PLUS_CIRCLE ->
            initialSpells + (classLevel - 1) + circleUnlockLevel.count { (circle, level) -> circle > 1 && level <= classLevel }
    }

    fun maxCircle(classLevel: Int): Int = circleUnlockLevel.filterValues { it <= classLevel }.keys.max()
}

/** Magia ensinada por raça ou poder, fora das magias de classe. */
data class GrantedSpell(
    val spell: SpellDefinition,
    val sourceName: String,
    val sourceType: SourceType,
    /** Redução de custo se a magia for aprendida novamente (ex.: Amiga das Plantas, –1 PM). */
    val relearnDiscount: Int = 0,
    /** Atributo-chave, quando a fonte define (null = atributo de magia da classe). */
    val attribute: Attribute? = null,
)
