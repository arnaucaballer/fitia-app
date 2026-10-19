package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.min

@Composable
fun NutritionHomeScreen(
    onOpenProfile: () -> Unit,
    onOpenTargets: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenFoods: () -> Unit,
    onAddEntry: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: NutritionHomeViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            NutritionHomeViewModel(repository = c.nutritionRepository)
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateLabel = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .format(LocalDate.ofEpochDay(state.dayEpochDay))
    val totals = state.dayLog.totals
    val targets = state.targets

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.nutrition_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.nutrition_home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = { viewModel.shiftDay(-1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.nutrition_prev_day),
                    )
                }
                Text(dateLabel, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { viewModel.shiftDay(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.nutrition_next_day),
                    )
                }
            }
        }

        item {
            MacroProgress(
                label = stringResource(R.string.nutrition_calories),
                current = totals.calories,
                target = targets?.calories?.toDouble(),
            )
            MacroProgress(
                label = stringResource(R.string.nutrition_protein),
                current = totals.proteinGrams,
                target = targets?.proteinGrams,
            )
            MacroProgress(
                label = stringResource(R.string.nutrition_carbs),
                current = totals.carbsGrams,
                target = targets?.carbsGrams,
            )
            MacroProgress(
                label = stringResource(R.string.nutrition_fat),
                current = totals.fatGrams,
                target = targets?.fatGrams,
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onOpenProfile) {
                    Text(stringResource(R.string.nutrition_profile))
                }
                TextButton(onClick = onOpenTargets) {
                    Text(stringResource(R.string.nutrition_targets))
                }
                TextButton(onClick = onOpenPlans) {
                    Text(stringResource(R.string.nutrition_plans))
                }
                TextButton(onClick = onOpenFoods) {
                    Text(stringResource(R.string.nutrition_foods))
                }
            }
        }

        state.dayLog.meals.forEach { meal ->
            item {
                Text(meal.name, style = MaterialTheme.typography.titleLarge)
                Button(onClick = { onAddEntry(meal.id) }) {
                    Text(stringResource(R.string.nutrition_add_food))
                }
            }
            items(meal.entries, key = { it.id }) { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(
                                R.string.nutrition_entry_macros,
                                entry.grams.toInt(),
                                entry.calories.toInt(),
                                entry.proteinGrams.toInt(),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { viewModel.deleteEntry(entry.id) }) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroProgress(
    label: String,
    current: Double,
    target: Double?,
) {
    val progress = if (target == null || target <= 0.0) {
        0f
    } else {
        min(1.0, current / target).toFloat()
    }
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = if (target == null) {
                    current.toInt().toString()
                } else {
                    "${current.toInt()} / ${target.toInt()}"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}
