package com.vezhny.cookdiary.ui.dishes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.data.Dish
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DishesViewModel(repository: CookRepository) : ViewModel() {
    /** null while loading. */
    val dishes: StateFlow<List<Dish>?> = repository.observeDishes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
