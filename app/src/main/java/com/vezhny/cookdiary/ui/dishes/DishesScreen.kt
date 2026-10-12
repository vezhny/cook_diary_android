package com.vezhny.cookdiary.ui.dishes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.cookdiary.R
import com.vezhny.cookdiary.ui.EmptyState
import org.koin.androidx.compose.koinViewModel

/** Some file managers report .json files as plain text or binary. */
private val importMimeTypes = arrayOf("application/json", "text/plain", "application/octet-stream")

@Suppress("DEPRECATION") // Icons.Filled.List: AutoMirrored variant lives in material-icons-extended.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishesScreen(
    onEditDish: (Long) -> Unit,
    viewModel: DishesViewModel = koinViewModel(),
) {
    val dishes by viewModel.dishes.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val messageText = message?.let { transferMessageText(it) }
    val snackbar = remember { SnackbarHostState() }
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    // Saveable: the file picker is another activity, our process may be killed meanwhile.
    var exportScope by rememberSaveable { mutableStateOf(ExportScope.DISHES) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        uri -> uri?.let { viewModel.export(it, exportScope) }
    }

    fun startExport(scope: ExportScope) {
        menuOpen = false
        exportScope = scope
        exportLauncher.launch(viewModel.exportFileName(scope))
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri -> uri?.let(viewModel::import)
    }

    LaunchedEffect(message) {
        // Clear only after showing: clearing changes the key, which would cancel the snackbar.
        val text = messageText ?: return@LaunchedEffect
        snackbar.showSnackbar(text)
        viewModel.messageShown()
    }

    Scaffold(
        // The app-level Scaffold already applies system bar insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_dishes)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_dishes)) },
                                enabled = !dishes.isNullOrEmpty(),
                                onClick = { startExport(ExportScope.DISHES) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_everything)) },
                                enabled = !dishes.isNullOrEmpty(),
                                onClick = { startExport(ExportScope.EVERYTHING) },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_from_file)) },
                                onClick = {
                                    menuOpen = false
                                    importLauncher.launch(importMimeTypes)
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            // The empty state has its own "add" button.
            if (!dishes.isNullOrEmpty()) {
                FloatingActionButton(onClick = { onEditDish(0) }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_dish))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val list = dishes
            when {
                list == null -> Unit
                list.isEmpty() -> EmptyState(
                    icon = Icons.Filled.List,
                    title = stringResource(R.string.dishes_empty_title),
                    text = stringResource(R.string.dishes_empty_text),
                ) {
                    Button(onClick = { onEditDish(0) }) { Text(stringResource(R.string.add_dish)) }
                    TextButton(onClick = { importLauncher.launch(importMimeTypes) }) { Text(stringResource(R.string.import_from_file)) }
                }
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(list, key = { it.id }) { dish ->
                        ListItem(
                            headlineContent = { Text(dish.name) },
                            supportingContent = if (dish.tagList.isEmpty() && dish.note.isNullOrBlank()) null else {
                                {
                                    Column {
                                        if (dish.tagList.isNotEmpty()) {
                                            Text(
                                                dish.tagList.joinToString(" · "),
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        if (!dish.note.isNullOrBlank()) {
                                            Text(dish.note, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = { onEditDish(dish.id) }) {
                                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
                                }
                            },
                            modifier = Modifier.clickable { onEditDish(dish.id) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun transferMessageText(message: TransferMessage): String = when (message) {
    is TransferMessage.Exported -> {
        val dishes = pluralStringResource(R.plurals.export_done, message.dishCount, message.dishCount)
        message.historyCount?.let { stringResource(R.string.export_done_with_history, dishes, it) } ?: dishes
    }
    is TransferMessage.Imported -> {
        val r = message.result
        var text = pluralStringResource(R.plurals.import_added, r.dishesAdded, r.dishesAdded)
        if (r.dishesSkipped > 0) text = stringResource(R.string.import_added_with_skipped, text, r.dishesSkipped)
        if (r.historyAdded + r.historySkipped > 0) text = stringResource(R.string.import_history_added, text, r.historyAdded)
        text
    }
    TransferMessage.ExportFailed -> stringResource(R.string.export_failed)
    TransferMessage.ReadFailed -> stringResource(R.string.import_read_failed)
    TransferMessage.InvalidFile -> stringResource(R.string.import_invalid)
}
