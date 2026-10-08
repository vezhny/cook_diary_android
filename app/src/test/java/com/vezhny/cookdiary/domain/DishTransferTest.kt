package com.vezhny.cookdiary.domain

import com.vezhny.cookdiary.data.Dish
import org.junit.Assert.assertEquals
import org.junit.Test

class DishTransferTest {
    @Test
    fun `round trip keeps name, tags and note but not ids`() {
        val dishes = listOf(
            Dish(id = 5, name = "Борщ", tags = "суп,обед", note = "Со сметаной"),
            Dish(id = 7, name = "Омлет"),
        )

        val decoded = DishTransfer.decode(DishTransfer.encode(dishes))

        assertEquals(
            listOf(Dish(name = "Борщ", tags = "суп,обед", note = "Со сметаной"), Dish(name = "Омлет")),
            decoded,
        )
    }

    @Test
    fun `tags are written as a list`() {
        val text = DishTransfer.encode(listOf(Dish(name = "Борщ", tags = "суп,обед")))
        assertEquals(true, Regex(""""tags":\s*\[\s*"суп",\s*"обед"\s*]""").containsMatchIn(text))
    }

    @Test
    fun `imported values are cleaned up`() {
        val text = """
            {"version": 1, "extra": true, "dishes": [
              {"name": "  Борщ ", "tags": ["Суп", " суп", ""], "note": "  "},
              {"name": "   "},
              {"name": "Каша"}
            ]}
        """
        assertEquals(
            listOf(Dish(name = "Борщ", tags = "суп"), Dish(name = "Каша")),
            DishTransfer.decode(text),
        )
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
