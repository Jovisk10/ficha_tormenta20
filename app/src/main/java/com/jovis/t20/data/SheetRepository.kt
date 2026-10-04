package com.jovis.t20.data

import com.jovis.t20.rules.ManualSheet
import com.jovis.t20.rules.SheetCodec
import java.io.File
import java.util.UUID

/** Uma ficha salva, com o identificador do arquivo. */
data class StoredSheet(val id: String, val sheet: ManualSheet)

/**
 * Guarda cada ficha como um arquivo JSON no armazenamento interno do app.
 * As telas só conversam com esta classe, então trocar por um banco de dados no futuro
 * não muda nada nelas.
 */
class SheetRepository(private val directory: File) {

    init {
        directory.mkdirs()
    }

    fun loadAll(): List<StoredSheet> =
        directory.listFiles { file -> file.extension == "json" }.orEmpty()
            .mapNotNull { file ->
                // Um arquivo corrompido não impede as outras fichas de abrirem
                runCatching { StoredSheet(file.nameWithoutExtension, SheetCodec.decode(file.readText())) }.getOrNull()
            }
            .sortedBy { it.sheet.name.lowercase() }

    /** Escreve num arquivo temporário e depois troca, para nunca deixar uma ficha pela metade. */
    fun save(stored: StoredSheet) {
        val target = File(directory, "${stored.id}.json")
        val temp = File(directory, "${stored.id}.json.tmp")
        temp.writeText(SheetCodec.encode(stored.sheet))
        if (!temp.renameTo(target)) {
            target.writeText(temp.readText())
            temp.delete()
        }
    }

    fun delete(id: String) {
        File(directory, "$id.json").delete()
    }

    fun newId(): String = UUID.randomUUID().toString()
}
