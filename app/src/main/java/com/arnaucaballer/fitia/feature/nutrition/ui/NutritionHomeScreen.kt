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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.arnaucaballer.fitia.design.FitiaCard
import com.arnaucaballer.fitia.design.FitiaPillChip
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.design.FitiaProgressRow
import com.arnaucaballer.fitia.design.FitiaSecondaryButton
import com.arnaucaballer.fitia.design.FitiaSectionHeader
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun NutritionHomeScreen(
    onOpenProfile: () -> Unit,
    onOpenTargets: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenFoods: () -> Unit,
    onAddEntry: (Long) -> Unit,
    onFoodPhoto: () -> Unit,
    onCreatePlanWizard: () -> Unit,
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
    val needsPlan = state.plans.isEmpty() || targets == null

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.nutrition_title),
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = stringResource(R.string.nutrition_home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (needsPlan) {
            item {
                FitiaCard {
                    Text(stringResource(R.string.nutrition_need_plan_title), style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = stringResource(R.string.nutrition_need_plan_body),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 14.dp),
                    )
                    FitiaPrimaryButton(
                        text = stringResource(R.string.nutrition_create_plan_ai),
                        onClick = onCreatePlanWizard,
                    )
                }
            }
        } else {
            item {
                FitiaSectionHeader(
                    title = stringResource(R.string.nutrition_section_plan),
                    actionLabel = stringResource(R.string.see_all),
                    onAction = onOpenPlans,
                )
            }
            items(state.plans.take(3), key = { it.id }) { plan ->
                FitiaCard(onClick = onOpenPlans) {
                    Text(plan.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(
                            R.string.nutrition_plan_macros,
                            plan.calories,
                            plan.proteinGrams.toInt(),
                            plan.carbsGrams.toInt(),
                            plan.fatGrams.toInt(),
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            item {
                FitiaSecondaryButton(
                    text = stringResource(R.string.nutrition_create_plan_ai),
                    onClick = onCreatePlanWizard,
                )
            }
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
            FitiaCard {
                Text(stringResource(R.string.nutrition_today_progress), style = MaterialTheme.typography.titleLarge)
                FitiaProgressRow(
                    label = stringResource(R.string.nutrition_calories),
                    current = totals.calories,
                    target = targets?.calories?.toDouble(),
                )
                FitiaProgressRow(
                    label = stringResource(R.string.nutrition_protein),
                    current = totals.proteinGrams,
                    target = targets?.proteinGrams,
                )
                FitiaProgressRow(
                    label = stringResource(R.string.nutrition_carbs),
                    current = totals.carbsGrams,
                    target = targets?.carbsGrams,
                )
                FitiaProgressRow(
                    label = stringResource(R.string.nutrition_fat),
                    current = totals.fatGrams,
                    target = targets?.fatGrams,
                )
                FitiaProgressRow(
                    label = stringResource(R.string.nutrition_water),
                    current = state.waterMl.toDouble(),
                    target = targets?.waterMlGoal?.toDouble() ?: 3000.0,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FitiaPillChip(text = "+250 ml", selected = false, onClick = { viewModel.addWater(250) })
                    FitiaPillChip(text = "+500 ml", selected = false, onClick = { viewModel.addWater(500) })
                }
            }
        }

        item {
            FitiaPrimaryButton(
                text = stringResource(R.string.nutrition_food_photo),
                onClick = onFoodPhoto,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FitiaSecondaryButton(
                    text = stringResource(R.string.nutrition_profile),
                    onClick = onOpenProfile,
                    modifier = Modifier.weight(1f),
                )
                FitiaSecondaryButton(
                    text = stringResource(R.string.nutrition_targets),
                    onClick = onOpenTargets,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            FitiaSecondaryButton(
                text = stringResource(R.string.nutrition_foods),
                onClick = onOpenFoods,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        state.dayLog.meals.forEach { meal ->
            item(key = "meal-header-${meal.id}") {
                FitiaSectionHeader(
                    title = meal.name,
                    actionLabel = stringResource(R.string.nutrition_add_food),
                    onAction = { onAddEntry(meal.id) },
                )
            }
            items(
                items = meal.entries,
                key = { entry -> "meal-${meal.id}-entry-${entry.id}" },
            ) { entry ->
                FitiaCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { viewModel.deleteEntry(entry.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
