package com.vezhny.cookdiary.ui.dishes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.data.Dish
import com.vezhny.cookdiary.data.TextFiles
import com.vezhny.cookdiary.domain.DishTransfer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate

class DishesViewModel(
    private val repository: CookRepository,
    private val files: TextFiles,
) : ViewModel() {
    /** null while loading. */
    val dishes: StateFlow<List<Dish>?> = repository.observeDishes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _message = MutableStateFlow<String?>(null)
    /** One-shot result of import/export to show in a snackbar. */
    val message: StateFlow<String?> = _message.asStateFlow()

    fun exportFileName(today: LocalDate = LocalDate.now()) = "cook-diary-dishes-$today.json"

    fun export(uri: Uri) = viewModelScope.launch {
        _message.value = try {
            val dishes = repository.getAllDishes()
            files.write(uri, DishTransfer.encode(dishes))
            "Экспортировано блюд: ${dishes.size}"
        } catch (e: IOException) {
            "Не удалось сохранить файл"
        }
    }

    fun import(uri: Uri) = viewModelScope.launch {
        _message.value = try {
            val dishes = DishTransfer.decode(files.read(uri))
            val added = repository.importDishes(dishes)
            val skipped = dishes.size - added
            if (skipped == 0) "Добавлено блюд: $added" else "Добавлено блюд: $added, уже были в списке: $skipped"
        } catch (e: IOException) {
            "Не удалось прочитать файл"
        } catch (e: IllegalArgumentException) {
            "Это не файл со списком блюд"
        }
    }

    fun messageShown() {
        _message.value = null
    }
}
