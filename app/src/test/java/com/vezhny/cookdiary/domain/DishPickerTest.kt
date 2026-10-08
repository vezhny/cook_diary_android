package com.vezhny.cookdiary.domain

import com.vezhny.cookdiary.data.Dish
import com.vezhny.cookdiary.data.DishWithLastCooked
import com.vezhny.cookdiary.domain.DishPicker.Companion.DAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DishPickerTest {
    private val now = 1_000 * DAY_MILLIS
    private fun picker(seed: Int = 42) = DishPicker(random = Random(seed), clock = { now })
    private fun dish(id: Long, daysAgo: Int?) =
        DishWithLastCooked(Dish(id = id, name = "dish$id"), daysAgo?.let { now - it * DAY_MILLIS })

    @Test
    fun `weight grows with days since last cooked and is capped`() {
        val p = picker()
        assertEquals(1, p.weight(now, now))
        assertEquals(1, p.weight(now - DAY_MILLIS / 2, now))
        assertEquals(8, p.weight(now - 7 * DAY_MILLIS, now))
        assertEquals(31, p.weight(now - 365 * DAY_MILLIS, now))
        assertEquals(31, p.weight(null, now))
    }

    @Test
    fun `future timestamp is treated as today`() {
        assertEquals(1, picker().weight(now + DAY_MILLIS, now))
    }

    @Test
    fun `empty list gives null`() {
        assertNull(picker().pick(emptyList()))
    }

    @Test
    fun `excluded dish is skipped when alternatives exist`() {
        val dishes = listOf(dish(1, null), dish(2, 0))
        repeat(100) { seed ->
            assertEquals(2L, picker(seed).pick(dishes, excludeId = 1)?.dish?.id)
        }
    }

    @Test
    fun `single dish is returned even if excluded`() {
        assertEquals(1L, picker().pick(listOf(dish(1, 3)), excludeId = 1)?.dish?.id)
    }

    @Test
    fun `recently cooked dish is picked much less often`() {
        val dishes = listOf(dish(1, 0), dish(2, null))
        val p = picker()
        val counts = (1..10_000).groupingBy { p.pick(dishes)!!.dish.id }.eachCount()
        val recent = counts[1L] ?: 0
        // Expected share 1/32 ≈ 312 of 10k.
        assertTrue("recent dish picked $recent times", recent in 150..500)
        assertNotEquals(0, recent)
    }
}
