package com.jovis.t20.rules

/**
 * JSON mínimo em Kotlin puro, para salvar fichas sem depender de bibliotecas.
 * Suporta tudo que a ficha usa: objetos, listas, textos, números, booleanos e null.
 */
sealed interface Json {
    data class Obj(val fields: Map<String, Json>) : Json
    data class Arr(val items: List<Json>) : Json
    data class Str(val value: String) : Json
    data class Num(val value: Double) : Json
    data class Bool(val value: Boolean) : Json
    data object Null : Json

    companion object {
        fun obj(vararg pairs: Pair<String, Json>) = Obj(linkedMapOf(*pairs))
        fun str(value: String?) = if (value == null) Null else Str(value)
        fun num(value: Int) = Num(value.toDouble())
        fun num(value: Double) = Num(value)
        fun arr(items: List<Json>) = Arr(items)
    }
}

// ---------- Leitura tolerante (campos ausentes ou de tipo errado viram o padrão) ----------

fun Json.Obj.string(key: String, default: String = ""): String = (fields[key] as? Json.Str)?.value ?: default
fun Json.Obj.stringOrNull(key: String): String? = (fields[key] as? Json.Str)?.value
fun Json.Obj.int(key: String, default: Int = 0): Int = (fields[key] as? Json.Num)?.value?.toInt() ?: default
fun Json.Obj.double(key: String, default: Double = 0.0): Double = (fields[key] as? Json.Num)?.value ?: default
fun Json.Obj.bool(key: String, default: Boolean = false): Boolean = (fields[key] as? Json.Bool)?.value ?: default
fun Json.Obj.obj(key: String): Json.Obj? = fields[key] as? Json.Obj
fun Json.Obj.list(key: String): List<Json> = (fields[key] as? Json.Arr)?.items ?: emptyList()
fun Json.Obj.objects(key: String): List<Json.Obj> = list(key).filterIsInstance<Json.Obj>()

object JsonWriter {
    fun write(json: Json, indent: Boolean = true): String = buildString { write(json, this, if (indent) 0 else -1) }

    private fun write(json: Json, out: StringBuilder, level: Int) {
        val pretty = level >= 0
        fun newline(l: Int) { if (pretty) { out.append('\n'); repeat(l) { out.append("  ") } } }
        when (json) {
            is Json.Null -> out.append("null")
            is Json.Bool -> out.append(json.value)
            is Json.Num -> {
                val v = json.value
                if (v % 1.0 == 0.0 && kotlin.math.abs(v) < 1e15) out.append(v.toLong()) else out.append(v)
            }
            is Json.Str -> writeString(json.value, out)
            is Json.Arr -> {
                if (json.items.isEmpty()) { out.append("[]"); return }
                out.append('[')
                json.items.forEachIndexed { i, item ->
                    if (i > 0) out.append(',')
                    newline(level + 1)
                    write(item, out, if (pretty) level + 1 else -1)
                }
                newline(level)
                out.append(']')
            }
            is Json.Obj -> {
                if (json.fields.isEmpty()) { out.append("{}"); return }
                out.append('{')
                json.fields.entries.forEachIndexed { i, (key, value) ->
                    if (i > 0) out.append(',')
                    newline(level + 1)
                    writeString(key, out)
                    out.append(if (pretty) ": " else ":")
                    write(value, out, if (pretty) level + 1 else -1)
                }
                newline(level)
                out.append('}')
            }
        }
    }

    private fun writeString(s: String, out: StringBuilder) {
        out.append('"')
        for (c in s) {
            when (c) {
                '"' -> out.append("\\\"")
                '\\' -> out.append("\\\\")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                '\b' -> out.append("\\b")
                '\u000C' -> out.append("\\f")
                else -> if (c < ' ') out.append("\\u%04x".format(c.code)) else out.append(c)
            }
        }
        out.append('"')
    }
}

class JsonParseException(message: String) : Exception(message)

