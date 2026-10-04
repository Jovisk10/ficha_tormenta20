package com.jovis.t20.ui.edit

import com.jovis.t20.ui.theme.T20Fonts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jovis.t20.rules.ArmorCatalog
import com.jovis.t20.rules.Attribute
import com.jovis.t20.rules.CatalogApply
import com.jovis.t20.rules.CatalogClass
import com.jovis.t20.rules.CatalogOrigin
import com.jovis.t20.rules.CatalogRace
import com.jovis.t20.rules.ClassCatalog
import com.jovis.t20.rules.DeityCatalog
import com.jovis.t20.rules.PowerCatalog
import com.jovis.t20.rules.SkillGroupId
import com.jovis.t20.rules.OriginCatalog
import com.jovis.t20.rules.RaceCatalog
import com.jovis.t20.rules.SpellCatalog
import com.jovis.t20.rules.SpellSchool
import com.jovis.t20.rules.AttackMode
import com.jovis.t20.rules.ManualSheet
import com.jovis.t20.rules.SheetArmor
import com.jovis.t20.rules.SheetCodec
import com.jovis.t20.rules.SheetEngine
import com.jovis.t20.rules.SheetEntry
import com.jovis.t20.rules.SheetItem
import com.jovis.t20.rules.SheetShield
import com.jovis.t20.rules.SheetSpell
import com.jovis.t20.rules.SheetWeapon
import com.jovis.t20.rules.Skill

/** Guarda o rascunho ao girar a tela, usando o mesmo formato de salvamento das fichas. */
private val SheetSaver = Saver<ManualSheet, String>(
    save = { SheetCodec.encode(it) },
    restore = { SheetCodec.decode(it) },
)

/** Item de uma lista sendo editado num diálogo. index = -1 para um item novo. */
private data class Editing<T>(val index: Int, val item: T)

private fun <T> List<T>.upsert(index: Int, item: T): List<T> =
    if (index < 0) this + item else toMutableList().also { it[index] = item }

private fun <T> List<T>.without(index: Int): List<T> = filterIndexed { i, _ -> i != index }

