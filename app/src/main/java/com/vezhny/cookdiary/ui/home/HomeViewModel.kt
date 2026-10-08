package com.vezhny.cookdiary.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.data.DishWithLastCooked
import com.vezhny.cookdiary.domain.DishOrdering
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    /** The dish list is shown after "Что приготовить?" is pressed. */
    val choosing: Boolean = false,
    /** Tag filter: only dishes having all of these tags are shown. */
    val selectedTags: Set<String> = emptySet(),
    /** Dish awaiting "Вы приготовили …?" confirmation. */
    val confirming: DishWithLastCooked? = null,
    /** Name of the dish just marked as cooked, for a confirmation message. */
    val justCooked: String? = null,
)

data class ChooserList(
    val allTags: List<String>,
    val dishes: List<DishWithLastCooked>,
    /** True when there are no dishes at all, regardless of the filter. */
    val noDishes: Boolean,
)

class HomeViewModel(
    private val repository: CookRepository,
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    /** null while loading. */
    val chooser: StateFlow<ChooserList?> = combine(
        repository.observeDishesWithLastCooked(),
        _state.map { it.selectedTags }.distinctUntilChanged(),
    ) { dishes, tags ->
        ChooserList(
            allTags = DishOrdering.allTags(dishes),
            dishes = DishOrdering.arrange(dishes, tags, today()),
            noDishes = dishes.isEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun startChoosing() = _state.update { it.copy(choosing = true, justCooked = null) }

    fun stopChoosing() = _state.update { it.copy(choosing = false, confirming = null) }

    fun toggleTag(tag: String) = _state.update {
        it.copy(selectedTags = if (tag in it.selectedTags) it.selectedTags - tag else it.selectedTags + tag)
    }

    fun clearTags() = _state.update { it.copy(selectedTags = emptySet()) }

    fun select(dish: DishWithLastCooked) = _state.update { it.copy(confirming = dish) }

    fun cancelConfirm() = _state.update { it.copy(confirming = null) }

    fun confirmCooked() = viewModelScope.launch {
        val dish = _state.value.confirming ?: return@launch
        repository.markCooked(dish.dish.id)
        _state.update { HomeUiState(selectedTags = it.selectedTags, justCooked = dish.dish.name) }
    }

    fun dismissMessage() = _state.update { it.copy(justCooked = null) }
}
