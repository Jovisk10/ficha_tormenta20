package com.jovis.t20.rules

/**
 * Contas universais do Tormenta 20 Jogo do Ano, aplicadas a uma ficha preenchida pelo jogador.
 * Cada resultado é um DerivedValue: o total e a explicação.
 */
object SheetEngine {

    fun attribute(sheet: ManualSheet, attribute: Attribute): Int = sheet.attributes[attribute] ?: 0

    fun halfLevel(sheet: ManualSheet): Int = sheet.level / 2

    /** +2 do 1º ao 6º nível, +4 do 7º ao 14º, +6 do 15º em diante (p. 114). */
    fun trainingBonus(sheet: ManualSheet): Int = when (sheet.level) {
        in 1..6 -> 2
        in 7..14 -> 4
        else -> 6
    }

    // ---------- PV e PM (p. 35) ----------

    fun maxHp(sheet: ManualSheet): DerivedValue {
        val con = attribute(sheet, Attribute.CONSTITUICAO)
        val classSource = Source(SourceType.CLASS, sheet.className.ifBlank { "Classe" })
        val conSource = Source(SourceType.BASE, "Constituição")
        val parts = mutableListOf(
            Contribution("Classe (1º nível)", sheet.hpFirstLevel, classSource),
            Contribution("Constituição (1º nível)", con, conSource),
        )
        val extra = sheet.level - 1
        if (extra > 0) {
            val range = if (extra == 1) "2º nível" else "níveis 2 a ${sheet.level}"
            if (sheet.hpPerLevel + con >= 1) {
                parts += Contribution("Classe ($range)", sheet.hpPerLevel * extra, classSource)
                parts += Contribution("Constituição ($range)", con * extra, conSource)
            } else {
                parts += Contribution("Mínimo de 1 PV por nível ($range)", extra, classSource)
            }
        }
        return build(sheet, parts, sheet.hpBonuses, StatTarget.MaxHp)
    }

    fun maxMp(sheet: ManualSheet): DerivedValue {
        val parts = mutableListOf(
            Contribution("Classe (${sheet.mpPerLevel} × ${sheet.level})", sheet.mpPerLevel * sheet.level, Source(SourceType.CLASS, "Classe")),
        )
        sheet.mpAttribute?.let {
            parts += Contribution(it.displayName, attribute(sheet, it), Source(SourceType.BASE, it.displayName))
        }
        return build(sheet, parts, sheet.mpBonuses, StatTarget.MaxMp)
    }

    fun currentHp(sheet: ManualSheet): Int = sheet.currentHp ?: maxHp(sheet).total

    fun currentMp(sheet: ManualSheet): Int = sheet.currentMp ?: maxMp(sheet).total

    // ---------- Defesa (p. 106, 152) ----------

    fun defense(sheet: ManualSheet): DerivedValue {
        val armor = sheet.armor
        val parts = mutableListOf(Contribution("Base", 10, Source(SourceType.BASE, "Regra de Defesa")))
        parts += if (armor?.heavy == true) {
            Contribution("Destreza (não se aplica com armadura pesada)", 0, Source(SourceType.ITEM, armor.name))
        } else {
            Contribution("Destreza", attribute(sheet, Attribute.DESTREZA), Source(SourceType.BASE, "Destreza"))
        }
        armor?.let { parts += Contribution(it.name, it.defenseBonus, Source(SourceType.ITEM, it.name)) }
        sheet.shield?.let { parts += Contribution(it.name, it.defenseBonus, Source(SourceType.ITEM, it.name)) }
        return build(sheet, parts, sheet.defenseBonuses, StatTarget.Defense)
    }

    // ---------- Carga e deslocamento (p. 141, 152) ----------

    /** 10 espaços +2 por ponto de Força (ou –1 por ponto de Força negativo). */
    fun loadLimit(sheet: ManualSheet): DerivedValue {
        val str = attribute(sheet, Attribute.FORCA)
        return DerivedValue(
            listOf(
                Contribution("Base", 10, Source(SourceType.BASE, "Regra de carga")),
                Contribution(
                    if (str >= 0) "Força (+2 por ponto)" else "Força (–1 por ponto negativo)",
                    if (str >= 0) str * 2 else str,
                    Source(SourceType.BASE, "Força"),
                ),
            ),
        )
    }

    fun maxLoad(sheet: ManualSheet): Int = loadLimit(sheet).total * 2

    fun currentLoad(sheet: ManualSheet): Double = sheet.inventory.sumOf { it.slots * it.quantity }

    fun isOverloaded(sheet: ManualSheet): Boolean = currentLoad(sheet) > loadLimit(sheet).total

    fun speed(sheet: ManualSheet): DerivedValue {
        val parts = mutableListOf(Contribution("Base", sheet.baseSpeed, Source(SourceType.BASE, "Deslocamento")))
        sheet.armor?.takeIf { it.heavy }?.let { parts += Contribution("Armadura pesada", -3, Source(SourceType.ITEM, it.name)) }
        if (isOverloaded(sheet)) parts += Contribution("Sobrecarregado", -3, Source(SourceType.CONDITION, "Sobrecarregado"))
        return DerivedValue(parts)
    }