@Composable
fun EditSheetScreen(
    initial: ManualSheet,
    onSave: (ManualSheet) -> Unit,
    onCancel: () -> Unit,
) {
    var draft by rememberSaveable(stateSaver = SheetSaver) { mutableStateOf(initial) }

    var skillDialog by remember { mutableStateOf<Skill?>(null) }
    var weaponDialog by remember { mutableStateOf<Editing<SheetWeapon>?>(null) }
    var spellDialog by remember { mutableStateOf<Editing<SheetSpell>?>(null) }
    var abilityDialog by remember { mutableStateOf<Editing<SheetEntry>?>(null) }
    var itemDialog by remember { mutableStateOf<Editing<SheetItem>?>(null) }
    var catalogDialog by remember { mutableStateOf<CatalogDialog?>(null) }
    var deityDialog by remember { mutableStateOf(false) }
    var skillGroupDialog by remember { mutableStateOf<SkillGroupId?>(null) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            // Barra de ações
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Text(
                    "Editar ficha",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = T20Fonts.display,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                )
                Button(onClick = { onSave(draft.copy(name = draft.name.ifBlank { "Sem nome" })) }) { Text("Salvar") }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    EditSection("Identidade") {
                        TextInput("Nome do personagem", draft.name, { draft = draft.copy(name = it) })
                        TextInput("Jogador", draft.player, { draft = draft.copy(player = it) })
                        // Raça
                        OptionPicker(
                            label = "Raça do livro",
                            options = listOf<CatalogRace?>(null) + RaceCatalog.ALL,
                            selected = RaceCatalog.byId(draft.raceId),
                            optionLabel = { it?.name ?: "Outra" },
                            onSelect = { race -> draft = if (race == null) draft.copy(raceId = null) else CatalogApply.applyRace(draft, race) },
                            perRow = 3,
                        )
                        RaceCatalog.byId(draft.raceId)?.let { race ->
                            Hint("Confira os atributos: ${race.attributeText} As habilidades da raça foram adicionadas. (p. ${race.page})")
                        }
                        if (draft.raceId == null) TextInput("Raça", draft.race, { draft = draft.copy(race = it) })

                        // Classe
                        OptionPicker(
                            label = "Classe do livro",
                            options = listOf<CatalogClass?>(null) + ClassCatalog.ALL,
                            selected = ClassCatalog.byId(draft.classId),
                            optionLabel = { it?.name ?: "Outra" },
                            onSelect = { cls -> draft = if (cls == null) draft.copy(classId = null) else CatalogApply.applyClass(draft, cls) },
                            perRow = 3,
                        )
                        ClassCatalog.byId(draft.classId)?.let { cls ->
                            Hint(classHint(cls))
                            if (cls.paths.isNotEmpty()) {
                                OptionPicker(
                                    label = "Caminho",
                                    options = cls.paths,
                                    selected = cls.pathById(draft.classPathId) ?: cls.paths.first().copy(id = ""),
                                    optionLabel = { it.name },
                                    onSelect = { path -> draft = CatalogApply.applyPath(draft, path) },
                                    perRow = 3,
                                )
                                val path = cls.pathById(draft.classPathId)
                                Hint(path?.summary ?: "Escolha o caminho para definir o atributo das magias e liberar a lista de magias.")
                            }
                        }
                        if (draft.classId == null) TextInput("Classe", draft.className, { draft = draft.copy(className = it) })

                        // Origem
                        OptionPicker(
                            label = "Origem do livro",
                            options = listOf<CatalogOrigin?>(null) + OriginCatalog.ALL,
                            selected = OriginCatalog.byId(draft.originId),
                            optionLabel = { it?.name ?: "Outra" },
                            onSelect = { origin -> draft = if (origin == null) draft.copy(originId = null) else CatalogApply.applyOrigin(draft, origin) },
                            perRow = 3,
                        )
                        OriginCatalog.byId(draft.originId)?.let { origin ->
                            Hint(
                                "A origem dá ${origin.benefitCount} benefícios, entre perícias (escolhidas em Perícias: " +
                                    "${origin.skillOptions.joinToString(", ") { it.displayName }}) e poderes. Itens: ${origin.items} (p. ${origin.page})"
                            )
                            val catalogPowers = origin.powerOptions.mapNotNull { PowerCatalog.origin(it) }
                            val extras = origin.powerOptions.filter { PowerCatalog.origin(it) == null }
                            PowerChoiceList(
                                title = "Poderes da origem",
                                powers = catalogPowers,
                                selected = draft.originPowers,
                                limit = CatalogApply.originPowerLimit(draft),
                                onToggle = { name, on -> draft = CatalogApply.setOriginPower(draft, name, on) },
                            )
                            if (extras.isNotEmpty()) {
                                Hint("Também disponível: ${extras.joinToString(", ")}. Registre em Habilidades e poderes.")
                            }
                        }
                        if (draft.originId == null) TextInput("Origem", draft.origin, { draft = draft.copy(origin = it) })
                        NumberStepper("Nível", draft.level, { draft = CatalogApply.refresh(draft.copy(level = it)) }, 1..20)

                        // Divindade
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        val deity = DeityCatalog.byId(draft.deityId)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Divindade", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(deity?.name ?: draft.deity.ifBlank { "Nenhuma" }, style = MaterialTheme.typography.titleMedium)
                            }
                            OutlinedButton(onClick = { deityDialog = true }) { Text("Escolher") }
                        }
                        if (deity == null) {
                            TextInput("Divindade (texto livre)", draft.deity, { draft = draft.copy(deity = it) })
                        } else {
                            Hint("Obrigações e restrições: ${deity.obligations} (p. ${deity.page})")
                            if (!DeityCatalog.canBeDevote(deity, draft.raceId, draft.classId)) {
                                Text(
                                    "Pelas regras, nem a raça nem a classe deste personagem podem ser devotas de ${deity.name}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                            PowerChoiceList(
                                title = "Poderes concedidos",
                                powers = deity.powers.mapNotNull { PowerCatalog.granted(it) },
                                selected = draft.grantedPowers,
                                limit = CatalogApply.grantedPowerLimit(draft),
                                onToggle = { name, on -> draft = CatalogApply.setGrantedPower(draft, name, on) },
                            )
                        }
                    }
                }

                item {
                    EditSection("Atributos") {
                        AttributesEditor(draft) { draft = it }
                    }
                }

                item {
                    EditSection("Pontos de vida") {
                        NumberStepper("PV no 1º nível (da classe)", draft.hpFirstLevel, { draft = draft.copy(hpFirstLevel = it) }, 0..40)
                        NumberStepper("PV por nível seguinte", draft.hpPerLevel, { draft = draft.copy(hpPerLevel = it) }, 0..20)
                        Text(
                            "A Constituição é somada automaticamente. Total atual: ${SheetEngine.maxHp(draft).total} PV.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        BonusListEditor("Bônus em PV", draft.hpBonuses) { draft = draft.copy(hpBonuses = it) }
                    }
                }

                item {
                    EditSection("Pontos de mana") {
                        NumberStepper("PM por nível", draft.mpPerLevel, { draft = draft.copy(mpPerLevel = it) }, 0..20)
                        AttributePicker("Atributo somado aos PM", draft.mpAttribute) { draft = draft.copy(mpAttribute = it) }
                        Text(
                            "Total atual: ${SheetEngine.maxMp(draft).total} PM.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        BonusListEditor("Bônus em PM", draft.mpBonuses) { draft = draft.copy(mpBonuses = it) }
                    }
                }

                item {
                    EditSection("Defesa e deslocamento") {
                        SwitchRow("Usa armadura", draft.armor != null) { on ->
                            draft = draft.copy(armor = if (on) SheetArmor("Armadura", 1) else null)
                        }
                        draft.armor?.let { armor ->
                            TextInput("Nome da armadura", armor.name, { draft = draft.copy(armor = armor.copy(name = it)) })
                            NumberStepper("Bônus na Defesa", armor.defenseBonus, { draft = draft.copy(armor = armor.copy(defenseBonus = it)) }, 0..20, showSign = true)
                            NumberStepper("Penalidade de armadura", armor.armorPenalty, { draft = draft.copy(armor = armor.copy(armorPenalty = it)) }, -10..0)
                            SwitchRow("Armadura pesada", armor.heavy) { draft = draft.copy(armor = armor.copy(heavy = it)) }
                        }
                        OutlinedButton(onClick = { catalogDialog = CatalogDialog.ARMOR }) { Text("Escolher armadura do livro") }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SwitchRow("Usa escudo", draft.shield != null) { on ->
                            draft = draft.copy(shield = if (on) SheetShield("Escudo", 1, -1) else null)
                        }
                        draft.shield?.let { shield ->
                            TextInput("Nome do escudo", shield.name, { draft = draft.copy(shield = shield.copy(name = it)) })
                            NumberStepper("Bônus na Defesa", shield.defenseBonus, { draft = draft.copy(shield = shield.copy(defenseBonus = it)) }, 0..10, showSign = true)
                            NumberStepper("Penalidade de armadura", shield.armorPenalty, { draft = draft.copy(shield = shield.copy(armorPenalty = it)) }, -10..0)
                        }
                        OutlinedButton(onClick = { catalogDialog = CatalogDialog.SHIELD }) { Text("Escolher escudo do livro") }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        BonusListEditor("Outros bônus na Defesa", draft.defenseBonuses) { draft = draft.copy(defenseBonuses = it) }
                        Text(
                            "Defesa atual: ${SheetEngine.defense(draft).total}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        NumberStepper("Deslocamento base", draft.baseSpeed, { draft = draft.copy(baseSpeed = it) }, 0..30, step = 3, suffix = "m")
                    }
                }

                item {
                    EditSection("Perícias") {
                        val groups = CatalogApply.skillGroups(draft)
                        if (groups.isNotEmpty()) {
                            Hint("O treinamento segue as regras da classe, origem, raça e Inteligência. Toque em uma perícia da lista abaixo para adicionar bônus.")
                            groups.forEach { group -> SkillGroupRow(group) { skillGroupDialog = group.id } }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        } else {
                            Hint("Marque as treinadas. Toque no nome para adicionar bônus. Escolha uma classe do livro para aplicar as regras de treinamento.")
                        }
                        val structured = groups.isNotEmpty()
                        Skill.entries.forEach { skill ->
                            val trained = skill in draft.trainedSkills
                            val bonusCount = draft.skillBonuses[skill].orEmpty().size
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { skillDialog = skill },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = trained,
                                    enabled = !structured,
                                    onCheckedChange = { on ->
                                        draft = draft.copy(trainedSkills = if (on) draft.trainedSkills + skill else draft.trainedSkills - skill)
                                    },
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(skill.displayName, style = MaterialTheme.typography.bodyLarge)
                                    if (bonusCount > 0) {
                                        Text(
                                            if (bonusCount == 1) "1 bônus" else "$bonusCount bônus",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                val total = SheetEngine.skill(draft, skill).total
                                Text(if (total >= 0) "+$total" else "$total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (ClassCatalog.byId(draft.classId) != null) {
                    item {
                        EditSection("Poderes de classe") {
                            ClassPowersEditor(draft) { draft = it }
                        }
                    }
                }

                item {
                    EditSection("Ataques") {
                        draft.weapons.forEachIndexed { index, weapon ->
                            EditableRow(
                                weapon.name,
                                "${weapon.damageDice}, ${modeLabel(weapon.mode).lowercase()}",
                                onEdit = { weaponDialog = Editing(index, weapon) },
                                onDelete = { draft = draft.copy(weapons = draft.weapons.without(index)) },
                            )
                        }
                        Button(onClick = { catalogDialog = CatalogDialog.WEAPON }) { Text("Adicionar arma do livro") }
                        OutlinedButton(onClick = { weaponDialog = Editing(-1, SheetWeapon("", "1d6", AttackMode.MELEE)) }) { Text("Adicionar ataque personalizado") }
                    }
                }

                item {
                    EditSection("Magias") {
                        val casting = CatalogApply.casting(draft)
                        if (casting != null) {
                            if (casting.restrictsSchools) {
                                SchoolPicker(
                                    selected = draft.spellSchools,
                                    max = casting.schoolCount,
                                    onChange = { draft = draft.copy(spellSchools = it) },
                                )
                            } else {
                                Hint("Pode aprender magias ${casting.type.displayName.lowercase()}s de qualquer escola.")
                            }
                            Hint(
                                "${draft.className} de ${draft.level}º nível conhece ${casting.spellsKnown(draft.level)} magias de classe " +
                                    "e lança até o ${casting.maxCircle(draft.level)}º círculo. Magias de raça e poderes vêm à parte."
                            )
                        }
                        AttributePicker("Atributo das magias (para a CD)", draft.spellAttribute) { draft = draft.copy(spellAttribute = it) }
                        BonusListEditor("Bônus na CD", draft.spellDcBonuses) { draft = draft.copy(spellDcBonuses = it) }
                        draft.spells.forEachIndexed { index, spell ->
                            EditableRow(
                                spell.name,
                                listOf("${spell.circle}º círculo", spell.school).filter { it.isNotBlank() }.joinToString(", "),
                                onEdit = { spellDialog = Editing(index, spell) },
                                onDelete = { draft = draft.copy(spells = draft.spells.without(index)) },
                            )
                        }
                        Button(onClick = { catalogDialog = CatalogDialog.SPELL }) { Text("Adicionar magia do livro") }
                        OutlinedButton(onClick = { spellDialog = Editing(-1, SheetSpell("", 1)) }) { Text("Adicionar magia personalizada") }
                    }
                }

                item {
                    EditSection("Habilidades e poderes") {
                        draft.abilities.forEachIndexed { index, entry ->
                            EditableRow(
                                entry.name,
                                entry.description.take(60),
                                onEdit = { abilityDialog = Editing(index, entry) },
                                onDelete = { draft = draft.copy(abilities = draft.abilities.without(index)) },
                            )
                        }
                        OutlinedButton(onClick = { abilityDialog = Editing(-1, SheetEntry("")) }) { Text("Adicionar habilidade ou poder") }
                    }
                }

                item {
                    EditSection("Equipamento") {
                        draft.inventory.forEachIndexed { index, item ->
                            val qty = if (item.quantity > 1) "${item.quantity}× " else ""
                            EditableRow(
                                qty + item.name,
                                "${formatSlots(item.slots * item.quantity)} espaço(s)" + if (item.equipped) ", vestido" else "",
                                onEdit = { itemDialog = Editing(index, item) },
                                onDelete = { draft = draft.copy(inventory = draft.inventory.without(index)) },
                            )
                        }
                        Button(onClick = { catalogDialog = CatalogDialog.ITEM }) { Text("Adicionar item do livro") }
                        OutlinedButton(onClick = { itemDialog = Editing(-1, SheetItem("")) }) { Text("Adicionar item personalizado") }
                        IntInput("Tibares (T$)", draft.money, { draft = draft.copy(money = it) })
                    }
                }

                item {
                    EditSection("Anotações") {
                        TextInput("Anotações", draft.notes, { draft = draft.copy(notes = it) }, singleLine = false, minLines = 4)
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    // ---------- Diálogos ----------

    when (catalogDialog) {
        CatalogDialog.SPELL -> {
            val casting = CatalogApply.casting(draft)
            val needsPath = ClassCatalog.byId(draft.classId)?.paths?.isNotEmpty() == true && draft.classPathId == null
            val explanation = when {
                needsPath -> "Escolha o caminho da classe (em Identidade) para filtrar as magias."
                casting == null -> "Escolha uma classe conjuradora do livro para filtrar as magias."
                casting.restrictsSchools && draft.spellSchools.isEmpty() -> "Escolha as escolas de magia da ficha para filtrar a lista."
                else -> "Magias que ${draft.className.ifBlank { "o personagem" }} pode aprender: " +
                    "${casting.type.displayName.lowercase()}s ou universais" +
                    (if (casting.restrictsSchools) ", das escolas escolhidas" else "") +
                    ", até o ${casting.maxCircle(draft.level)}º círculo."
            }
            SpellPickerDialog(
                available = CatalogApply.availableSpells(draft).filter { c -> draft.spells.none { it.name == c.name } },
                all = SpellCatalog.ALL.filter { c -> draft.spells.none { it.name == c.name } },
                explanation = explanation,
                onPick = { spell ->
                    draft = draft.copy(spells = draft.spells + spell.toSheetSpell())
                    catalogDialog = null
                },
                onDismiss = { catalogDialog = null },
            )
        }
        CatalogDialog.WEAPON -> WeaponPickerDialog(
            onPick = { weapon ->
                // A arma entra nos ataques e também no equipamento, ocupando espaço
                draft = draft.copy(weapons = draft.weapons + weapon.toSheetWeapons(), inventory = draft.inventory + weapon.toSheetItem())
                catalogDialog = null
            },
            onDismiss = { catalogDialog = null },
        )
        CatalogDialog.ARMOR -> ArmorPickerDialog(
            onPick = { armor ->
                val oldName = draft.armor?.name
                draft = draft.copy(
                    armor = ArmorCatalog.toSheet(armor),
                    inventory = draft.inventory.filterNot { it.name == oldName && it.equipped } + ArmorCatalog.toItem(armor),
                )
                catalogDialog = null
            },
            onDismiss = { catalogDialog = null },
        )
        CatalogDialog.SHIELD -> ShieldPickerDialog(
            onPick = { shield ->
                val oldName = draft.shield?.name
                draft = draft.copy(
                    shield = ArmorCatalog.toSheet(shield),
                    inventory = draft.inventory.filterNot { it.name == oldName && it.equipped } + ArmorCatalog.toItem(shield),
                )
                catalogDialog = null
            },
            onDismiss = { catalogDialog = null },
        )
        CatalogDialog.ITEM -> ItemPickerDialog(
            onPick = { item ->
                draft = draft.copy(inventory = draft.inventory + item.toSheetItem())
                catalogDialog = null
            },
            onDismiss = { catalogDialog = null },
        )
        null -> Unit
    }

    if (deityDialog) {
        DeityPickerDialog(
            raceId = draft.raceId,
            classId = draft.classId,
            onPick = { deity ->
                draft = CatalogApply.setDeity(draft, deity)
                deityDialog = false
            },
            onDismiss = { deityDialog = false },
        )
    }

    skillGroupDialog?.let { groupId ->
        val group = CatalogApply.skillGroups(draft).firstOrNull { it.id == groupId }
        if (group == null) {
            skillGroupDialog = null
        } else {
            SkillGroupDialog(
                group = group,
                blockedReason = { skill -> CatalogApply.blockedReason(draft, groupId, skill) },
                onToggle = { skill, on ->
                    val selected = if (on) group.selected + skill else group.selected - skill
                    draft = CatalogApply.setSkillGroup(draft, groupId, selected)
                },
                onDismiss = { skillGroupDialog = null },
            )
        }
    }

    skillDialog?.let { skill ->
        AlertDialog(
            onDismissRequest = { skillDialog = null },
            title = { Text(skill.displayName) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (CatalogApply.skillGroups(draft).isEmpty()) {
                        SwitchRow("Treinada", skill in draft.trainedSkills) { on ->
                            draft = draft.copy(trainedSkills = if (on) draft.trainedSkills + skill else draft.trainedSkills - skill)
                        }
                    } else {
                        Hint(if (skill in draft.trainedSkills) "Treinada (definido pelos grupos de perícias)." else "Não treinada.")
                    }
                    BonusListEditor("Bônus", draft.skillBonuses[skill].orEmpty()) { list ->
                        draft = draft.copy(skillBonuses = draft.skillBonuses + (skill to list))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { skillDialog = null }) { Text("Concluir") } },
        )
    }

    weaponDialog?.let { editing ->
        WeaponDialog(
            initial = editing.item,
            onConfirm = { weapon ->
                draft = draft.copy(weapons = draft.weapons.upsert(editing.index, weapon))
                weaponDialog = null
            },
            onDismiss = { weaponDialog = null },
        )
    }

    spellDialog?.let { editing ->
        var spell by remember(editing) { mutableStateOf(editing.item) }
        ItemDialog(
            title = if (editing.index < 0) "Nova magia" else "Editar magia",
            onConfirm = {
                draft = draft.copy(spells = draft.spells.upsert(editing.index, spell))
                spellDialog = null
            },
            onDismiss = { spellDialog = null },
        ) {
            TextInput("Nome", spell.name, { spell = spell.copy(name = it) })
            NumberStepper("Círculo", spell.circle, { spell = spell.copy(circle = it) }, 1..5)
            TextInput("Escola", spell.school, { spell = spell.copy(school = it) })
            TextInput("Anotações", spell.notes, { spell = spell.copy(notes = it) }, singleLine = false, minLines = 2)
        }
    }

    abilityDialog?.let { editing ->
        var entry by remember(editing) { mutableStateOf(editing.item) }
        ItemDialog(
            title = if (editing.index < 0) "Nova habilidade ou poder" else "Editar habilidade ou poder",
            onConfirm = {
                draft = draft.copy(abilities = draft.abilities.upsert(editing.index, entry))
                abilityDialog = null
            },
            onDismiss = { abilityDialog = null },
        ) {
            TextInput("Nome", entry.name, { entry = entry.copy(name = it) })
            TextInput("Descrição", entry.description, { entry = entry.copy(description = it) }, singleLine = false, minLines = 3)
        }
    }

    itemDialog?.let { editing ->
        var item by remember(editing) { mutableStateOf(editing.item) }
        var slotsText by remember(editing) { mutableStateOf(formatSlots(editing.item.slots)) }
        ItemDialog(
            title = if (editing.index < 0) "Novo item" else "Editar item",
            onConfirm = {
                val slots = slotsText.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: item.slots
                draft = draft.copy(inventory = draft.inventory.upsert(editing.index, item.copy(slots = slots)))
                itemDialog = null
            },
            onDismiss = { itemDialog = null },
        ) {
            TextInput("Nome", item.name, { item = item.copy(name = it) })
            NumberStepper("Quantidade", item.quantity, { item = item.copy(quantity = it) }, 1..999)
            OutlinedTextField(
                value = slotsText,
                onValueChange = { text -> slotsText = text.filter { it.isDigit() || it == ',' || it == '.' }.take(5) },
                label = { Text("Espaços por unidade (ex.: 1 ou 0,5)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            SwitchRow("Vestido", item.equipped) { item = item.copy(equipped = it) }
        }
    }
}

@Composable
private fun WeaponDialog(initial: SheetWeapon, onConfirm: (SheetWeapon) -> Unit, onDismiss: () -> Unit) {
    var weapon by remember(initial) { mutableStateOf(initial) }
    ItemDialog(
        title = if (initial.name.isBlank()) "Novo ataque" else "Editar ataque",
        onConfirm = { onConfirm(weapon) },
        onDismiss = onDismiss,
    ) {
        TextInput("Nome", weapon.name, { weapon = weapon.copy(name = it) })
        TextInput("Dano (ex.: 1d6)", weapon.damageDice, { weapon = weapon.copy(damageDice = it) })
        OptionPicker(
            label = "Tipo de ataque",
            options = AttackMode.entries,
            selected = weapon.mode,
            optionLabel = ::modeLabel,
            onSelect = { weapon = weapon.copy(mode = it) },
            perRow = 3,
        )
        Text(
            "Corpo a corpo usa Luta; arremesso e disparo usam Pontaria. Disparo não soma Força no dano.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        NumberStepper("Margem de ameaça", weapon.criticalRange, { weapon = weapon.copy(criticalRange = it) }, 2..20)
        NumberStepper("Multiplicador de crítico", weapon.criticalMultiplier, { weapon = weapon.copy(criticalMultiplier = it) }, 2..6, suffix = "×")
        BonusListEditor("Bônus no ataque", weapon.attackBonuses) { weapon = weapon.copy(attackBonuses = it) }
        BonusListEditor("Bônus no dano", weapon.damageBonuses) { weapon = weapon.copy(damageBonuses = it) }
        TextInput("Anotações", weapon.notes, { weapon = weapon.copy(notes = it) })
    }
}

/** Diálogo padrão de edição de um item de lista, com rolagem. */
@Composable
private fun ItemDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

private fun modeLabel(mode: AttackMode): String = when (mode) {
    AttackMode.MELEE -> "Corpo a corpo"
    AttackMode.THROWN -> "Arremesso"
    AttackMode.FIRING -> "Disparo"
}

private fun formatSlots(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')

/** Qual lista do catálogo está aberta. */
private enum class CatalogDialog { SPELL, WEAPON, ARMOR, SHIELD, ITEM }

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun classHint(cls: CatalogClass): String = buildString {
    append("Treinadas automaticamente: ${cls.fixedSkills.joinToString(", ") { it.displayName }}")
    if (cls.fixedChoice.isNotEmpty()) append(", mais ${cls.fixedChoice.joinToString(" ou ") { it.displayName }}")
    append(". Escolha mais ${cls.skillChoiceCount} entre: ${cls.skillChoiceList.joinToString(", ") { it.displayName }}. ")
    append("Proficiências: ${cls.proficiencies}. PV, PM e atributos de magia foram preenchidos. (p. ${cls.page})")
}

/** Escolha das escolas de magia, até o limite da classe. */
@Composable
private fun SchoolPicker(selected: Set<SpellSchool>, max: Int, onChange: (Set<SpellSchool>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Escolas de magia (${selected.size} de $max)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SpellSchool.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { school ->
                    val isSelected = school in selected
                    val modifier = Modifier.weight(1f)
                    if (isSelected) {
                        Button(onClick = { onChange(selected - school) }, modifier = modifier) { Text(school.displayName, maxLines = 1) }
                    } else {
                        OutlinedButton(
                            onClick = { onChange(selected + school) },
                            enabled = selected.size < max,
                            modifier = modifier,
                        ) { Text(school.displayName, maxLines = 1) }
                    }
                }
            }
        }
    }
}
