package com.vezhny.cookdiary.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * How "hot" a dish is: 1.0 if cooked today, fading linearly by calendar days
 * to 0.0 after [COOL_DOWN_DAYS]. Never cooked is fully cold.
 */
object DishHeat {
    const val COOL_DOWN_DAYS = 14

    fun of(lastCooked: LocalDate?, today: LocalDate): Float {
        if (lastCooked == null) return 0f
        val days = ChronoUnit.DAYS.between(lastCooked, today).coerceAtLeast(0)
        return (1f - days.toFloat() / COOL_DOWN_DAYS).coerceAtLeast(0f)
    }
}
