package com.vezhny.cookdiary.ui.dishes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.data.Dish
import com.vezhny.cookdiary.data.ImportResult
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

/** What goes into an export file. */
enum class ExportScope { DISHES, EVERYTHING }

/** Result of import/export, turned into localized text by the screen. */
sealed interface TransferMessage {
    /** [historyCount] is null for a dishes-only export. */
    data class Exported(val dishCount: Int, val historyCount: Int?) : TransferMessage
    data class Imported(val result: ImportResult) : TransferMessage
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

    fun exportFileName(scope: ExportScope, today: LocalDate = LocalDate.now()) = when (scope) {
        ExportScope.DISHES -> "cook-diary-dishes-$today.json"
        ExportScope.EVERYTHING -> "cook-diary-dishes-and-history-$today.json"
    }

    fun export(uri: Uri, scope: ExportScope) = viewModelScope.launch {
        _message.value = try {
            val dishes = repository.getAllDishes()
            val history = if (scope == ExportScope.EVERYTHING) repository.getHistoryForExport() else null
            files.write(uri, DishTransfer.encode(dishes, history))
            TransferMessage.Exported(dishes.size, history?.size)
        } catch (e: IOException) {
            TransferMessage.ExportFailed
        }
    }

    /** Imports everything the file contains: dishes, and history if it is a full export. */
    fun import(uri: Uri) = viewModelScope.launch {
        _message.value = try {
            val data = DishTransfer.decode(files.read(uri))
            TransferMessage.Imported(repository.importData(data.dishes, data.history))
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
