package com.jovis.t20.ui.edit

import com.jovis.t20.ui.theme.T20Fonts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jovis.t20.rules.Attribute
import com.jovis.t20.rules.SheetBonus

@Composable
fun EditSection(title: String, content: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = T20Fonts.display,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Composable
fun TextInput(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Campo só de números inteiros (ex.: tibares). */
@Composable
fun IntInput(label: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { text -> onChange(text.filter { it.isDigit() }.take(9).toIntOrNull() ?: 0) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Número pequeno com botões de menos e mais. */
@Composable
fun NumberStepper(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    step: Int = 1,
    showSign: Boolean = false,
    suffix: String = "",
) {
    Row(modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { onChange((value - step).coerceIn(range)) }, enabled = value > range.first) { Text("−") }
        Text(
            (if (showSign && value > 0) "+$value" else "$value") + suffix,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp),
        )
        OutlinedButton(onClick = { onChange((value + step).coerceIn(range)) }, enabled = value < range.last) { Text("+") }
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** Escolha de uma opção entre poucas, como botões. */
@Composable
fun <T> OptionPicker(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    perRow: Int = 4,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        options.chunked(perRow).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { option ->
                    val modifier = Modifier.weight(1f)
                    if (option == selected) {
                        Button(onClick = { onSelect(option) }, modifier = modifier) { Text(optionLabel(option), maxLines = 1) }
                    } else {
                        OutlinedButton(onClick = { onSelect(option) }, modifier = modifier) { Text(optionLabel(option), maxLines = 1) }
                    }
                }
                // Mantém os botões da última linha do mesmo tamanho dos outros
                repeat(perRow - row.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun AttributePicker(label: String, selected: Attribute?, onSelect: (Attribute?) -> Unit) {
    OptionPicker(
        label = label,
        options = listOf<Attribute?>(null) + Attribute.entries,
        selected = selected,
        optionLabel = { it?.abbreviation ?: "Nenhum" },
        onSelect = onSelect,
    )
}

/** Lista de bônus com nome: o jogador adiciona, ajusta e remove. */
@Composable
fun BonusListEditor(title: String, bonuses: List<SheetBonus>, onChange: (List<SheetBonus>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        bonuses.forEachIndexed { index, bonus ->
            fun replace(new: SheetBonus) = onChange(bonuses.toMutableList().also { it[index] = new })
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(8.dp)) {
                    TextInput("Nome do bônus", bonus.label, { replace(bonus.copy(label = it)) })
                    NumberStepper("Valor", bonus.value, { replace(bonus.copy(value = it)) }, -30..30, showSign = true)
                    SwitchRow("Multiplicar pelo nível", bonus.perLevel) { replace(bonus.copy(perLevel = it)) }
                    TextInput(
                        "Só vale quando (deixe vazio se vale sempre)",
                        bonus.situation.orEmpty(),
                        { replace(bonus.copy(situation = it.ifBlank { null })) },
                    )
                    TextButton(onClick = { onChange(bonuses.filterIndexed { i, _ -> i != index }) }) { Text("Remover bônus") }
                }
            }
        }
        OutlinedButton(onClick = { onChange(bonuses + SheetBonus("", 1)) }) { Text("Adicionar bônus") }
    }
}

/** Linha de uma lista editável (ataque, magia, item...). */
@Composable
fun EditableRow(title: String, subtitle: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onEdit), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title.ifBlank { "Sem nome" }, style = MaterialTheme.typography.bodyLarge)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        TextButton(onClick = onEdit) { Text("Editar") }
        TextButton(onClick = onDelete) { Text("Remover", color = MaterialTheme.colorScheme.tertiary) }
    }
}
