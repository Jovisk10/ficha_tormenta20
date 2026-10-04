package com.jovis.t20.rules

/**
 * Converte a ficha do jogador para JSON e de volta.
 * A leitura é tolerante: campos ausentes recebem o valor padrão e nomes desconhecidos
 * são ignorados, para que fichas antigas continuem abrindo quando o app evoluir.
 */
object SheetCodec {
    const val VERSION = 1

    fun encode(sheet: ManualSheet): String = JsonWriter.write(toJson(sheet))

    fun decode(text: String): ManualSheet {
        val root = JsonParser.parse(text) as? Json.Obj ?: throw JsonParseException("A ficha precisa ser um objeto JSON.")
        return fromJson(root)
    }

    // ---------- Escrita ----------

    fun toJson(s: ManualSheet): Json.Obj = Json.obj(
        "version" to Json.num(VERSION),
        "name" to Json.str(s.name),
        "player" to Json.str(s.player),
        "race" to Json.str(s.race),
        "origin" to Json.str(s.origin),
        "className" to Json.str(s.className),
        "level" to Json.num(s.level),
        "deity" to Json.str(s.deity),
        "raceId" to Json.str(s.raceId),
        "classId" to Json.str(s.classId),
        "classPathId" to Json.str(s.classPathId),
        "originId" to Json.str(s.originId),
        "attributes" to Json.Obj(Attribute.entries.associate { it.name to Json.num(s.attributes[it] ?: 0) }),
        "attributeMode" to Json.str(s.attributeMode?.name),
        "baseAttributes" to Json.Obj(Attribute.entries.associate { it.name to Json.num(s.baseAttributes[it] ?: 0) }),
        "raceAttributeChoices" to Json.arr(s.raceAttributeChoices.map { Json.str(it.name) }),
        "attributeIncreases" to Json.Obj(s.attributeIncreases.filterValues { it != 0 }.entries.associate { (k, v) -> k.name to Json.num(v) }),
        "hpFirstLevel" to Json.num(s.hpFirstLevel),
        "hpPerLevel" to Json.num(s.hpPerLevel),
        "hpBonuses" to bonuses(s.hpBonuses),
        "mpPerLevel" to Json.num(s.mpPerLevel),
        "mpAttribute" to Json.str(s.mpAttribute?.name),
        "mpBonuses" to bonuses(s.mpBonuses),
        "trainedSkills" to Json.arr(s.trainedSkills.map { Json.str(it.name) }),
        "skillPicks" to (s.skillPicks?.let { p ->
            Json.obj(
                "classFixedChoice" to Json.str(p.classFixedChoice?.name),
                "classChoices" to skills(p.classChoices),
                "origin" to skills(p.origin),
                "race" to skills(p.race),
                "intelligence" to skills(p.intelligence),
                "other" to skills(p.other),
                "raceBonus" to skills(p.raceBonus),
            )
        } ?: Json.Null),
        "skillBonuses" to Json.Obj(s.skillBonuses.filterValues { it.isNotEmpty() }.entries.associate { (k, v) -> k.name to bonuses(v) }),
        "armor" to (s.armor?.let {
            Json.obj("name" to Json.str(it.name), "defenseBonus" to Json.num(it.defenseBonus), "armorPenalty" to Json.num(it.armorPenalty), "heavy" to Json.Bool(it.heavy))
        } ?: Json.Null),
        "shield" to (s.shield?.let {
            Json.obj("name" to Json.str(it.name), "defenseBonus" to Json.num(it.defenseBonus), "armorPenalty" to Json.num(it.armorPenalty))
        } ?: Json.Null),
        "defenseBonuses" to bonuses(s.defenseBonuses),
        "baseSpeed" to Json.num(s.baseSpeed),
        "weapons" to Json.arr(s.weapons.map {
            Json.obj(
                "name" to Json.str(it.name),
                "damageDice" to Json.str(it.damageDice),
                "mode" to Json.str(it.mode.name),
                "criticalRange" to Json.num(it.criticalRange),
                "criticalMultiplier" to Json.num(it.criticalMultiplier),
                "attackBonuses" to bonuses(it.attackBonuses),
                "damageBonuses" to bonuses(it.damageBonuses),
                "notes" to Json.str(it.notes),
            )
        }),
        "inventory" to Json.arr(s.inventory.map {
            Json.obj("name" to Json.str(it.name), "quantity" to Json.num(it.quantity), "slots" to Json.num(it.slots), "equipped" to Json.Bool(it.equipped))
        }),
        "money" to Json.num(s.money),
        "spellAttribute" to Json.str(s.spellAttribute?.name),
        "spellSchools" to Json.arr(s.spellSchools.map { Json.str(it.name) }),
        "spellDcBonuses" to bonuses(s.spellDcBonuses),
        "spells" to Json.arr(s.spells.map {
            Json.obj(
                "name" to Json.str(it.name), "circle" to Json.num(it.circle), "school" to Json.str(it.school),
                "notes" to Json.str(it.notes), "type" to Json.str(it.type), "execution" to Json.str(it.execution),
                "range" to Json.str(it.range), "target" to Json.str(it.target), "duration" to Json.str(it.duration),
                "resistance" to Json.str(it.resistance), "summary" to Json.str(it.summary),
                "page" to (it.page?.let { p -> Json.num(p) } ?: Json.Null),
            )
        }),
        "abilities" to Json.arr(s.abilities.map { Json.obj("name" to Json.str(it.name), "description" to Json.str(it.description)) }),
        "deityId" to Json.str(s.deityId),
        "grantedPowers" to Json.arr(s.grantedPowers.map { Json.str(it) }),
        "originPowers" to Json.arr(s.originPowers.map { Json.str(it) }),
        "classPowers" to Json.arr(s.classPowers.map { Json.str(it) }),
        "currentHp" to (s.currentHp?.let { Json.num(it) } ?: Json.Null),
        "currentMp" to (s.currentMp?.let { Json.num(it) } ?: Json.Null),
        "tempHp" to Json.num(s.tempHp),
        "tempMp" to Json.num(s.tempMp),
        "notes" to Json.str(s.notes),
    )

