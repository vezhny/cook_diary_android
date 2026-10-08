package com.vezhny.cookdiary.ui.dishes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun DishesScreen(
    onEditDish: (Long) -> Unit,
    viewModel: DishesViewModel = koinViewModel(),
) {
    val dishes by viewModel.dishes.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        val list = dishes
        when {
            list == null -> Unit
            list.isEmpty() -> Text(
                "Список пуст. Нажмите «+», чтобы добавить первое блюдо.",
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

        FloatingActionButton(
            onClick = { onEditDish(0) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить блюдо")
        }
    }
}
