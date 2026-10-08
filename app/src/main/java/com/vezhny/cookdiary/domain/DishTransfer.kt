package com.vezhny.cookdiary.domain

import com.vezhny.cookdiary.data.Dish
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * JSON format for exporting and importing the dish list:
 * `{"version": 1, "dishes": [{"name": "Борщ", "tags": ["суп"], "note": "..."}]}`
 */
object DishTransfer {
    const val VERSION = 1

    @Serializable
    private data class FileDto(val version: Int = VERSION, val dishes: List<DishDto>)

    @Serializable
    private data class DishDto(val name: String, val tags: List<String> = emptyList(), val note: String? = null)

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun encode(dishes: List<Dish>): String = json.encodeToString(
        FileDto.serializer(),
        FileDto(dishes = dishes.map { DishDto(it.name, it.tagList, it.note) }),
    )

    /**
     * Parses an exported file into new (unsaved) dishes. Dishes with a blank name are skipped.
     * @throws IllegalArgumentException if the text is not a valid dish file.
     */
    fun decode(text: String): List<Dish> {
        val file = try {
            json.decodeFromString(FileDto.serializer(), text)
        } catch (e: SerializationException) {
            throw IllegalArgumentException("Not a dish file", e)
        }
        require(file.version <= VERSION) { "Unsupported version ${file.version}" }
        return file.dishes
            .filter { it.name.isNotBlank() }
            .map { Dish(name = it.name.trim(), tags = normalizeTags(it.tags), note = it.note?.trim()?.ifEmpty { null }) }
    }
}