    private fun skills(set: Set<Skill>): Json = Json.arr(set.map { Json.str(it.name) })

    private fun readSkills(items: List<Json>): Set<Skill> =
        items.mapNotNull { enumOrNull<Skill>((it as? Json.Str)?.value) }.toSet()

    private fun readStrings(items: List<Json>): Set<String> = items.mapNotNull { (it as? Json.Str)?.value }.toSet()

    private fun bonuses(list: List<SheetBonus>): Json = Json.arr(list.map {
        Json.obj(
            "label" to Json.str(it.label), "value" to Json.num(it.value),
            "situation" to Json.str(it.situation), "perLevel" to Json.Bool(it.perLevel),
        )
    })

    // ---------- Leitura ----------

    fun fromJson(o: Json.Obj): ManualSheet {
        val attributes = o.obj("attributes")
        return ManualSheet(
            name = o.string("name", "Sem nome"),
            player = o.string("player"),
            race = o.string("race"),
            origin = o.string("origin"),
            className = o.string("className"),
            level = o.int("level", 1).coerceIn(1, 20),
            deity = o.string("deity"),
            raceId = o.stringOrNull("raceId"),
            classId = o.stringOrNull("classId"),
            classPathId = o.stringOrNull("classPathId"),
            originId = o.stringOrNull("originId"),
            attributes = Attribute.entries.associateWith { attributes?.int(it.name, 0) ?: 0 },
            attributeMode = enumOrNull<AttributeMethod>(o.stringOrNull("attributeMode")),
            baseAttributes = Attribute.entries.associateWith { o.obj("baseAttributes")?.int(it.name, 0) ?: 0 },
            raceAttributeChoices = o.list("raceAttributeChoices").mapNotNull { enumOrNull<Attribute>((it as? Json.Str)?.value) }.toSet(),
            attributeIncreases = o.obj("attributeIncreases")?.fields?.mapNotNull { (k, v) ->
                enumOrNull<Attribute>(k)?.let { a -> a to ((v as? Json.Num)?.value?.toInt() ?: 0) }
            }?.toMap().orEmpty(),
            hpFirstLevel = o.int("hpFirstLevel"),
            hpPerLevel = o.int("hpPerLevel"),
            hpBonuses = readBonuses(o.list("hpBonuses")),
            mpPerLevel = o.int("mpPerLevel"),
            mpAttribute = enumOrNull<Attribute>(o.stringOrNull("mpAttribute")),
            mpBonuses = readBonuses(o.list("mpBonuses")),
            trainedSkills = o.list("trainedSkills").mapNotNull { enumOrNull<Skill>((it as? Json.Str)?.value) }.toSet(),
            skillPicks = o.obj("skillPicks")?.let { p ->
                SkillPicks(
                    classFixedChoice = enumOrNull<Skill>(p.stringOrNull("classFixedChoice")),
                    classChoices = readSkills(p.list("classChoices")),
                    origin = readSkills(p.list("origin")),
                    race = readSkills(p.list("race")),
                    intelligence = readSkills(p.list("intelligence")),
                    other = readSkills(p.list("other")),
                    raceBonus = readSkills(p.list("raceBonus")),
                )
            },
            skillBonuses = o.obj("skillBonuses")?.fields?.mapNotNull { (k, v) ->
                enumOrNull<Skill>(k)?.let { skill -> skill to readBonuses((v as? Json.Arr)?.items.orEmpty()) }
            }?.toMap().orEmpty(),
            armor = o.obj("armor")?.let {
                SheetArmor(it.string("name"), it.int("defenseBonus"), it.int("armorPenalty"), it.bool("heavy"))
            },
            shield = o.obj("shield")?.let { SheetShield(it.string("name"), it.int("defenseBonus"), it.int("armorPenalty")) },
            defenseBonuses = readBonuses(o.list("defenseBonuses")),
            baseSpeed = o.int("baseSpeed", 9),
            weapons = o.objects("weapons").map {
                SheetWeapon(
                    name = it.string("name"),
                    damageDice = it.string("damageDice"),
                    mode = enumOrNull<AttackMode>(it.stringOrNull("mode")) ?: AttackMode.MELEE,
                    criticalRange = it.int("criticalRange", 20),
                    criticalMultiplier = it.int("criticalMultiplier", 2),
                    attackBonuses = readBonuses(it.list("attackBonuses")),
                    damageBonuses = readBonuses(it.list("damageBonuses")),
                    notes = it.string("notes"),
                )
            },
            inventory = o.objects("inventory").map {
                SheetItem(it.string("name"), it.int("quantity", 1).coerceAtLeast(1), it.double("slots", 1.0), it.bool("equipped"))
            },
            money = o.int("money"),
            spellAttribute = enumOrNull<Attribute>(o.stringOrNull("spellAttribute")),
            spellSchools = o.list("spellSchools").mapNotNull { enumOrNull<SpellSchool>((it as? Json.Str)?.value) }.toSet(),
            spellDcBonuses = readBonuses(o.list("spellDcBonuses")),
            spells = o.objects("spells").map {
                SheetSpell(
                    name = it.string("name"), circle = it.int("circle", 1).coerceIn(1, 5), school = it.string("school"),
                    notes = it.string("notes"), type = it.string("type"), execution = it.string("execution"),
                    range = it.string("range"), target = it.string("target"), duration = it.string("duration"),
                    resistance = it.string("resistance"), summary = it.string("summary"),
                    page = (it.fields["page"] as? Json.Num)?.value?.toInt(),
                )
            },
            abilities = o.objects("abilities").map { SheetEntry(it.string("name"), it.string("description")) },
            deityId = o.stringOrNull("deityId"),
            grantedPowers = readStrings(o.list("grantedPowers")),
            originPowers = readStrings(o.list("originPowers")),
            classPowers = o.list("classPowers").mapNotNull { (it as? Json.Str)?.value },
            currentHp = (o.fields["currentHp"] as? Json.Num)?.value?.toInt(),
            currentMp = (o.fields["currentMp"] as? Json.Num)?.value?.toInt(),
            tempHp = o.int("tempHp"),
            tempMp = o.int("tempMp"),
            notes = o.string("notes"),
        )
    }

    private fun readBonuses(items: List<Json>): List<SheetBonus> = items.filterIsInstance<Json.Obj>().map {
        SheetBonus(it.string("label"), it.int("value"), it.stringOrNull("situation"), it.bool("perLevel"))
    }

    private inline fun <reified E : Enum<E>> enumOrNull(name: String?): E? =
        name?.let { n -> enumValues<E>().firstOrNull { it.name == n } }
}
