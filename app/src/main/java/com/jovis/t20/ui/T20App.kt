package com.jovis.t20.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.jovis.t20.ui.edit.EditSheetScreen
import com.jovis.t20.ui.list.SheetListScreen
import com.jovis.t20.ui.sheet.SheetScreen

/** Navegação simples entre lista, ficha e edição. */
@Composable
fun T20App(viewModel: SheetsViewModel) {
    val sheets by viewModel.sheets.collectAsState()
    val loaded by viewModel.loaded.collectAsState()

    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by rememberSaveable { mutableStateOf(false) }
    // Ficha recém-criada: se o jogador cancelar a primeira edição, ela é descartada
    var newSheetId by rememberSaveable { mutableStateOf<String?>(null) }

    val open = sheets.firstOrNull { it.id == openId }

    fun closeEditor(saved: Boolean) {
        val id = openId
        if (!saved && id != null && id == newSheetId) {
            viewModel.delete(id)
            openId = null
        }
        newSheetId = null
        editing = false
    }

    when {
        open == null -> SheetListScreen(
            sheets = sheets,
            loaded = loaded,
            onOpen = { openId = it },
            onCreate = {
                val id = viewModel.create()
                newSheetId = id
                openId = id
                editing = true
            },
        )

        editing -> {
            BackHandler { closeEditor(saved = false) }
            EditSheetScreen(
                initial = open.sheet,
                onSave = { edited ->
                    viewModel.update(open.id, edited)
                    closeEditor(saved = true)
                },
                onCancel = { closeEditor(saved = false) },
            )
        }

        else -> {
            BackHandler { openId = null }
            SheetScreen(
                sheet = open.sheet,
                onSheetChange = { viewModel.update(open.id, it) },
                onBack = { openId = null },
                onEdit = { editing = true },
                onDelete = {
                    viewModel.delete(open.id)
                    openId = null
                },
            )
        }
    }
}
