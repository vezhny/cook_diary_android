package com.vezhny.cookdiary.ui.history

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.cookdiary.ui.EmptyState
import com.vezhny.cookdiary.ui.formatDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onChooseDish: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val days by viewModel.days.collectAsStateWithLifecycle()

    Scaffold(
        // The app-level Scaffold already applies system bar insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("История") }, windowInsets = WindowInsets(0, 0, 0, 0)) },
    ) { padding ->
        val list = days
        when {
            list == null -> Unit
            list.isEmpty() -> EmptyState(
                icon = Icons.Filled.DateRange,
                title = "История пока пуста",
                text = "Нажмите «Что приготовить?», выберите блюдо — и оно появится здесь.",
                modifier = Modifier.padding(padding),
            ) {
                Button(onClick = onChooseDish) { Text("Что приготовить?") }
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                list.forEach { day ->
                    item(key = "day-${day.date}") {
                        Text(
                            formatDate(day.date),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                        )
                    }
                    items(day.events, key = { it.eventId }) { event ->
                        ListItem(
                            headlineContent = { Text(event.dishName) },
                            trailingContent = {
                                IconButton(onClick = { viewModel.delete(event) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Убрать из истории")
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
