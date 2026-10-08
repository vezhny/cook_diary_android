package com.vezhny.cookdiary.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.vezhny.cookdiary.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

/** "Today", "Yesterday" or a long date in the UI language. */
@Composable
@ReadOnlyComposable
fun formatDate(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> stringResource(R.string.date_today)
    today.minusDays(1) -> stringResource(R.string.date_yesterday)
    else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(LocalLocale.current.platformLocale))
}

@Composable
@ReadOnlyComposable
fun formatLastCooked(lastCookedAt: Long?, today: LocalDate = LocalDate.now()): String {
    if (lastCookedAt == null) return stringResource(R.string.last_cooked_never)
    val days = ChronoUnit.DAYS.between(lastCookedAt.toLocalDate(), today).toInt()
    return when (days) {
        0 -> stringResource(R.string.last_cooked_today)
        1 -> stringResource(R.string.last_cooked_yesterday)
        else -> pluralStringResource(R.plurals.last_cooked_days_ago, days, days)
    }
}
