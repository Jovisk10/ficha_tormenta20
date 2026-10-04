package com.jovis.t20.ui.sheet

import androidx.compose.foundation.BorderStroke
import com.jovis.t20.ui.theme.TornStyle
import com.jovis.t20.ui.theme.TornEdge
import com.jovis.t20.ui.theme.T20Fonts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jovis.t20.rules.Attribute
import com.jovis.t20.rules.DerivedValue
import com.jovis.t20.rules.ManualSheet
import com.jovis.t20.rules.SheetEngine
import com.jovis.t20.rules.Skill

/** Um valor que o jogador tocou para ver a explicação. */
private data class Explanation(val title: String, val value: DerivedValue)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetScreen(
    sheet: ManualSheet,
    onSheetChange: (ManualSheet) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var explanation by remember { mutableStateOf<Explanation?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    // Guardado como texto para sobreviver à rotação da tela
    var expandedNames by rememberSaveable { mutableStateOf("") }
    val expanded = expandedNames.split(",").mapNotNull { name -> SheetSection.entries.firstOrNull { it.name == name } }.toSet()
    fun toggle(section: SheetSection) {
        val updated = if (section in expanded) expanded - section else expanded + section
        expandedNames = updated.joinToString(",") { it.name }
    }
    val explain: (String, DerivedValue) -> Unit = { title, value -> explanation = Explanation(title, value) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("‹ Fichas") }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onEdit) { Text("Editar") }
                }
            }
            item { Header(sheet) }
            item {
                Text(
                    "Toque em um valor calculado para ver de onde ele vem.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { AttributeStrip(sheet) }
            item {
                Resources(
                    sheet = sheet,
                    onHpChange = { delta ->
                        val max = SheetEngine.maxHp(sheet).total
                        onSheetChange(sheet.copy(currentHp = (SheetEngine.currentHp(sheet) + delta).coerceAtMost(max)))
                    },
                    onMpChange = { delta ->
                        val max = SheetEngine.maxMp(sheet).total
                        onSheetChange(sheet.copy(currentMp = (SheetEngine.currentMp(sheet) + delta).coerceIn(0, max)))
                    },
                    explain = explain,
                )
            }
            // Seções recolhíveis: abrem e fecham com um toque no título
            item {
                run {
                    Column {
                        val spellDc = SheetEngine.spellDc(sheet)
                        val sections = buildList {
                            add(
                                SectionSpec(SheetSection.COMBATE, "Defesa ${SheetEngine.defense(sheet).total}") {
                                    Combat(sheet, explain)
                                },
                            )
                            add(
                                SectionSpec(SheetSection.PERICIAS, "${sheet.trainedSkills.size} treinadas") {
                                    SkillList(sheet, explain)
                                },
                            )
                            add(
                                SectionSpec(
                                    SheetSection.MAGIAS,
                                    when {
                                        sheet.spells.isEmpty() && spellDc == null -> "Nenhuma"
                                        spellDc == null -> countLabel(sheet.spells.size, "magia", "magias")
                                        else -> "${countLabel(sheet.spells.size, "magia", "magias")}, CD ${spellDc.total}"
                                    },
                                ) { Spells(sheet, explain) },
                            )
                            add(
                                SectionSpec(SheetSection.HABILIDADES, if (sheet.abilities.isEmpty()) "Nenhuma" else "${sheet.abilities.size}") {
                                    Abilities(sheet)
                                },
                            )
                            add(
                                SectionSpec(
                                    SheetSection.EQUIPAMENTO,
                                    "${formatSlots(SheetEngine.currentLoad(sheet))} de ${SheetEngine.loadLimit(sheet).total} espaços",
                                ) { Equipment(sheet) },
                            )
                            if (sheet.notes.isNotBlank()) {
                                add(SectionSpec(SheetSection.ANOTACOES, "") {
                                    Text(sheet.notes, style = MaterialTheme.typography.bodyMedium)
                                })
                            }
                        }
                        sections.forEachIndexed { index, spec ->
                            // Faixas alternadas, como as tabelas do livro
                            val stripe = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent
                            Box(Modifier.background(stripe)) {
                            CollapsibleSection(
                                title = spec.section.title,
                                summary = spec.summary,
                                expanded = spec.section in expanded,
                                onToggle = { toggle(spec.section) },
                                content = spec.content,
                            )
                            }
                        }
                    }
                }
            }
            item {
                TextButton(onClick = { confirmDelete = true }) {
                    Text("Excluir ficha", color = MaterialTheme.colorScheme.tertiary)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Excluir ${sheet.name}?") },
            text = { Text("A ficha será apagada deste celular. Isso não pode ser desfeito.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.tertiary)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }

    explanation?.let { current ->
        ModalBottomSheet(onDismissRequest = { explanation = null }) {
            ExplanationContent(current)
        }
    }
}

// ---------- Cabeçalho e atributos ----------

@Composable
private fun Header(sheet: ManualSheet) {
    Column {
        Text(
            sheet.name,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = T20Fonts.display,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        TornEdge(MaterialTheme.colorScheme.primary, Modifier.padding(top = 2.dp, bottom = 8.dp), height = 7.dp)
        val identity = listOfNotNull(
            sheet.race.takeIf { it.isNotBlank() },
            sheet.origin.takeIf { it.isNotBlank() }?.let { "origem $it" },
        ).joinToString(", ")
        Text(
            "$identity. ${sheet.className} de ${sheet.level}º nível.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
        )
        if (sheet.deity.isNotBlank()) {
            Text(
                "Devoção: ${sheet.deity}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** O elemento de destaque da ficha: os seis atributos com numerais grandes. */
@Composable
private fun AttributeStrip(sheet: ManualSheet) {
    val band = MaterialTheme.colorScheme.primaryContainer
    Column {
        TornEdge(band, style = TornStyle.TOP, height = 8.dp, seed = 7)
        Row(Modifier.fillMaxWidth().background(band).padding(vertical = 10.dp, horizontal = 6.dp)) {
            Attribute.entries.forEach { attribute ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        SheetEngine.attribute(sheet, attribute).toString(),
                        fontFamily = T20Fonts.serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        attribute.abbreviation,
                        fontFamily = T20Fonts.display,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }
        TornEdge(band, style = TornStyle.BOTTOM, height = 8.dp, seed = 11)
    }
}

// ---------- PV e PM ----------

@Composable
private fun Resources(
    sheet: ManualSheet,
    onHpChange: (Int) -> Unit,
    onMpChange: (Int) -> Unit,
    explain: (String, DerivedValue) -> Unit,
) {
    val maxHp = SheetEngine.maxHp(sheet)
    val maxMp = SheetEngine.maxMp(sheet)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ResourceCard(
            label = "Pontos de vida",
            current = SheetEngine.currentHp(sheet),
            max = maxHp.total,
            color = MaterialTheme.colorScheme.tertiary,
            buttonColor = MaterialTheme.colorScheme.tertiaryContainer,
            onButtonColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onChange = onHpChange,
            onMaxClick = { explain("PV máximos", maxHp) },
            modifier = Modifier.weight(1f),
        )
        ResourceCard(
            label = "Pontos de mana",
            current = SheetEngine.currentMp(sheet),
            max = maxMp.total,
            color = MaterialTheme.colorScheme.secondary,
            buttonColor = MaterialTheme.colorScheme.secondaryContainer,
            onButtonColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onChange = onMpChange,
            onMaxClick = { explain("PM máximos", maxMp) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ResourceCard(
    label: String,
    current: Int,
    max: Int,
    color: Color,
    buttonColor: Color,
    onButtonColor: Color,
    onChange: (Int) -> Unit,
    onMaxClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            // Toda a área do rótulo e do número abre a explicação do máximo
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onMaxClick)) {
                Text(label, fontFamily = T20Fonts.display, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = color)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$current", fontFamily = T20Fonts.serif, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = color)
                    Text(
                        " / $max",
                        fontFamily = T20Fonts.serif,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
            // Barra simples desenhada à mão (evita depender da versão do LinearProgressIndicator)
            val fraction = if (max > 0) (current.toFloat() / max).coerceIn(0f, 1f) else 0f
            Box(
                Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(color))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val buttonColors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = buttonColor,
                    contentColor = onButtonColor,
                )
                FilledTonalButton(onClick = { onChange(-1) }, colors = buttonColors, modifier = Modifier.weight(1f)) { Text("−1") }
                FilledTonalButton(onClick = { onChange(+1) }, colors = buttonColors, modifier = Modifier.weight(1f)) { Text("+1") }
            }
        }
    }
}

// ---------- Seções recolhíveis ----------

private enum class SheetSection(val title: String) {
    COMBATE("Combate"),
    PERICIAS("Perícias"),
    MAGIAS("Magias"),
    HABILIDADES("Habilidades e poderes"),
    EQUIPAMENTO("Equipamento"),
    ANOTACOES("Anotações"),
}

private class SectionSpec(
    val section: SheetSection,
    val summary: String,
    val content: @Composable () -> Unit,
)

@Composable
private fun CollapsibleSection(
    title: String,
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = T20Fonts.display,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            if (summary.isNotBlank()) {
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            // Seta que gira ao abrir
            val rotation by animateFloatAsState(if (expanded) 90f else 0f, label = "seta")
            Text(
                "›",
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(rotation),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun Abilities(sheet: ManualSheet) {
    if (sheet.abilities.isEmpty()) {
        Text(
            "Nenhuma habilidade ou poder cadastrado. Adicione em Editar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sheet.abilities.forEach { entry ->
            Column(Modifier.fillMaxWidth()) {
                Text(entry.name, style = MaterialTheme.typography.titleSmall)
                if (entry.description.isNotBlank()) {
                    Text(entry.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun countLabel(count: Int, singular: String, plural: String) = if (count == 1) "1 $singular" else "$count $plural"

// ---------- Combate ----------

@Composable
private fun Combat(sheet: ManualSheet, explain: (String, DerivedValue) -> Unit) {
    val defense = SheetEngine.defense(sheet)
    val speed = SheetEngine.speed(sheet)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox("Defesa", "${defense.total}", Modifier.weight(1f)) { explain("Defesa", defense) }
            StatBox("Deslocamento", "${speed.total}m", Modifier.weight(1f)) { explain("Deslocamento", speed) }
        }
        sheet.weapons.forEach { weapon ->
            val attack = SheetEngine.attackBonus(sheet, weapon)
            ListRow(onClick = { explain("Ataque: ${weapon.name}", attack) }) {
                Column(Modifier.weight(1f)) {
                    Text(weapon.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Dano ${SheetEngine.damageText(sheet, weapon)}, crítico ${SheetEngine.criticalText(weapon)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ValueText(attack.total)
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(4.dp)).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(4.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ---------- Perícias ----------

@Composable
private fun SkillList(sheet: ManualSheet, explain: (String, DerivedValue) -> Unit) {
    run {
        Column {
            Skill.entries.forEachIndexed { index, skill ->
                val value = SheetEngine.skill(sheet, skill)
                val trained = SheetEngine.isTrained(sheet, skill)
                val usable = SheetEngine.canUse(sheet, skill)
                val stripe = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f) else Color.Transparent
                Box(Modifier.background(stripe)) {
                ListRow(onClick = { explain(skill.displayName, value) }, horizontalPadding = 8.dp) {
                    // Marca de treinamento: círculo cheio = treinada
                    Box(
                        Modifier.size(10.dp).clip(CircleShape).background(
                            if (trained) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        ),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            skill.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (usable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        )
                        if (!usable) {
                            Text("Só pode ser usada se treinada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    Text(
                        skill.keyAttribute.abbreviation,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    ValueText(value.total, muted = !usable)
                }
                }
            }
        }
    }
}

// ---------- Magias ----------

@Composable
private fun Spells(sheet: ManualSheet, explain: (String, DerivedValue) -> Unit) {
    var spellDetails by remember { mutableStateOf<com.jovis.t20.rules.SheetSpell?>(null) }
    spellDetails?.let { spell ->
        AlertDialog(
            onDismissRequest = { spellDetails = null },
            title = { Text(spell.name) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    com.jovis.t20.ui.edit.SpellDetails(spell)
                }
            },
            confirmButton = { TextButton(onClick = { spellDetails = null }) { Text("Fechar") } },
        )
    }
    if (sheet.spells.isEmpty() && sheet.spellAttribute == null) {
        Text(
            "Nenhuma magia cadastrada. Adicione em Editar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SheetEngine.spellDc(sheet)?.let { dc ->
            StatBox("CD das magias", "${dc.total}", Modifier.fillMaxWidth()) { explain("CD das magias", dc) }
        }
        sheet.spells.forEach { spell ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { spellDetails = spell },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(spell.name, style = MaterialTheme.typography.bodyLarge)
                    val details = listOf("${spell.circle}º círculo", spell.school, spell.notes).filter { it.isNotBlank() }
                    Text(details.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "${SheetEngine.spellCost(spell)} PM",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

// ---------- Equipamento ----------

@Composable
private fun Equipment(sheet: ManualSheet) {
    val load = SheetEngine.currentLoad(sheet)
    val limit = SheetEngine.loadLimit(sheet).total
    val overloaded = SheetEngine.isOverloaded(sheet)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Carga: ${formatSlots(load)} de $limit espaços (máximo ${SheetEngine.maxLoad(sheet)})",
            style = MaterialTheme.typography.bodyLarge,
            color = if (overloaded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
        )
        if (overloaded) {
            Text(
                "Sobrecarregado: –5 nas perícias com penalidade de armadura e –3m de deslocamento.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        sheet.inventory.forEach { item ->
            Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (if (item.quantity > 1) "${item.quantity}× " else "") + item.name + (if (item.equipped) " (vestido)" else ""),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${formatSlots(item.slots * item.quantity)} esp.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text("Tibares: T$ ${sheet.money}", style = MaterialTheme.typography.bodyLarge)
    }
}

// ---------- Explicação do cálculo ----------

@Composable
private fun ExplanationContent(explanation: Explanation) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
        Text(
            "${explanation.title}: ${explanation.value.total}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        explanation.value.contributions.forEach { part ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(part.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(signed(part.value), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
        if (explanation.value.situational.isNotEmpty()) {
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Text("Não incluído no total", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            explanation.value.situational.forEach { mod ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(mod.source.name, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
                        mod.situation?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(signed(mod.value), style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}

// ---------- Peças reutilizáveis ----------

@Composable
private fun ListRow(
    onClick: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun ValueText(value: Int, muted: Boolean = false) {
    Text(
        signed(value),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.End,
        color = if (muted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
        modifier = Modifier.width(44.dp),
    )
}

private fun signed(value: Int): String = if (value >= 0) "+$value" else "$value"

private fun formatSlots(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')
