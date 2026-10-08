package com.vezhny.cookdiary.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vezhny.cookdiary.ui.formatLastCooked
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.justCooked) {
        if (state.justCooked != null) {
            delay(2500)
            viewModel.dismissMessage()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val suggestion = state.suggestion
        when {
            suggestion != null -> Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(suggestion.dish.name, style = MaterialTheme.typography.headlineMedium)
                    if (suggestion.dish.tagList.isNotEmpty()) {
                        Text(suggestion.dish.tagList.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(formatLastCooked(suggestion.lastCookedAt), style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                        Button(onClick = { viewModel.cookSuggestion() }) { Text("Готовлю") }
                        OutlinedButton(onClick = { viewModel.suggest() }) { Text("Другое") }
                    }
                }
            }
            state.noDishes -> Text(
                "Пока нет ни одного блюда. Добавьте их во вкладке «Блюда».",
                textAlign = TextAlign.Center,
            )
            state.justCooked != null -> Text(
                "Записано: ${state.justCooked}. Приятного аппетита!",
                textAlign = TextAlign.Center,
            )
        }

        if (suggestion == null) {
            Button(
                onClick = { viewModel.suggest() },
                modifier = Modifier.fillMaxWidth().height(72.dp),
            ) {
                Text("Что приготовить?", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
