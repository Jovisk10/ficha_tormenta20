package com.jovis.t20.rules

import kotlin.random.Random

/** Um grupo de dados iguais, como 2d6. */
data class DiceTerm(val count: Int, val sides: Int)

/** Uma fórmula de dados, como 1d8+1d6+3. */
data class DiceFormula(val terms: List<DiceTerm>, val modifier: Int = 0) {
    val text: String
        get() = buildString {
            append(terms.joinToString("+") { "${it.count}d${it.sides}" })
            if (modifier > 0) append("+$modifier") else if (modifier < 0) append("$modifier")
            if (terms.isEmpty() && modifier == 0) append("0")
        }

    /** O dado principal, usado para escolher o desenho na animação. */
    val mainSides: Int get() = terms.firstOrNull()?.sides ?: 20

    /** No crítico, multiplicam-se só os dados, não os bônus numéricos (p. 142). */
    fun criticalOf(multiplier: Int): DiceFormula = copy(terms = terms.map { it.copy(count = it.count * multiplier) })
}

object DiceParser {
    private val termRegex = Regex("""([+-]?)\s*(\d*)\s*[dD]\s*(\d+)|([+-]?)\s*(\d+)""")

    /**
     * Lê fórmulas como "1d6+2", "2d4", "d20", "1d8+1d6-1".
     * Armas com dois danos ("1d10/1d12") usam o primeiro.
     */
    fun parse(text: String): DiceFormula? {
        val clean = text.substringBefore('/').replace(" ", "")
        if (clean.isBlank()) return null
        val terms = mutableListOf<DiceTerm>()
        var modifier = 0
        var consumed = 0
        termRegex.findAll(clean).forEach { m ->
            if (m.range.first != consumed) return null
            consumed = m.range.last + 1
            if (m.groups[3] != null) {
                if (m.groupValues[1] == "-") return null // dados negativos não fazem sentido aqui
                val count = m.groupValues[2].ifEmpty { "1" }.toInt()
                val sides = m.groupValues[3].toInt()
                if (count !in 1..100 || sides !in 2..1000) return null
                terms += DiceTerm(count, sides)
            } else {
                val value = m.groupValues[5].toInt()
                modifier += if (m.groupValues[4] == "-") -value else value
            }
        }
        if (consumed != clean.length) return null
        return DiceFormula(terms, modifier)
    }
}

/** Tipo de rolagem, para o histórico e a animação. */
enum class RollKind { SKILL, ATTACK, ATTRIBUTE, DAMAGE, FREE }

/** Uma rolagem registrada no histórico. */
sealed interface RollRecord {
    val id: Long
    val label: String
    val kind: RollKind
    val total: Int
    /** Como o total foi formado, para exibir embaixo do resultado. */
    val breakdown: String
}

/**
 * Teste de d20 (perícia, ataque ou atributo).
 * 20 natural sempre é sucesso e 1 natural sempre é falha (p. 221).
 */
data class TestRoll(
    override val id: Long,
    override val label: String,
    override val kind: RollKind,
    val natural: Int,
    val bonus: Int,
    /** Margem de ameaça da arma (só em ataques). */
    val threatFrom: Int? = null,
) : RollRecord {
    override val total: Int get() = natural + bonus
    val isNatural20: Boolean get() = natural == 20
    val isNatural1: Boolean get() = natural == 1
    /** Ataque com resultado natural dentro da margem de ameaça: se acertar, é crítico (p. 142). */
    val isThreat: Boolean get() = threatFrom != null && natural >= threatFrom
    override val breakdown: String get() = "d20 ($natural) ${spaced(bonus)} = $total"
}

/** Rolagem de dados somados (dano ou dados livres). */
data class DiceRoll(
    override val id: Long,
    override val label: String,
    override val kind: RollKind,
    val formula: DiceFormula,
    /** Resultado de cada dado, na ordem dos termos. */
    val results: List<Pair<Int, Int>>,
    val critical: Boolean = false,
) : RollRecord {
    override val total: Int get() = results.sumOf { it.second } + formula.modifier
    override val breakdown: String
        get() = buildString {
            append(formula.text)
            append(": ")
            append(results.joinToString(" + ") { it.second.toString() })
            if (formula.modifier != 0) append(" ${spaced(formula.modifier)}")
            append(" = $total")
        }
}

/** Rola dados. Recebe o gerador de números para os testes poderem controlar o resultado. */
class DiceRoller(private val random: Random = Random.Default) {
    private var nextId = 1L

    fun die(sides: Int): Int = random.nextInt(1, sides + 1)

    fun test(label: String, bonus: Int, kind: RollKind, threatFrom: Int? = null): TestRoll =
        TestRoll(nextId++, label, kind, die(20), bonus, threatFrom)

    fun roll(label: String, formula: DiceFormula, kind: RollKind, critical: Boolean = false): DiceRoll {
        val results = formula.terms.flatMap { term -> List(term.count) { term.sides to die(term.sides) } }
        return DiceRoll(nextId++, label, kind, formula, results, critical)
    }
}

/** Rolagens prontas a partir da ficha. */
object SheetRolls {

    fun skill(roller: DiceRoller, sheet: ManualSheet, skill: Skill): TestRoll =
        roller.test(skill.displayName, SheetEngine.skill(sheet, skill).total, RollKind.SKILL)

    fun attribute(roller: DiceRoller, sheet: ManualSheet, attribute: Attribute): TestRoll =
        roller.test(attribute.displayName, SheetEngine.attribute(sheet, attribute), RollKind.ATTRIBUTE)

    fun attack(roller: DiceRoller, sheet: ManualSheet, weapon: SheetWeapon): TestRoll =
        roller.test("Ataque: ${weapon.name}", SheetEngine.attackBonus(sheet, weapon).total, RollKind.ATTACK, weapon.criticalRange)

    /** Fórmula do dano da arma, com os bônus fixos da ficha (ex.: Força). */
    fun damageFormula(sheet: ManualSheet, weapon: SheetWeapon): DiceFormula? {
        val base = DiceParser.parse(weapon.damageDice) ?: return null
        return base.copy(modifier = base.modifier + SheetEngine.damageBonus(sheet, weapon).total)
    }

    fun damage(roller: DiceRoller, sheet: ManualSheet, weapon: SheetWeapon, critical: Boolean = false): DiceRoll? {
        val formula = damageFormula(sheet, weapon) ?: return null
        val final = if (critical) formula.criticalOf(weapon.criticalMultiplier) else formula
        val label = if (critical) "Dano crítico: ${weapon.name}" else "Dano: ${weapon.name}"
        return roller.roll(label, final, RollKind.DAMAGE, critical)
    }
}

private fun spaced(value: Int): String = if (value >= 0) "+ $value" else "− ${-value}"