object JsonParser {
    fun parse(text: String): Json {
        val reader = Reader(text)
        val value = reader.value()
        reader.skipWhitespace()
        if (!reader.atEnd()) throw JsonParseException("Conteúdo extra na posição ${reader.pos}.")
        return value
    }

    private class Reader(val text: String) {
        var pos = 0

        fun atEnd() = pos >= text.length

        fun skipWhitespace() {
            while (!atEnd() && text[pos].isWhitespace()) pos++
        }

        fun expect(c: Char) {
            skipWhitespace()
            if (atEnd() || text[pos] != c) throw JsonParseException("Esperado '$c' na posição $pos.")
            pos++
        }

        fun value(): Json {
            skipWhitespace()
            if (atEnd()) throw JsonParseException("Fim inesperado do texto.")
            return when (val c = text[pos]) {
                '{' -> obj()
                '[' -> arr()
                '"' -> Json.Str(string())
                't' -> literal("true", Json.Bool(true))
                'f' -> literal("false", Json.Bool(false))
                'n' -> literal("null", Json.Null)
                else -> if (c == '-' || c.isDigit()) number() else throw JsonParseException("Caractere inesperado '$c' na posição $pos.")
            }
        }

        fun literal(word: String, result: Json): Json {
            if (!text.startsWith(word, pos)) throw JsonParseException("Esperado '$word' na posição $pos.")
            pos += word.length
            return result
        }

        fun obj(): Json.Obj {
            expect('{')
            val fields = linkedMapOf<String, Json>()
            skipWhitespace()
            if (!atEnd() && text[pos] == '}') { pos++; return Json.Obj(fields) }
            while (true) {
                skipWhitespace()
                val key = string()
                expect(':')
                fields[key] = value()
                skipWhitespace()
                if (atEnd()) throw JsonParseException("Objeto não fechado.")
                when (text[pos]) {
                    ',' -> pos++
                    '}' -> { pos++; return Json.Obj(fields) }
                    else -> throw JsonParseException("Esperado ',' ou '}' na posição $pos.")
                }
            }
        }

        fun arr(): Json.Arr {
            expect('[')
            val items = mutableListOf<Json>()
            skipWhitespace()
            if (!atEnd() && text[pos] == ']') { pos++; return Json.Arr(items) }
            while (true) {
                items += value()
                skipWhitespace()
                if (atEnd()) throw JsonParseException("Lista não fechada.")
                when (text[pos]) {
                    ',' -> pos++
                    ']' -> { pos++; return Json.Arr(items) }
                    else -> throw JsonParseException("Esperado ',' ou ']' na posição $pos.")
                }
            }
        }

        fun string(): String {
            if (atEnd() || text[pos] != '"') throw JsonParseException("Esperado texto na posição $pos.")
            pos++
            val out = StringBuilder()
            while (true) {
                if (atEnd()) throw JsonParseException("Texto não fechado.")
                val c = text[pos++]
                when (c) {
                    '"' -> return out.toString()
                    '\\' -> {
                        if (atEnd()) throw JsonParseException("Escape incompleto.")
                        when (val e = text[pos++]) {
                            '"' -> out.append('"')
                            '\\' -> out.append('\\')
                            '/' -> out.append('/')
                            'b' -> out.append('\b')
                            'f' -> out.append('\u000C')
                            'n' -> out.append('\n')
                            'r' -> out.append('\r')
                            't' -> out.append('\t')
                            'u' -> {
                                if (pos + 4 > text.length) throw JsonParseException("Escape \\u incompleto.")
                                out.append(text.substring(pos, pos + 4).toInt(16).toChar())
                                pos += 4
                            }
                            else -> throw JsonParseException("Escape inválido '\\$e'.")
                        }
                    }
                    else -> out.append(c)
                }
            }
        }

        fun number(): Json.Num {
            val start = pos
            if (text[pos] == '-') pos++
            while (!atEnd() && (text[pos].isDigit() || text[pos] in ".eE+-")) pos++
            val raw = text.substring(start, pos)
            return Json.Num(raw.toDoubleOrNull() ?: throw JsonParseException("Número inválido '$raw'."))
        }
    }
}
