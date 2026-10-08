package com.vezhny.cookdiary.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.cookdiary.ui.formatDate
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = koinViewModel()) {
    val days by viewModel.days.collectAsStateWithLifecycle()

    val list = days
    when {
        list == null -> Unit
        list.isEmpty() -> Box(Modifier.fillMaxSize()) {
            Text(
                "История пуста. Отметьте приготовленное блюдо — и оно появится здесь.",
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
            )
        }
        else -> LazyColumn(Modifier.fillMaxSize()) {
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
