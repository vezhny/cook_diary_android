package com.vezhny.cookdiary.ui.dishes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.data.Dish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DishEditUiState(
    val name: String = "",
    val tags: String = "",
    val note: String = "",
    val isNew: Boolean = true,
    val done: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank()
}

class DishEditViewModel(
    private val dishId: Long,
    private val repository: CookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DishEditUiState(isNew = dishId == 0L))
    val state: StateFlow<DishEditUiState> = _state.asStateFlow()

    init {
        if (dishId != 0L) viewModelScope.launch {
            repository.getDish(dishId)?.let { dish ->
                _state.update {
                    it.copy(name = dish.name, tags = dish.tags.replace(",", ", "), note = dish.note.orEmpty())
                }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onTagsChange(value: String) = _state.update { it.copy(tags = value) }
    fun onNoteChange(value: String) = _state.update { it.copy(note = value) }

    fun save() = viewModelScope.launch {
        val s = _state.value
        if (!s.canSave) return@launch
        repository.saveDish(
            Dish(
                id = dishId,
                name = s.name.trim(),
                tags = normalizeTags(s.tags),
                note = s.note.trim().ifEmpty { null },
            ),
        )
        _state.update { it.copy(done = true) }
    }

    fun delete() = viewModelScope.launch {
        repository.getDish(dishId)?.let { repository.deleteDish(it) }
        _state.update { it.copy(done = true) }
    }

    companion object {
        /** "Суп,  быстро , ,суп" -> "суп,быстро" */
        fun normalizeTags(raw: String): String =
            raw.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().joinToString(",")
    }
}
