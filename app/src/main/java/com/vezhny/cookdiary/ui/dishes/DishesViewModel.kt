package com.vezhny.cookdiary.ui.dishes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.data.Dish
import com.vezhny.cookdiary.data.DishWithLastCooked
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DishesViewModel(private val repository: CookRepository) : ViewModel() {
    /** null while loading. */
    val dishes: StateFlow<List<DishWithLastCooked>?> = repository.observeDishes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun markCooked(dish: Dish) = viewModelScope.launch { repository.markCooked(dish.id) }

    fun delete(dish: Dish) = viewModelScope.launch { repository.deleteDish(dish) }
}
