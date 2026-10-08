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

/** Result of import/export, turned into localized text by the screen. */
sealed interface TransferMessage {
    data class Exported(val count: Int) : TransferMessage
    data class Imported(val added: Int, val skipped: Int) : TransferMessage
    data object ExportFailed : TransferMessage
    data object ReadFailed : TransferMessage
    data object InvalidFile : TransferMessage
}

class DishesViewModel(
    private val repository: CookRepository,
    private val files: TextFiles,
) : ViewModel() {
    /** null while loading. */
    val dishes: StateFlow<List<Dish>?> = repository.observeDishes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _message = MutableStateFlow<TransferMessage?>(null)
    /** One-shot result of import/export to show in a snackbar. */
    val message: StateFlow<TransferMessage?> = _message.asStateFlow()

    fun exportFileName(today: LocalDate = LocalDate.now()) = "cook-diary-dishes-$today.json"

    fun export(uri: Uri) = viewModelScope.launch {
        _message.value = try {
            val dishes = repository.getAllDishes()
            files.write(uri, DishTransfer.encode(dishes))
            TransferMessage.Exported(dishes.size)
        } catch (e: IOException) {
            TransferMessage.ExportFailed
        }
    }

    fun import(uri: Uri) = viewModelScope.launch {
        _message.value = try {
            val dishes = DishTransfer.decode(files.read(uri))
            val added = repository.importDishes(dishes)
            TransferMessage.Imported(added = added, skipped = dishes.size - added)
        } catch (e: IOException) {
            TransferMessage.ReadFailed
        } catch (e: IllegalArgumentException) {
            TransferMessage.InvalidFile
        }
    }

    fun messageShown() {
        _message.value = null
    }
}
