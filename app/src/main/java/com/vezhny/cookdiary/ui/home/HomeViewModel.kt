package com.vezhny.cookdiary.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.data.DishWithLastCooked
import com.vezhny.cookdiary.domain.DishPicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val suggestion: DishWithLastCooked? = null,
    /** Set after "Что приготовить?" found no dishes at all. */
    val noDishes: Boolean = false,
    /** Name of the dish just marked as cooked, for a confirmation message. */
    val justCooked: String? = null,
)

class HomeViewModel(
    private val repository: CookRepository,
    private val picker: DishPicker,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun suggest() = viewModelScope.launch {
        val current = _state.value.suggestion?.dish?.id
        val pick = picker.pick(repository.getDishesForPicking(), excludeId = current)
        _state.value = HomeUiState(suggestion = pick, noDishes = pick == null)
    }

    fun cookSuggestion() = viewModelScope.launch {
        val suggestion = _state.value.suggestion ?: return@launch
        repository.markCooked(suggestion.dish.id)
        _state.value = HomeUiState(justCooked = suggestion.dish.name)
    }

    fun dismissMessage() = _state.update { it.copy(justCooked = null) }
}
