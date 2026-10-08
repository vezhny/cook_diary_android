package com.vezhny.cookdiary.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.cookdiary.data.CookEventWithDish
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.ui.toLocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HistoryDay(val date: LocalDate, val events: List<CookEventWithDish>)

class HistoryViewModel(private val repository: CookRepository) : ViewModel() {
    /** Events grouped by day, newest first; null while loading. */
    val days: StateFlow<List<HistoryDay>?> = repository.observeHistory()
        .map { events -> events.groupBy { it.cookedAt.toLocalDate() }.map { (date, list) -> HistoryDay(date, list) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(event: CookEventWithDish) = viewModelScope.launch { repository.deleteCookEvent(event.eventId) }
}
