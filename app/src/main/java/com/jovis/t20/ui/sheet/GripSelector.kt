package com.jovis.t20.ui.sheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Seletor "Uma mão / Duas mãos" para armas adaptáveis (espada bastarda, katana).
 * Só aparece quando a arma tem dano próprio para as duas mãos; a conta fica no `rules`.
 */
@Composable
fun GripSelector(twoHanded: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    // Cores do tema, para não aparecer o lilás padrão do Material
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = scheme.primary,
        activeContentColor = scheme.onPrimary,
        activeBorderColor = scheme.primary,
        inactiveContainerColor = scheme.surface,
        inactiveContentColor = scheme.onSurfaceVariant,
        inactiveBorderColor = scheme.outline,
    )
    val options = listOf(false to "Uma mão", true to "Duas mãos")
    SingleChoiceSegmentedButtonRow(modifier.widthIn(max = 320.dp).fillMaxWidth().height(36.dp)) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = twoHanded == value,
                onClick = { onChange(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                colors = colors,
                icon = {},
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }
    }
}
