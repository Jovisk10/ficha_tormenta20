package com.jovis.t20.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jovis.t20.data.SheetRepository
import com.jovis.t20.data.StoredSheet
import com.jovis.t20.rules.AttributeMethod
import com.jovis.t20.rules.ManualSheet
import com.jovis.t20.rules.SampleSheets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Guarda a lista de fichas e cuida de salvar cada mudança.
 * A tela é atualizada na hora; o arquivo é gravado em segundo plano.
 */
class SheetsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SheetRepository(File(application.filesDir, "sheets"))
    private val preferences = application.getSharedPreferences("t20", Context.MODE_PRIVATE)

    private val _sheets = MutableStateFlow<List<StoredSheet>>(emptyList())
    val sheets: StateFlow<List<StoredSheet>> = _sheets.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    init {
        viewModelScope.launch {
            val all = withContext(Dispatchers.IO) {
                // Na primeira vez que o app abre, já vem com a ficha de exemplo da SEDAF
                if (!preferences.getBoolean("sample_created", false)) {
                    repository.save(StoredSheet(repository.newId(), SampleSheets.SEDAF))
                    preferences.edit().putBoolean("sample_created", true).apply()
                }
                repository.loadAll()
            }
            _sheets.value = all
            _loaded.value = true
        }
    }

    /** Cria uma ficha em branco e devolve o identificador dela. */
    fun create(): String {
        val stored = StoredSheet(repository.newId(), ManualSheet(name = "Novo personagem", attributeMode = AttributeMethod.POINT_BUY))
        _sheets.value = sortByName(_sheets.value + stored)
        persist(stored)
        return stored.id
    }

    fun update(id: String, sheet: ManualSheet) {
        val stored = StoredSheet(id, sheet)
        _sheets.value = sortByName(_sheets.value.map { if (it.id == id) stored else it })
        persist(stored)
    }

    fun delete(id: String) {
        _sheets.value = _sheets.value.filterNot { it.id == id }
        viewModelScope.launch(Dispatchers.IO) { repository.delete(id) }
    }

    private fun persist(stored: StoredSheet) {
        viewModelScope.launch(Dispatchers.IO) { repository.save(stored) }
    }

    private fun sortByName(list: List<StoredSheet>) = list.sortedBy { it.sheet.name.lowercase() }
}
