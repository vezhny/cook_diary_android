package com.vezhny.cookdiary.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.cookdiary.R
import com.vezhny.cookdiary.data.DishWithLastCooked
import com.vezhny.cookdiary.domain.DishHeat
import com.vezhny.cookdiary.ui.EmptyState
import com.vezhny.cookdiary.ui.formatLastCooked
import com.vezhny.cookdiary.ui.settings.SettingsViewModel
import com.vezhny.cookdiary.ui.settings.SettingsDialog
import com.vezhny.cookdiary.ui.theme.HotRed
import com.vezhny.cookdiary.ui.toLocalDate
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun HomeScreen(
    onAddDish: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val chooser by viewModel.chooser.collectAsStateWithLifecycle()
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    // Saveable: the dialog stays open while a language change recreates the activity.
    var settingsOpen by rememberSaveable { mutableStateOf(false) }

    if (settingsOpen) {
        SettingsDialog(
            themeMode = themeMode,
            language = settingsViewModel.language(),
            onThemeMode = settingsViewModel::setThemeMode,
            onLanguage = settingsViewModel::setLanguage,
            onDismiss = { settingsOpen = false },
        )
    }

    LaunchedEffect(state.justCooked) {
        if (state.justCooked != null) {
            delay(2500)
            viewModel.dismissMessage()
        }
    }

    state.confirming?.let { dish ->
        AlertDialog(
            onDismissRequest = viewModel::cancelConfirm,
            title = { Text(stringResource(R.string.confirm_cooked, dish.dish.name)) },
            confirmButton = { TextButton(onClick = { viewModel.confirmCooked() }) { Text(stringResource(R.string.yes)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelConfirm) { Text(stringResource(R.string.no)) } },
        )
    }

    if (state.choosing) {
        BackHandler(onBack = viewModel::stopChoosing)
        DishChooser(
            chooser = chooser,
            selectedTags = state.selectedTags,
            onToggleTag = viewModel::toggleTag,
            onClearTags = viewModel::clearTags,
            onAddDish = onAddDish,
            onSelect = viewModel::select,
            onClose = viewModel::stopChoosing,
        )
    } else {
        StartPane(
            justCooked = state.justCooked,
            onChoose = viewModel::startChoosing,
            onOpenSettings = { settingsOpen = true },
        )
    }
}

@Composable
private fun StartPane(justCooked: String?, onChoose: () -> Unit, onOpenSettings: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        IconButton(onClick = onOpenSettings, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
        }
        StartContent(justCooked, onChoose)
    }
}

@Composable
private fun StartContent(justCooked: String?, onChoose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.cook_diary_art),
            contentDescription = null,
            modifier = Modifier.width(220.dp),
        )
        // Reserve the line so the button doesn't jump when the message appears.
        Text(
            justCooked?.let { stringResource(R.string.just_cooked, it) } ?: "",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            minLines = 2,
        )
        Button(onClick = onChoose, modifier = Modifier.fillMaxWidth().height(72.dp)) {
            Text(stringResource(R.string.action_what_to_cook), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Suppress("DEPRECATION") // Icons.Filled.List: AutoMirrored variant lives in material-icons-extended.
@Composable
private fun DishChooser(
    chooser: ChooserList?,
    selectedTags: Set<String>,
    onToggleTag: (String) -> Unit,
    onClearTags: () -> Unit,
    onAddDish: () -> Unit,
    onSelect: (DishWithLastCooked) -> Unit,
    onClose: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.choose_dish_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close)) }
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
            chooser.noDishes -> EmptyState(
                icon = Icons.Filled.List,
                title = stringResource(R.string.chooser_empty_title),
                text = stringResource(R.string.chooser_empty_text),
            ) {
                Button(onClick = onAddDish) { Text(stringResource(R.string.add_dish)) }
            }
            dishes.isEmpty() -> EmptyState(
                icon = Icons.Filled.Search,
                title = stringResource(R.string.filter_empty_title),
                text = stringResource(R.string.filter_empty_text),
            ) {
                TextButton(onClick = onClearTags) { Text(stringResource(R.string.reset_filter)) }
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
