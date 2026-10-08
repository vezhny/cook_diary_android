package com.vezhny.cookdiary.domain

import com.vezhny.cookdiary.data.Dish
import com.vezhny.cookdiary.data.DishWithLastCooked
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class DishOrderingTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 10, 8)
    private var nextId = 1L

    private fun dish(name: String, tags: String = "", daysAgo: Long? = null, hour: Int = 12) = DishWithLastCooked(
        Dish(id = nextId++, name = name, tags = tags),
        daysAgo?.let { today.minusDays(it).atTime(hour, 0).toInstant(zone).toEpochMilli() },
    )

    private fun arrange(vararg dishes: DishWithLastCooked, tags: Set<String> = emptySet()) =
        DishOrdering.arrange(dishes.toList(), tags, today, zone).map { it.dish.name }

    @Test
    fun `longest ago first, never cooked before everything`() {
        assertEquals(
            listOf("никогда", "неделю", "вчера", "сегодня"),
            arrange(dish("сегодня", daysAgo = 0), dish("неделю", daysAgo = 7), dish("никогда"), dish("вчера", daysAgo = 1)),
        )
    }

    @Test
    fun `same day sorts by tag then name, time of day ignored`() {
        assertEquals(
            listOf("Омлет", "Блины", "Борщ", "Суп без тегов"),
            arrange(
                dish("Суп без тегов", daysAgo = 2, hour = 8),
                dish("Борщ", tags = "суп", daysAgo = 2, hour = 9),
                dish("Блины", tags = "завтрак", daysAgo = 2, hour = 20),
                dish("Омлет", tags = "быстро,завтрак", daysAgo = 2, hour = 10),
            ),
        )
    }

    @Test
    fun `same tags sort by name ignoring case`() {
        assertEquals(
            listOf("арбуз", "Борщ", "вареники"),
            arrange(dish("вареники", "x"), dish("Борщ", "x"), dish("арбуз", "x")),
        )
    }

    @Test
    fun `filter keeps dishes having all selected tags`() {
        val dishes = arrayOf(
            dish("Омлет", "быстро,завтрак"),
            dish("Каша", "завтрак"),
            dish("Паста", "быстро"),
            dish("Борщ"),
        )
        // Омлет first: its tags "быстро,завтрак" sort before "завтрак".
        assertEquals(listOf("Омлет", "Каша"), arrange(*dishes, tags = setOf("завтрак")))
        assertEquals(listOf("Омлет"), arrange(*dishes, tags = setOf("завтрак", "быстро")))
        assertEquals(4, arrange(*dishes).size)
    }

    @Test
    fun `all tags are distinct and sorted`() {
        val dishes = listOf(dish("a", "суп,быстро"), dish("b", "завтрак,быстро"), dish("c"))
        assertEquals(listOf("быстро", "завтрак", "суп"), DishOrdering.allTags(dishes))
    }
}
