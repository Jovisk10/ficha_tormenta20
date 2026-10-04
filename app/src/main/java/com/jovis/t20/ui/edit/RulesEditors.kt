package com.jovis.t20.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jovis.t20.rules.Attribute
import com.jovis.t20.rules.AttributeMethod
import com.jovis.t20.rules.CatalogApply
import com.jovis.t20.rules.CatalogDeity
import com.jovis.t20.rules.CatalogPower
import com.jovis.t20.rules.DeityCatalog
import com.jovis.t20.rules.ManualSheet
import com.jovis.t20.rules.PointBuy
import com.jovis.t20.rules.RaceCatalog
import com.jovis.t20.rules.Skill
import com.jovis.t20.rules.SkillGroup
import com.jovis.t20.ui.theme.T20Fonts

@Composable
private fun RuleHint(text: String, warning: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (warning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun signed(value: Int) = if (value > 0) "+$value" else "$value"

// ============================== Atributos ==============================

/** Modo de definir atributos: compra de pontos, rolagem ou valor final digitado. */
private enum class AttributeModeOption(val label: String, val method: AttributeMethod?) {
    PONTOS("Pontos", AttributeMethod.POINT_BUY),
    ROLAGEM("Rolagem", AttributeMethod.ROLLED),
    FINAL("Valor final", null),
}

@Composable
fun AttributesEditor(sheet: ManualSheet, onChange: (ManualSheet) -> Unit) {
    val current = AttributeModeOption.entries.first { it.method == sheet.attributeMode }
    OptionPicker(
        label = "Como definir os atributos",
        options = AttributeModeOption.entries,
        selected = current,
        optionLabel = { it.label },
        onSelect = { option ->
            if (option == current) return@OptionPicker
            val updated = if (sheet.attributeMode == null && option.method != null) {
                // Ao sair do valor final, tira o ajuste racial para chegar ao valor base
                val racial = CatalogApply.racialAdjustments(sheet)
                sheet.copy(
                    attributeMode = option.method,
                    baseAttributes = Attribute.entries.associateWith { (sheet.attributes[it] ?: 0) - (racial[it] ?: 0) },
                )
            } else {
                sheet.copy(attributeMode = option.method)
            }
            onChange(CatalogApply.refresh(updated))
        },
        perRow = 3,
    )

    if (sheet.attributeMode == null) {
        RuleHint("Digite o valor final, já com os ajustes de raça e de poderes.")
        Attribute.entries.forEach { attribute ->
            NumberStepper(
                attribute.displayName,
                sheet.attributes[attribute] ?: 0,
                { onChange(CatalogApply.refresh(sheet.copy(attributes = sheet.attributes + (attribute to it)))) },
                -5..20,
            )
        }
        return
    }

    // Contador da compra de pontos
    if (sheet.attributeMode == AttributeMethod.POINT_BUY) {
        val cost = CatalogApply.pointBuyCost(sheet)
        val over = cost == null || cost > PointBuy.BUDGET
        Text(
            if (cost == null) "Há valores fora da tabela de compra." else "Pontos gastos: $cost de ${PointBuy.BUDGET}",
            style = MaterialTheme.typography.titleMedium,
            color = if (over) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        )
        RuleHint("Custos (p. 17): –1 devolve 1 ponto; 0 custa 0; 1 custa 1; 2 custa 2; 3 custa 4; 4 custa 7.")
    } else {
        RuleHint("Digite os valores rolados, antes dos ajustes de raça.")
    }

    val range = if (sheet.attributeMode == AttributeMethod.POINT_BUY) -1..4 else -2..5
    Attribute.entries.forEach { attribute ->
        val base = sheet.baseAttributes[attribute] ?: 0
        val label = if (sheet.attributeMode == AttributeMethod.POINT_BUY) {
            "${attribute.displayName} (custo ${runCatching { PointBuy.cost(base) }.getOrDefault(0)})"
        } else attribute.displayName
        NumberStepper(
            label,
            base,
            { onChange(CatalogApply.refresh(sheet.copy(baseAttributes = sheet.baseAttributes + (attribute to it)))) },
            range,
        )
    }

    // Ajustes da raça
    val race = RaceCatalog.byId(sheet.raceId)
    if (race != null) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text("Ajustes de ${race.name}", style = MaterialTheme.typography.titleSmall)
        RuleHint(race.attributeText)
        if (race.attributeChoiceCount > 0) {
            val chosen = sheet.raceAttributeChoices.filter { it !in race.attributeChoiceExcluded }
            RuleHint("Escolha ${race.attributeChoiceCount} atributos para +1 (${chosen.size} de ${race.attributeChoiceCount}).")
            Attribute.entries.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { attribute ->
                        val isChosen = attribute in chosen
                        val excluded = attribute in race.attributeChoiceExcluded
                        val modifier = Modifier.weight(1f)
                        val toggle = {
                            val set = if (isChosen) sheet.raceAttributeChoices - attribute else sheet.raceAttributeChoices + attribute
                            onChange(CatalogApply.refresh(sheet.copy(raceAttributeChoices = set)))
                        }
                        if (isChosen) {
                            Button(onClick = toggle, modifier = modifier) { Text("${attribute.abbreviation} +1") }
                        } else {
                            OutlinedButton(
                                onClick = toggle,
                                enabled = !excluded && chosen.size < race.attributeChoiceCount,
                                modifier = modifier,
                            ) { Text(attribute.abbreviation) }
                        }
                    }
                }
            }
        }
    } else {
        RuleHint("Escolha uma raça do livro para aplicar os ajustes automaticamente.")
    }

    // Aumentos por poderes (ficam recolhidos)
    var showIncreases by remember { mutableStateOf(sheet.attributeIncreases.any { it.value != 0 }) }
    TextButton(onClick = { showIncreases = !showIncreases }) {
        Text(if (showIncreases) "Ocultar aumentos por poderes" else "Aumentos por poderes (ex.: Aumento de Atributo)")
    }
    if (showIncreases) {
        Attribute.entries.forEach { attribute ->
            NumberStepper(
                "${attribute.displayName}: aumentos",
                sheet.attributeIncreases[attribute] ?: 0,
                { onChange(CatalogApply.refresh(sheet.copy(attributeIncreases = sheet.attributeIncreases + (attribute to it)))) },
                0..10,
                showSign = true,
            )
        }
    }

    // Resultado final
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Text("Valores finais", style = MaterialTheme.typography.titleSmall)
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(vertical = 8.dp),
    ) {
        Attribute.entries.forEach { attribute ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${sheet.attributes[attribute] ?: 0}",
                    fontFamily = T20Fonts.serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    attribute.abbreviation,
                    fontFamily = T20Fonts.display,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

// ============================== Perícias ==============================

@Composable
fun SkillGroupRow(group: SkillGroup, onChoose: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(group.title, style = MaterialTheme.typography.titleSmall)
            Text(
                if (group.selected.isEmpty()) "Nenhuma escolhida" else group.selected.joinToString(", ") { it.displayName },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            group.remaining?.takeIf { it > 0 && !group.locked }?.let {
                RuleHint(if (it == 1) "Falta 1 escolha." else "Faltam $it escolhas.", warning = true)
            }
        }
        if (!group.locked) OutlinedButton(onClick = onChoose) { Text("Escolher") }
    }
}

/** Todas as perícias, com as que não podem ser marcadas bloqueadas e o motivo. */
@Composable
fun SkillGroupDialog(
    group: SkillGroup,
    blockedReason: (Skill) -> String?,
    onToggle: (Skill, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(group.title) },
        text = {
            Column {
                RuleHint(group.description)
                group.limit?.let { Text("${group.selected.size} de $it", style = MaterialTheme.typography.titleMedium) }
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    // As permitidas primeiro, depois as bloqueadas
                    val sorted = Skill.entries.sortedBy { if (it in group.selected || blockedReason(it) == null) 0 else 1 }
                    sorted.forEach { skill ->
                        val selected = skill in group.selected
                        val reason = blockedReason(skill)
                        val enabled = selected || reason == null
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .clickable(enabled = enabled) { onToggle(skill, !selected) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = selected, onCheckedChange = { onToggle(skill, it) }, enabled = enabled)
                            Column(Modifier.weight(1f)) {
                                Text(
                                    skill.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                )
                                if (!enabled && reason != null) RuleHint(reason)
                            }
                            Text(skill.keyAttribute.abbreviation, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Concluir") } },
    )
}

// ============================== Divindades e poderes ==============================

@Composable
fun DeityPickerDialog(
    raceId: String?,
    classId: String?,
    onPick: (CatalogDeity?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Divindade") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                RuleHint("Para ser devoto, a raça ou a classe precisa estar entre os devotos do deus; humanos podem seguir qualquer um (p. 96).")
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { onPick(null) },
                    verticalAlignment = Alignment.CenterVertically,
                ) { Text("Nenhuma (não devoto)", style = MaterialTheme.typography.bodyLarge) }
                DeityCatalog.ALL.forEach { deity ->
                    val allowed = DeityCatalog.canBeDevote(deity, raceId, classId)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(
                        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(enabled = allowed) { onPick(deity) }.padding(vertical = 8.dp),
                    ) {
                        Text(
                            deity.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (allowed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        )
                        RuleHint("Devotos: ${deity.devotesText}")
                        if (!allowed) RuleHint("Sua raça e sua classe não estão entre os devotos.", warning = true)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}

/** Lista de poderes para marcar, com limite e resumo de cada um. */
@Composable
fun PowerChoiceList(
    title: String,
    powers: List<CatalogPower>,
    selected: Set<String>,
    limit: Int,
    onToggle: (String, Boolean) -> Unit,
) {
    if (powers.isEmpty()) return
    val count = powers.count { it.name in selected }
    Text("$title ($count de $limit)", style = MaterialTheme.typography.titleSmall)
    powers.forEach { power ->
        val isSelected = power.name in selected
        val enabled = isSelected || count < limit
        Row(
            Modifier.fillMaxWidth().clickable(enabled = enabled) { onToggle(power.name, !isSelected) }.padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle(power.name, it) }, enabled = enabled)
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f).padding(top = 10.dp)) {
                Text(
                    power.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                )
                Text("${power.summary} (p. ${power.page})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ============================== Poderes de classe ==============================

/** Seção da edição com os poderes de classe escolhidos e o botão para escolher mais. */
@Composable
fun ClassPowersEditor(sheet: ManualSheet, onChange: (ManualSheet) -> Unit) {
    val limit = CatalogApply.classPowerLimit(sheet)
    val chosen = sheet.classPowers
    var picking by remember { mutableStateOf(false) }

    Text(
        "${chosen.size} de $limit escolhidos",
        style = MaterialTheme.typography.titleMedium,
        color = if (chosen.size > limit) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
    )
    RuleHint("Um poder no 2º nível e um a cada nível seguinte.")
    if (chosen.size > limit) RuleHint("Há mais poderes do que o nível permite.", warning = true)

    chosen.distinct().forEach { name ->
        val power = com.jovis.t20.rules.ClassPowerCatalog.find(sheet.classId, name)
        val count = chosen.count { it == name }
        val unmet = power?.let { CatalogApply.unmetRequirements(sheet, it) }.orEmpty()
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (count > 1) "$name (×$count)" else name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                power?.let { Text("${it.summary} (p. ${it.page})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                if (unmet.isNotEmpty()) RuleHint("Pré-requisito não cumprido: ${unmet.joinToString(", ")}.", warning = true)
            }
            TextButton(onClick = { onChange(CatalogApply.removeClassPower(sheet, name)) }) {
                Text("Remover", color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
    Button(onClick = { picking = true }, enabled = chosen.size < limit) { Text("Escolher poder") }

    if (picking) {
        ClassPowerPickerDialog(
            sheet = sheet,
            onPick = { name ->
                onChange(CatalogApply.addClassPower(sheet, name))
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}

/** Todos os poderes da classe; os que ainda não podem ser escolhidos aparecem bloqueados com o motivo. */
@Composable
fun ClassPowerPickerDialog(sheet: ManualSheet, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val powers = com.jovis.t20.rules.ClassPowerCatalog.of(sheet.classId)
    var showBlocked by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Poderes de ${sheet.className}") },
        text = {
            Column {
                SwitchRow("Mostrar os que ainda não posso escolher", showBlocked) { showBlocked = it }
                Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                    powers.forEach { power ->
                        val alreadyChosen = !power.repeatable && power.name in sheet.classPowers
                        val unmet = CatalogApply.unmetRequirements(sheet, power)
                        val enabled = !alreadyChosen && unmet.isEmpty()
                        if (enabled || showBlocked) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Column(
                                Modifier.fillMaxWidth().clickable(enabled = enabled) { onPick(power.name) }.padding(vertical = 8.dp),
                            ) {
                                Text(
                                    power.name + if (power.repeatable) " (repetível)" else "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                )
                                Text(
                                    "${power.summary} (p. ${power.page})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (power.requirement.text.isNotBlank()) RuleHint("Pré-requisitos: ${power.requirement.text}")
                                when {
                                    alreadyChosen -> RuleHint("Já escolhido.")
                                    unmet.isNotEmpty() -> RuleHint("Falta: ${unmet.joinToString(", ")}.", warning = true)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}
