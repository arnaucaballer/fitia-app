package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
import com.arnaucaballer.fitia.design.ItemActionsDialog
import com.arnaucaballer.fitia.design.fitiaClickable
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import com.arnaucaballer.fitia.feature.nutrition.domain.Meal
import com.arnaucaballer.fitia.feature.nutrition.domain.MealEntry
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionPlan
import com.arnaucaballer.fitia.feature.nutrition.domain.WaterLog
import com.arnaucaballer.fitia.feature.training.ui.NameInputDialog
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest
import com.arnaucaballer.fitia.design.mediumDateFormatter
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NutritionHomeScreen(
    selectedDayFlow: StateFlow<Long>,
    onSelectedDayConsumed: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTargets: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenFoods: () -> Unit,
    onOpenCalendar: () -> Unit,
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
    val selectedDay by selectedDayFlow.collectAsStateWithLifecycle()
    LaunchedEffect(selectedDay) {
        if (selectedDay == Long.MIN_VALUE) return@LaunchedEffect
        viewModel.selectDay(selectedDay)
        onSelectedDayConsumed()
    }
    val dateLabel = mediumDateFormatter.format(LocalDate.ofEpochDay(state.dayEpochDay))
    val totals = state.dayLog.totals
    val targets = state.targets
    val needsPlan = state.plans.isEmpty() || targets == null
    val snackbar = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.deleted_snackbar)
    val undoLabel = stringResource(R.string.action_undo)
    val longPressLabel = stringResource(R.string.long_press_action)
    var pending by remember { mutableStateOf<NutritionItemAction?>(null) }
    var editPlan by remember { mutableStateOf<NutritionPlan?>(null) }
    var editEntry by remember { mutableStateOf<MealEntry?>(null) }
    var renameMeal by remember { mutableStateOf<Meal?>(null) }
    var showAddMeal by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.undoPrompts.collectLatest { token ->
            val result = snackbar.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undo(token)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 28.dp + padding.calculateTopPadding(),
            bottom = 28.dp + padding.calculateBottomPadding(),
        ),
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
            Text(
                text = stringResource(R.string.long_press_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
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
                FitiaCard(
                    onClick = onOpenPlans,
                    onLongClick = { pending = NutritionItemAction.PlanItem(plan) },
                    onLongClickLabel = longPressLabel,
                ) {
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
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable(onClick = onOpenCalendar),
                )
                IconButton(onClick = { viewModel.shiftDay(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.nutrition_next_day),
                    )
                }
            }
        }

        item {
            FitiaSecondaryButton(
                text = stringResource(R.string.nutrition_calendar),
                onClick = onOpenCalendar,
                modifier = Modifier.fillMaxWidth(),
            )
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
                if (state.waterLogs.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.nutrition_water_logs),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    state.waterLogs.forEach { log ->
                        Text(
                            text = stringResource(R.string.nutrition_water_log, log.amountMl),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fitiaClickable(
                                    onClick = {},
                                    onLongClick = { pending = NutritionItemAction.WaterItem(log) },
                                    onLongClickLabel = longPressLabel,
                                )
                                .padding(vertical = 6.dp),
                        )
                    }
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

        if (state.dayLog.meals.isEmpty()) {
            item(key = "meals-empty") {
                Text(
                    text = stringResource(R.string.nutrition_empty_meals),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        state.dayLog.meals.forEach { meal ->
            item(key = "meal-header-${meal.id}") {
                FitiaSectionHeader(
                    title = meal.name,
                    actionLabel = stringResource(R.string.nutrition_add_food),
                    onAction = { onAddEntry(meal.id) },
                    onTitleLongClick = { pending = NutritionItemAction.MealItem(meal) },
                    onTitleLongClickLabel = longPressLabel,
                )
            }
            items(
                items = meal.entries,
                key = { entry -> "meal-${meal.id}-entry-${entry.id}" },
            ) { entry ->
                FitiaCard(
                    onClick = { editEntry = entry },
                    onLongClick = { pending = NutritionItemAction.EntryItem(entry) },
                    onLongClickLabel = longPressLabel,
                ) {
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
                        IconButton(onClick = { viewModel.deleteEntry(entry) }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                            )
                        }
                    }
                }
            }
        }
        item(key = "add-meal") {
            FitiaSecondaryButton(
                text = stringResource(R.string.nutrition_add_meal),
                onClick = { showAddMeal = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    }

    when (val action = pending) {
        null -> Unit
        is NutritionItemAction.PlanItem -> ItemActionsDialog(
            title = action.plan.name,
            onDismiss = { pending = null },
            onEdit = {
                pending = null
                editPlan = action.plan
            },
            onDelete = {
                pending = null
                viewModel.deletePlan(action.plan)
            },
        )
        is NutritionItemAction.MealItem -> ItemActionsDialog(
            title = action.meal.name,
            onDismiss = { pending = null },
            onRename = {
                pending = null
                renameMeal = action.meal
            },
            onDelete = {
                pending = null
                viewModel.deleteMeal(action.meal)
            },
        )
        is NutritionItemAction.EntryItem -> ItemActionsDialog(
            title = action.entry.name,
            onDismiss = { pending = null },
            onEdit = {
                pending = null
                editEntry = action.entry
            },
            onDelete = {
                pending = null
                viewModel.deleteEntry(action.entry)
            },
        )
        is NutritionItemAction.WaterItem -> ItemActionsDialog(
            title = stringResource(R.string.nutrition_water_log, action.log.amountMl),
            onDismiss = { pending = null },
            onDelete = {
                pending = null
                viewModel.deleteWater(action.log)
            },
        )
    }

    val planToEdit = editPlan
    if (planToEdit != null) {
        NutritionPlanEditDialog(
            plan = planToEdit,
            onDismiss = { editPlan = null },
            onConfirm = { updated ->
                viewModel.savePlan(updated)
                editPlan = null
            },
        )
    }
    val entryToEdit = editEntry
    if (entryToEdit != null) {
        MealEntryEditDialog(
            entry = entryToEdit,
            onDismiss = { editEntry = null },
            onConfirm = { updated ->
                viewModel.updateEntry(updated)
                editEntry = null
            },
        )
    }
    val mealToRename = renameMeal
    if (mealToRename != null) {
        NameInputDialog(
            title = stringResource(R.string.nutrition_rename_meal),
            initialValue = mealToRename.name,
            label = stringResource(R.string.field_name),
            onDismiss = { renameMeal = null },
            onConfirm = { name ->
                viewModel.renameMeal(mealToRename.id, name)
                renameMeal = null
            },
        )
    }
    if (showAddMeal) {
        NameInputDialog(
            title = stringResource(R.string.nutrition_add_meal),
            label = stringResource(R.string.field_name),
            onDismiss = { showAddMeal = false },
            onConfirm = { name ->
                viewModel.addMeal(name)
                showAddMeal = false
            },
        )
    }
}

private sealed interface NutritionItemAction {
    data class PlanItem(val plan: NutritionPlan) : NutritionItemAction
    data class MealItem(val meal: Meal) : NutritionItemAction
    data class EntryItem(val entry: MealEntry) : NutritionItemAction
    data class WaterItem(val log: WaterLog) : NutritionItemAction
}

@Composable
private fun NutritionPlanEditDialog(
    plan: NutritionPlan,
    onDismiss: () -> Unit,
    onConfirm: (NutritionPlan) -> Unit,
) {
    var name by remember(plan.id) { mutableStateOf(plan.name) }
    var calories by remember(plan.id) { mutableStateOf(plan.calories.toString()) }
    var protein by remember(plan.id) { mutableStateOf(trimNumber(plan.proteinGrams)) }
    var carbs by remember(plan.id) { mutableStateOf(trimNumber(plan.carbsGrams)) }
    var fat by remember(plan.id) { mutableStateOf(trimNumber(plan.fatGrams)) }
    val parsedCalories = calories.toNumber()?.toInt()
    val parsedProtein = protein.toNumber()
    val parsedCarbs = carbs.toNumber()
    val parsedFat = fat.toNumber()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nutrition_edit_plan)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.field_name)) },
                    singleLine = true,
                )
                NumberField(calories, { calories = it }, stringResource(R.string.nutrition_calories))
                NumberField(protein, { protein = it }, stringResource(R.string.nutrition_protein))
                NumberField(carbs, { carbs = it }, stringResource(R.string.nutrition_carbs))
                NumberField(fat, { fat = it }, stringResource(R.string.nutrition_fat))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        plan.copy(
                            name = name.trim(),
                            calories = parsedCalories ?: return@TextButton,
                            proteinGrams = parsedProtein ?: return@TextButton,
                            carbsGrams = parsedCarbs ?: return@TextButton,
                            fatGrams = parsedFat ?: return@TextButton,
                        ),
                    )
                },
                enabled = name.isNotBlank() &&
                    parsedCalories != null &&
                    parsedProtein != null &&
                    parsedCarbs != null &&
                    parsedFat != null,
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun MealEntryEditDialog(
    entry: MealEntry,
    onDismiss: () -> Unit,
    onConfirm: (MealEntry) -> Unit,
) {
    var name by remember(entry.id) { mutableStateOf(entry.name) }
    var grams by remember(entry.id) { mutableStateOf(trimNumber(entry.grams)) }
    var calories by remember(entry.id) { mutableStateOf(trimNumber(entry.calories)) }
    var protein by remember(entry.id) { mutableStateOf(trimNumber(entry.proteinGrams)) }
    var carbs by remember(entry.id) { mutableStateOf(trimNumber(entry.carbsGrams)) }
    var fat by remember(entry.id) { mutableStateOf(trimNumber(entry.fatGrams)) }
    val parsedGrams = grams.toNumber()
    val parsedCalories = calories.toNumber()
    val parsedProtein = protein.toNumber()
    val parsedCarbs = carbs.toNumber()
    val parsedFat = fat.toNumber()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nutrition_edit_entry)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.field_name)) },
                    singleLine = true,
                )
                NumberField(grams, { grams = it }, stringResource(R.string.field_grams))
                NumberField(calories, { calories = it }, stringResource(R.string.nutrition_calories))
                NumberField(protein, { protein = it }, stringResource(R.string.nutrition_protein))
                NumberField(carbs, { carbs = it }, stringResource(R.string.nutrition_carbs))
                NumberField(fat, { fat = it }, stringResource(R.string.nutrition_fat))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        entry.copy(
                            name = name.trim(),
                            grams = parsedGrams ?: return@TextButton,
                            calories = parsedCalories ?: return@TextButton,
                            proteinGrams = parsedProtein ?: return@TextButton,
                            carbsGrams = parsedCarbs ?: return@TextButton,
                            fatGrams = parsedFat ?: return@TextButton,
                        ),
                    )
                },
                enabled = name.isNotBlank() &&
                    parsedGrams != null &&
                    parsedCalories != null &&
                    parsedProtein != null &&
                    parsedCarbs != null &&
                    parsedFat != null,
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

private fun trimNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

private fun String.toNumber(): Double? = trim().replace(',', '.').toDoubleOrNull()
