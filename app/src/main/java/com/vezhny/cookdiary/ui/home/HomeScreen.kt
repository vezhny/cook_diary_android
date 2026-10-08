package com.vezhny.cookdiary.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.cookdiary.data.DishWithLastCooked
import com.vezhny.cookdiary.domain.DishHeat
import com.vezhny.cookdiary.ui.formatLastCooked
import com.vezhny.cookdiary.ui.toLocalDate
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

private val HotRed = Color(0xFFD32F2F)

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val chooser by viewModel.chooser.collectAsStateWithLifecycle()

    LaunchedEffect(state.justCooked) {
        if (state.justCooked != null) {
            delay(2500)
            viewModel.dismissMessage()
        }
    }

    state.confirming?.let { dish ->
        AlertDialog(
            onDismissRequest = viewModel::cancelConfirm,
            title = { Text("Вы приготовили «${dish.dish.name}»?") },
            confirmButton = { TextButton(onClick = { viewModel.confirmCooked() }) { Text("Да") } },
            dismissButton = { TextButton(onClick = viewModel::cancelConfirm) { Text("Нет") } },
        )
    }

    if (state.choosing) {
        BackHandler(onBack = viewModel::stopChoosing)
        DishChooser(
            chooser = chooser,
            selectedTags = state.selectedTags,
            onToggleTag = viewModel::toggleTag,
            onClearTags = viewModel::clearTags,
            onSelect = viewModel::select,
            onClose = viewModel::stopChoosing,
        )
    } else {
        StartPane(justCooked = state.justCooked, onChoose = viewModel::startChoosing)
    }
}

@Composable
private fun StartPane(justCooked: String?, onChoose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (justCooked != null) {
            Text("Записано: $justCooked. Приятного аппетита!", textAlign = TextAlign.Center)
        }
        Button(onClick = onChoose, modifier = Modifier.fillMaxWidth().height(72.dp)) {
            Text("Что приготовить?", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun DishChooser(
    chooser: ChooserList?,
    selectedTags: Set<String>,
    onToggleTag: (String) -> Unit,
    onClearTags: () -> Unit,
    onSelect: (DishWithLastCooked) -> Unit,
    onClose: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Выберите блюдо", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Закрыть") }
        }

        if (chooser != null && chooser.allTags.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(chooser.allTags, key = { it }) { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = { onToggleTag(tag) },
                        label = { Text(tag) },
                    )
                }
            }
        }

        val dishes = chooser?.dishes
        when {
            chooser == null || dishes == null -> Unit
            chooser.noDishes -> CenteredMessage("Пока нет ни одного блюда. Добавьте их во вкладке «Блюда».")
            dishes.isEmpty() -> CenteredMessage("Нет блюд со всеми выбранными тегами.") {
                TextButton(onClick = onClearTags) { Text("Сбросить фильтр") }
            }
            else -> {
                val today = LocalDate.now()
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(dishes, key = { it.dish.id }) { item ->
                        val heat = DishHeat.of(item.lastCookedAt?.toLocalDate(), today)
                        DishHeatCard(item, heat, today, onClick = { onSelect(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(text: String, action: @Composable () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, textAlign = TextAlign.Center)
        action()
    }
}

@Composable
private fun DishHeatCard(item: DishWithLastCooked, heat: Float, today: LocalDate, onClick: () -> Unit) {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val container = lerp(base, HotRed, heat)
    val content = if (heat > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.dish.name, style = MaterialTheme.typography.titleMedium)
            if (item.dish.tagList.isNotEmpty()) {
                Text(item.dish.tagList.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
            }
            Text(formatLastCooked(item.lastCookedAt, today), style = MaterialTheme.typography.bodySmall)
        }
    }
}
