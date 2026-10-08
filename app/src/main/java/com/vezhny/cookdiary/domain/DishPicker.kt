package com.vezhny.cookdiary.domain

import com.vezhny.cookdiary.data.DishWithLastCooked
import kotlin.random.Random

/**
 * Weighted random pick: the longer ago a dish was cooked, the more likely it is suggested.
 *
 * Weight = days since last cooked + 1, capped at [maxDays] + 1. Never-cooked dishes get the cap,
 * so a dish cooked today still has weight 1 (rare, but possible).
 */
class DishPicker(
    private val random: Random = Random.Default,
    private val clock: () -> Long = System::currentTimeMillis,
    private val maxDays: Int = 30,
) {
    fun weight(lastCookedAt: Long?, now: Long = clock()): Int {
        if (lastCookedAt == null) return maxDays + 1
        val days = ((now - lastCookedAt) / DAY_MILLIS).coerceIn(0, maxDays.toLong()).toInt()
        return days + 1
    }

    /** Picks a dish, avoiding [excludeId] when there is anything else to choose from. */
    fun pick(dishes: List<DishWithLastCooked>, excludeId: Long? = null): DishWithLastCooked? {
        val candidates = dishes.filter { it.dish.id != excludeId }.ifEmpty { dishes }
        if (candidates.isEmpty()) return null

        val now = clock()
        val weights = candidates.map { weight(it.lastCookedAt, now) }
        var roll = random.nextInt(weights.sum())
        for ((index, w) in weights.withIndex()) {
            if (roll < w) return candidates[index]
            roll -= w
        }
        return candidates.last()
    }

    companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
