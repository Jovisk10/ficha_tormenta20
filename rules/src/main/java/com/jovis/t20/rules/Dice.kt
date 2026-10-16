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

    /**
     * No crítico, multiplicam-se só os dados da arma (o primeiro termo); bônus numéricos
     * e dados extras, como Ataque Furtivo, não são multiplicados (p. 142).
     */
    fun criticalOf(multiplier: Int): DiceFormula =
        copy(terms = terms.mapIndexed { i, t -> if (i == 0) t.copy(count = t.count * multiplier) else t })
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
    /** Posição da arma em `sheet.weapons` no momento do ataque (só em ataques). Ver [SheetRolls.weaponOf]. */
    val weaponIndex: Int? = null,
    /** Ataque feito com a arma adaptável nas duas mãos (p. 143). */
    val twoHanded: Boolean = false,
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

    /**
     * [twoHanded]: empunhar uma arma adaptável com as duas mãos (p. 143). Só muda o rótulo; a ficha
     * manual não controla proficiência, então não há –5 a remover aqui. O teste guarda a empunhadura
     * efetiva (só vale em arma que pode ser usada com as duas mãos).
     *
     * [weaponIndex]: posição da arma em `sheet.weapons`. Se não vier, procura a própria instância
     * da arma na lista (por identidade, não por igualdade, para distinguir duas armas iguais).
     */
    fun attack(
        roller: DiceRoller,
        sheet: ManualSheet,
        weapon: SheetWeapon,
        twoHanded: Boolean = false,
        weaponIndex: Int? = null,
    ): TestRoll {
        val grip = twoHanded && weapon.canUseTwoHanded
        val index = weaponIndex?.takeIf { it in sheet.weapons.indices }
            ?: sheet.weapons.indexOfFirst { it === weapon }.takeIf { it >= 0 }
        return roller.test("Ataque: ${weapon.name}${gripSuffix(weapon, twoHanded)}", SheetEngine.attackBonus(sheet, weapon).total, RollKind.ATTACK, weapon.criticalRange)
            .copy(weaponIndex = index, twoHanded = grip)
    }

    /**
     * A arma de um teste de ataque, pelo índice guardado em [TestRoll.weaponIndex].
     * Não compara rótulos: funciona com armas de mesmo nome e com arma renomeada depois do ataque.
     * Devolve null se não for ataque, se o ataque não tiver índice ou se o índice não existir mais
     * na lista (arma apagada). Se a lista foi reordenada, o índice aponta para a arma que está
     * naquela posição agora; o histórico vive só na memória, então isso só ocorre ao editar as armas
     * no meio da sessão.
     */
    fun weaponOf(sheet: ManualSheet, roll: TestRoll): SheetWeapon? {
        if (roll.kind != RollKind.ATTACK) return null
        return roll.weaponIndex?.let { sheet.weapons.getOrNull(it) }
    }

    /** Fórmula do dano da arma, com os bônus fixos da ficha (ex.: Força). Com as duas mãos, usa o segundo dano (p. 143). */
    fun damageFormula(sheet: ManualSheet, weapon: SheetWeapon, twoHanded: Boolean = false): DiceFormula? {
        val base = DiceParser.parse(weapon.damageDiceFor(twoHanded)) ?: return null
        return base.copy(modifier = base.modifier + SheetEngine.damageBonus(sheet, weapon).total)
    }

    fun damage(roller: DiceRoller, sheet: ManualSheet, weapon: SheetWeapon, critical: Boolean = false, twoHanded: Boolean = false): DiceRoll? {
        val formula = damageFormula(sheet, weapon, twoHanded) ?: return null
        val final = if (critical) formula.criticalOf(weapon.criticalMultiplier) else formula
        val name = "${weapon.name}${gripSuffix(weapon, twoHanded)}"
        val label = if (critical) "Dano crítico: $name" else "Dano: $name"
        return roller.roll(label, final, RollKind.DAMAGE, critical)
    }

    private fun gripSuffix(weapon: SheetWeapon, twoHanded: Boolean): String =
        if (twoHanded && weapon.canUseTwoHanded) " (duas mãos)" else ""
}

private fun spaced(value: Int): String = if (value >= 0) "+ $value" else "− ${-value}"
