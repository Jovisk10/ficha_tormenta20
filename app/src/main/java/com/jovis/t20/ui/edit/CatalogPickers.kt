package com.jovis.t20.ui.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.jovis.t20.rules.ArmorCatalog
import com.jovis.t20.rules.ArmorDefinition
import com.jovis.t20.rules.CatalogItem
import com.jovis.t20.rules.CatalogSpell
import com.jovis.t20.rules.CatalogWeapon
import com.jovis.t20.rules.ItemCatalog
import com.jovis.t20.rules.SheetSpell
import com.jovis.t20.rules.ShieldDefinition
import com.jovis.t20.rules.WeaponCatalog
import com.jovis.t20.rules.WeaponProficiency

/** Remove acentos e caixa para a busca achar "bencao" em "Bênção". */
private fun normalize(text: String): String =
    java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase()

/** Lista com busca, dentro de um diálogo. */
@Composable
fun <T> CatalogPickerDialog(
    title: String,
    items: List<T>,
    itemTitle: (T) -> String,
    itemSubtitle: (T) -> String,
    onPick: (T) -> Unit,
    onDismiss: () -> Unit,
    emptyMessage: String = "Nada encontrado.",
    header: @Composable () -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val filtered = items.filter { normalize(itemTitle(it)).contains(normalize(query)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                header()
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    if (filtered.isEmpty()) {
                        Text(emptyMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    filtered.forEachIndexed { index, item ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(
                            Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable { onPick(item) }.padding(vertical = 8.dp),
                        ) {
                            Text(itemTitle(item), style = MaterialTheme.typography.bodyLarge)
                            val subtitle = itemSubtitle(item)
                            if (subtitle.isNotBlank()) {
                                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}

// ---------- Magias ----------

/** Escolha de magia em dois passos: lista filtrada e, ao tocar, os detalhes com o botão de adicionar. */
@Composable
fun SpellPickerDialog(
    available: List<CatalogSpell>,
    all: List<CatalogSpell>,
    explanation: String,
    onPick: (CatalogSpell) -> Unit,
    onDismiss: () -> Unit,
) {
    var showAll by remember { mutableStateOf(available.isEmpty()) }
    var selected by remember { mutableStateOf<CatalogSpell?>(null) }

    val current = selected
    if (current != null) {
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(current.name) },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) { SpellDetails(current.toSheetSpell()) } },
            confirmButton = { TextButton(onClick = { onPick(current) }) { Text("Adicionar magia") } },
            dismissButton = { TextButton(onClick = { selected = null }) { Text("Voltar") } },
        )
        return
    }

    CatalogPickerDialog<CatalogSpell>(
        title = "Magias do livro",
        items = if (showAll) all else available,
        itemTitle = { it.name },
        itemSubtitle = { "${it.circle}º círculo, ${it.type.displayName.lowercase()}, ${it.school.displayName}" },
        onPick = { selected = it },
        onDismiss = onDismiss,
        emptyMessage = "Nenhuma magia com esses filtros.",
        header = {
            Text(
                if (showAll) "Mostrando todas as magias de 1º círculo." else explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SwitchRow("Mostrar todas (para poderes e raça)", showAll) { showAll = it }
        },
    )
}

/** Os dados de uma magia, usados no catálogo e na ficha. */
@Composable
fun SpellDetails(spell: SheetSpell) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val header = listOfNotNull(
            spell.type.takeIf { it.isNotBlank() },
            "${spell.circle}º círculo",
            spell.school.takeIf { it.isNotBlank() },
        ).joinToString(", ")
        Text(header, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        DetailLine("Custo", "${com.jovis.t20.rules.SpellCost.of(spell.circle)} PM")
        DetailLine("Execução", spell.execution)
        DetailLine("Alcance", spell.range)
        if (spell.target.isNotBlank()) {
            val (label, value) = spell.target.split(": ", limit = 2).let { if (it.size == 2) it[0] to it[1] else "Alvo" to it[0] }
            DetailLine(label, value)
        }
        DetailLine("Duração", spell.duration)
        DetailLine("Resistência", spell.resistance)
        if (spell.summary.isNotBlank()) {
            Text(spell.summary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        }
        if (spell.notes.isNotBlank()) {
            Text(spell.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        spell.page?.let {
            Text(
                "Regra completa: Tormenta 20 Jogo do Ano, p. $it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(verticalAlignment = Alignment.Top) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

// ---------- Armas, armaduras e itens ----------

private fun proficiencyLabel(p: WeaponProficiency) = when (p) {
    WeaponProficiency.SIMPLE -> "simples"
    WeaponProficiency.MARTIAL -> "marcial"
    WeaponProficiency.EXOTIC -> "exótica"
    WeaponProficiency.FIREARM -> "de fogo"
    WeaponProficiency.NATURAL -> "natural"
}

private fun slotsText(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')

@Composable
fun WeaponPickerDialog(onPick: (CatalogWeapon) -> Unit, onDismiss: () -> Unit) {
    CatalogPickerDialog<CatalogWeapon>(
        title = "Armas do livro",
        items = WeaponCatalog.ALL,
        itemTitle = { it.name },
        itemSubtitle = {
            listOfNotNull(
                "${it.damage}, crítico ${it.criticalText}",
                proficiencyLabel(it.proficiency),
                it.grip.displayName.lowercase(),
                it.range?.let { r -> "alcance ${r.lowercase()}" },
                "${slotsText(it.slots)} esp.",
                it.price,
            ).joinToString(", ")
        },
        onPick = onPick,
        onDismiss = onDismiss,
    )
}

@Composable
fun ArmorPickerDialog(onPick: (ArmorDefinition) -> Unit, onDismiss: () -> Unit) {
    CatalogPickerDialog<ArmorDefinition>(
        title = "Armaduras do livro",
        items = ArmorCatalog.ARMORS,
        itemTitle = { it.name },
        itemSubtitle = {
            val kind = if (it.category == com.jovis.t20.rules.ArmorCategory.HEAVY) "pesada" else "leve"
            "Defesa +${it.defenseBonus}, penalidade ${it.armorPenalty}, $kind, ${slotsText(it.slots)} esp., ${ArmorCatalog.PRICES[it.name].orEmpty()}"
        },
        onPick = onPick,
        onDismiss = onDismiss,
    )
}

@Composable
fun ShieldPickerDialog(onPick: (ShieldDefinition) -> Unit, onDismiss: () -> Unit) {
    CatalogPickerDialog<ShieldDefinition>(
        title = "Escudos do livro",
        items = ArmorCatalog.SHIELDS,
        itemTitle = { it.name },
        itemSubtitle = { "Defesa +${it.defenseBonus}, penalidade ${it.armorPenalty}, ${slotsText(it.slots)} esp., ${ArmorCatalog.PRICES[it.name].orEmpty()}" },
        onPick = onPick,
        onDismiss = onDismiss,
    )
}

@Composable
fun ItemPickerDialog(onPick: (CatalogItem) -> Unit, onDismiss: () -> Unit) {
    CatalogPickerDialog<CatalogItem>(
        title = "Itens do livro",
        items = ItemCatalog.ALL,
        itemTitle = { it.name },
        itemSubtitle = { "${it.price}, ${if (it.slots == 0.0) "não ocupa espaço" else "${slotsText(it.slots)} esp."}" },
        onPick = onPick,
        onDismiss = onDismiss,
    )
}
