package com.vezhny.cookdiary.domain

import com.vezhny.cookdiary.data.Dish
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeParseException

/** One cooking history entry in a file: the dish is referenced by name, so files are portable. */
data class HistoryEntry(val dishName: String, val cookedAt: Long)

/** Contents of an export file. [history] is null when only dishes were exported. */
data class TransferData(val dishes: List<Dish>, val history: List<HistoryEntry>?)

/**
 * JSON format for exporting and importing data:
 * ```
 * {"version": 2,
 *  "dishes": [{"name": "Борщ", "tags": ["суп"], "note": "..."}],
 *  "history": [{"dish": "Борщ", "cookedAt": "2026-10-08T12:30:00Z"}]}
 * ```
 * `history` is present only in full exports. Version 1 files (dishes only) are still readable.
 */
object DishTransfer {
    const val VERSION = 2

    @Serializable
    private data class FileDto(
        val version: Int = VERSION,
        val dishes: List<DishDto>,
        val history: List<HistoryDto>? = null,
    )

    @Serializable
    private data class DishDto(val name: String, val tags: List<String> = emptyList(), val note: String? = null)

    @Serializable
    private data class HistoryDto(val dish: String, val cookedAt: String)

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        explicitNulls = false // dishes-only files have no "history" key at all
    }

    /** Encodes [dishes] and, for a full export, [history]. */
    fun encode(dishes: List<Dish>, history: List<HistoryEntry>? = null): String = json.encodeToString(
        FileDto.serializer(),
        FileDto(
            dishes = dishes.map { DishDto(it.name, it.tagList, it.note) },
            history = history?.map { HistoryDto(it.dishName, Instant.ofEpochMilli(it.cookedAt).toString()) },
        ),
    )

    /**
     * Parses an export file into new (unsaved) dishes and history entries.
     * Dishes and history entries with a blank dish name are skipped.
     * @throws IllegalArgumentException if the text is not a valid file.
     */
    fun decode(text: String): TransferData {
        val file = try {
            json.decodeFromString(FileDto.serializer(), text)
        } catch (e: SerializationException) {
            throw IllegalArgumentException("Not a Cook Diary file", e)
        }
        require(file.version <= VERSION) { "Unsupported version ${file.version}" }
        val dishes = file.dishes
            .filter { it.name.isNotBlank() }
            .map { Dish(name = it.name.trim(), tags = normalizeTags(it.tags), note = it.note?.trim()?.ifEmpty { null }) }
        val history = file.history
            ?.filter { it.dish.isNotBlank() }
            ?.map { HistoryEntry(it.dish.trim(), parseInstant(it.cookedAt)) }
        return TransferData(dishes, history)
    }

    private fun parseInstant(text: String): Long = try {
        Instant.parse(text).toEpochMilli()
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Bad cookedAt '$text'", e)
    }
}