    // ---------- Perícias (p. 114-115) ----------

    fun isTrained(sheet: ManualSheet, skill: Skill): Boolean = skill in sheet.trainedSkills

    fun canUse(sheet: ManualSheet, skill: Skill): Boolean = !skill.trainedOnly || isTrained(sheet, skill)

    fun skill(sheet: ManualSheet, skill: Skill): DerivedValue {
        val parts = mutableListOf(
            Contribution("Metade do nível", halfLevel(sheet), Source(SourceType.LEVEL, "Nível ${sheet.level}")),
            Contribution(skill.keyAttribute.displayName, attribute(sheet, skill.keyAttribute), Source(SourceType.BASE, skill.keyAttribute.displayName)),
        )
        if (isTrained(sheet, skill)) {
            parts += Contribution("Treinamento", trainingBonus(sheet), Source(SourceType.LEVEL, "Treinamento"))
        }
        if (skill.armorPenalty) {
            sheet.armor?.takeIf { it.armorPenalty != 0 }?.let {
                parts += Contribution("Penalidade de armadura", it.armorPenalty, Source(SourceType.ITEM, it.name))
            }
            sheet.shield?.takeIf { it.armorPenalty != 0 }?.let {
                parts += Contribution("Penalidade de escudo", it.armorPenalty, Source(SourceType.ITEM, it.name))
            }
            if (isOverloaded(sheet)) {
                parts += Contribution("Sobrecarregado", -5, Source(SourceType.CONDITION, "Sobrecarregado"))
            }
        }
        return build(sheet, parts, sheet.skillBonuses[skill].orEmpty(), StatTarget.SkillTarget(skill))
    }

    fun allSkills(sheet: ManualSheet): Map<Skill, DerivedValue> = Skill.entries.associateWith { skill(sheet, it) }

    // ---------- Ataques (p. 142, 230) ----------

    fun attackBonus(sheet: ManualSheet, weapon: SheetWeapon): DerivedValue {
        val attackSkill = if (weapon.mode == AttackMode.MELEE) Skill.LUTA else Skill.PONTARIA
        val parts = listOf(
            Contribution(attackSkill.displayName, skill(sheet, attackSkill).total, Source(SourceType.BASE, attackSkill.displayName)),
        )
        return build(sheet, parts, weapon.attackBonuses, StatTarget.Attack)
    }

    fun damageBonus(sheet: ManualSheet, weapon: SheetWeapon): DerivedValue {
        val parts = if (weapon.mode != AttackMode.FIRING) {
            listOf(Contribution("Força", attribute(sheet, Attribute.FORCA), Source(SourceType.BASE, "Força")))
        } else {
            emptyList()
        }
        return build(sheet, parts, weapon.damageBonuses, StatTarget.WeaponDamage)
    }

    fun damageText(sheet: ManualSheet, weapon: SheetWeapon): String {
        val bonus = damageBonus(sheet, weapon).total
        return when {
            bonus > 0 -> "${weapon.damageDice}+$bonus"
            bonus < 0 -> "${weapon.damageDice}$bonus"
            else -> weapon.damageDice
        }
    }

    fun criticalText(weapon: SheetWeapon): String = "${weapon.criticalRange}/x${weapon.criticalMultiplier}"

    // ---------- Magias (p. 170, 227) ----------

    fun spellDc(sheet: ManualSheet): DerivedValue? {
        val attribute = sheet.spellAttribute ?: return null
        val parts = listOf(
            Contribution("Base", 10, Source(SourceType.BASE, "Regra de CD")),
            Contribution("Metade do nível", halfLevel(sheet), Source(SourceType.LEVEL, "Nível ${sheet.level}")),
            Contribution(attribute.displayName, attribute(sheet, attribute), Source(SourceType.BASE, attribute.displayName)),
        )
        return build(sheet, parts, sheet.spellDcBonuses, StatTarget.SpellDc)
    }

    fun spellCost(spell: SheetSpell): Int = SpellCost.of(spell.circle)

    // ---------- Infraestrutura ----------

    /** Valor efetivo de um bônus: bônus por nível são multiplicados pelo nível atual. */
    fun bonusValue(sheet: ManualSheet, bonus: SheetBonus): Int = if (bonus.perLevel) bonus.value * sheet.level else bonus.value

    private fun bonusLabel(sheet: ManualSheet, bonus: SheetBonus): String =
        if (bonus.perLevel) "${bonus.label} (${signed(bonus.value)} × ${sheet.level})" else bonus.label

    private fun build(sheet: ManualSheet, parts: List<Contribution>, bonuses: List<SheetBonus>, target: StatTarget): DerivedValue {
        val (situational, fixed) = bonuses.partition { it.situation != null }
        return DerivedValue(
            contributions = parts + fixed.map {
                Contribution(bonusLabel(sheet, it), bonusValue(sheet, it), Source(SourceType.MANUAL, it.label))
            },
            situational = situational.map {
                Modifier(target, bonusValue(sheet, it), Source(SourceType.MANUAL, bonusLabel(sheet, it)), it.situation)
            },
        )
    }
}
