package com.vezhny.cookdiary.domain

import com.vezhny.cookdiary.domain.DishHeat.COOL_DOWN_DAYS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DishHeatTest {
    private val today = LocalDate.of(2026, 10, 8)

    @Test
    fun `cooked today is fully hot`() {
        assertEquals(1f, DishHeat.of(today, today), 0f)
    }

    @Test
    fun `never cooked is cold`() {
        assertEquals(0f, DishHeat.of(null, today), 0f)
    }

    @Test
    fun `heat fades linearly by days`() {
        assertEquals(0.5f, DishHeat.of(today.minusDays(COOL_DOWN_DAYS / 2L), today), 0.001f)
        val yesterday = DishHeat.of(today.minusDays(1), today)
        val weekAgo = DishHeat.of(today.minusDays(7), today)
        assertTrue(yesterday in weekAgo..1f)
    }

    @Test
    fun `fully cold after cool down period and beyond`() {
        assertEquals(0f, DishHeat.of(today.minusDays(COOL_DOWN_DAYS.toLong()), today), 0f)
        assertEquals(0f, DishHeat.of(today.minusDays(365), today), 0f)
    }

    @Test
    fun `future date counts as today`() {
        assertEquals(1f, DishHeat.of(today.plusDays(1), today), 0f)
    }
}
