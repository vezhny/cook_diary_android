package com.vezhny.cookdiary.domain

import com.vezhny.cookdiary.data.DishWithLastCooked
import java.text.Collator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

object DishOrdering {
    private val collator: Collator = Collator.getInstance(Locale.forLanguageTag("ru")).apply {
        strength = Collator.SECONDARY
    }

    /** All distinct tags across [dishes], alphabetically. */
    fun allTags(dishes: List<DishWithLastCooked>): List<String> =
        dishes.flatMap { it.dish.tagList }.distinct().sortedWith(collator)

    /**
     * Keeps dishes having every tag in [selectedTags], then sorts them:
     * 1. by calendar days since last cooked, longest ago first (never cooked first);
     * 2. by tags (alphabetical, untagged last);
     * 3. by name.
     */
    fun arrange(
        dishes: List<DishWithLastCooked>,
        selectedTags: Set<String>,
        today: LocalDate,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<DishWithLastCooked> {
        fun daysAgo(item: DishWithLastCooked): Long = item.lastCookedAt
            ?.let { ChronoUnit.DAYS.between(Instant.ofEpochMilli(it).atZone(zone).toLocalDate(), today) }
            ?: Long.MAX_VALUE

        fun tagKey(item: DishWithLastCooked): String? =
            item.dish.tagList.sortedWith(collator).joinToString(",").ifEmpty { null }

        return dishes
            .filter { it.dish.tagList.containsAll(selectedTags) }
            .sortedWith(
                compareByDescending(::daysAgo)
                    .thenComparator { a, b -> compareTagKeys(tagKey(a), tagKey(b)) }
                    .thenComparator { a, b -> collator.compare(a.dish.name, b.dish.name) },
            )
    }

    private fun compareTagKeys(a: String?, b: String?): Int = when {
        a == b -> 0
        a == null -> 1
        b == null -> -1
        else -> collator.compare(a, b)
    }
}
