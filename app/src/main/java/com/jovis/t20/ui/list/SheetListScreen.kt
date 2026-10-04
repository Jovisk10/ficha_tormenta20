package com.jovis.t20.ui.list

import androidx.compose.foundation.BorderStroke
import com.jovis.t20.ui.theme.TornEdge
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jovis.t20.data.StoredSheet
import com.jovis.t20.rules.SheetEngine

@Composable
fun SheetListScreen(
    sheets: List<StoredSheet>,
    loaded: Boolean,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Text("Nova ficha") }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Fichas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = T20Fonts.display,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                TornEdge(MaterialTheme.colorScheme.primary, Modifier.padding(top = 2.dp), height = 7.dp)
            }
            if (loaded && sheets.isEmpty()) {
                item {
                    Text(
                        "Nenhuma ficha ainda. Toque em Nova ficha para criar a primeira.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(sheets, key = { it.id }) { stored ->
                SheetCard(stored) { onOpen(stored.id) }
            }
        }
    }
}

@Composable
private fun SheetCard(stored: StoredSheet, onClick: () -> Unit) {
    val sheet = stored.sheet
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    sheet.name.ifBlank { "Sem nome" },
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = T20Fonts.display,
                    fontWeight = FontWeight.SemiBold,
                )
                val description = listOf(sheet.race, sheet.className).filter { it.isNotBlank() }.joinToString(", ")
                Text(
                    if (description.isBlank()) "Nível ${sheet.level}" else "$description, nível ${sheet.level}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (sheet.player.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text("Jogador: ${sheet.player}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${SheetEngine.currentHp(sheet)}/${SheetEngine.maxHp(sheet).total} PV",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    "${SheetEngine.currentMp(sheet)}/${SheetEngine.maxMp(sheet).total} PM",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}
