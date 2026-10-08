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
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/** Some file managers report .json files as plain text or binary. */
private val importMimeTypes = arrayOf("application/json", "text/plain", "application/octet-stream")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishesScreen(
    onEditDish: (Long) -> Unit,
    viewModel: DishesViewModel = koinViewModel(),
) {
    val dishes by viewModel.dishes.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        uri -> uri?.let(viewModel::export)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri -> uri?.let(viewModel::import)
    }

    LaunchedEffect(message) {
        message?.let {
            viewModel.messageShown()
            snackbar.showSnackbar(it)
        }
    }

    Scaffold(
        // The app-level Scaffold already applies system bar insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Блюда") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Ещё")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Экспорт в файл") },
                                enabled = !dishes.isNullOrEmpty(),
                                onClick = {
                                    menuOpen = false
                                    exportLauncher.launch(viewModel.exportFileName())
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Импорт из файла") },
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
            FloatingActionButton(onClick = { onEditDish(0) }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить блюдо")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val list = dishes
            when {
                list == null -> Unit
                list.isEmpty() -> Text(
                    "Список пуст. Нажмите «+», чтобы добавить первое блюдо, или импортируйте список через меню «⋮».",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
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
                                    Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
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
