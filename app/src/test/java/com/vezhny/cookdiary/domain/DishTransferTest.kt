package com.vezhny.cookdiary.domain

import com.vezhny.cookdiary.data.Dish
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DishTransferTest {
    @Test
    fun `dishes-only round trip keeps name, tags and note but not ids`() {
        val dishes = listOf(
            Dish(id = 5, name = "Борщ", tags = "суп,обед", note = "Со сметаной"),
            Dish(id = 7, name = "Омлет"),
        )

        val decoded = DishTransfer.decode(DishTransfer.encode(dishes))

        assertEquals(
            listOf(Dish(name = "Борщ", tags = "суп,обед", note = "Со сметаной"), Dish(name = "Омлет")),
            decoded.dishes,
        )
        assertNull(decoded.history)
    }

    @Test
    fun `dishes-only file has no history key`() {
        assertFalse(DishTransfer.encode(listOf(Dish(name = "Борщ"))).contains("history"))
    }

    @Test
    fun `full round trip keeps history with dish names and exact time`() {
        val history = listOf(
            HistoryEntry("Борщ", 1_759_926_600_000),
            HistoryEntry("Омлет", 1_760_000_000_123),
        )

        val decoded = DishTransfer.decode(DishTransfer.encode(listOf(Dish(name = "Борщ"), Dish(name = "Омлет")), history))

        assertEquals(history, decoded.history)
    }

    @Test
    fun `history time is written as ISO-8601 UTC`() {
        val text = DishTransfer.encode(listOf(Dish(name = "Борщ")), listOf(HistoryEntry("Борщ", 1_759_926_600_000)))
        assertTrue(text, text.contains(""""cookedAt": "2025-10-08T12:30:00Z""""))
    }

    @Test
    fun `tags are written as a list`() {
        val text = DishTransfer.encode(listOf(Dish(name = "Борщ", tags = "суп,обед")))
        assertTrue(Regex(""""tags":\s*\[\s*"суп",\s*"обед"\s*]""").containsMatchIn(text))
    }

    @Test
    fun `version 1 file without history is still readable`() {
        val decoded = DishTransfer.decode("""{"version": 1, "dishes": [{"name": "Каша", "tags": ["завтрак"]}]}""")
        assertEquals(listOf(Dish(name = "Каша", tags = "завтрак")), decoded.dishes)
        assertNull(decoded.history)
    }

    @Test
    fun `imported values are cleaned up`() {
        val text = """
            {"version": 2, "extra": true, "dishes": [
              {"name": "  Борщ ", "tags": ["Суп", " суп", ""], "note": "  "},
              {"name": "   "},
              {"name": "Каша"}
            ], "history": [
              {"dish": " Борщ ", "cookedAt": "2025-10-08T12:30:00Z"},
              {"dish": "  ", "cookedAt": "2026-10-08T12:30:00Z"}
            ]}
        """
        val decoded = DishTransfer.decode(text)
        assertEquals(listOf(Dish(name = "Борщ", tags = "суп"), Dish(name = "Каша")), decoded.dishes)
        assertEquals(listOf(HistoryEntry("Борщ", 1_759_926_600_000)), decoded.history)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `bad history date is rejected`() {
        DishTransfer.decode("""{"version": 2, "dishes": [], "history": [{"dish": "Борщ", "cookedAt": "yesterday"}]}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `garbage is rejected`() {
        DishTransfer.decode("not json at all")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `json of another shape is rejected`() {
        DishTransfer.decode("""{"items": []}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `newer version is rejected`() {
        DishTransfer.decode("""{"version": 99, "dishes": []}""")
    }
}
