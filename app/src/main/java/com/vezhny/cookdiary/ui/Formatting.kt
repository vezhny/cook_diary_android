package com.vezhny.cookdiary.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

fun formatDate(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Сегодня"
    today.minusDays(1) -> "Вчера"
    else -> date.format(dateFormatter)
}

fun formatLastCooked(lastCookedAt: Long?, today: LocalDate = LocalDate.now()): String {
    if (lastCookedAt == null) return "Ещё не готовили"
    val days = ChronoUnit.DAYS.between(lastCookedAt.toLocalDate(), today)
    return when (days) {
        0L -> "Готовили сегодня"
        1L -> "Готовили вчера"
        else -> "Готовили $days дн. назад"
    }
}
