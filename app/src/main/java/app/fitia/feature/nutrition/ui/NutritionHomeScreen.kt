package app.fitia.feature.nutrition.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.fitia.R
import app.fitia.design.FitiaCard
import app.fitia.design.FitiaSpace
import app.fitia.design.FitiaPillChip
import app.fitia.design.FitiaPrimaryButton
import app.fitia.design.FitiaProgressRow
import app.fitia.design.FitiaSecondaryButton
import app.fitia.design.FitiaSectionHeader
import app.fitia.design.ItemActionsDialog
import app.fitia.design.fitiaClickable
import app.fitia.di.fitiaContainer
import app.fitia.di.fitiaViewModelFactory
import app.fitia.feature.nutrition.domain.Meal
import app.fitia.feature.nutrition.domain.MealEntry
import app.fitia.feature.nutrition.domain.NutritionPlan
import app.fitia.feature.nutrition.domain.WaterLog
import app.fitia.feature.training.ui.NameInputDialog
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest
import app.fitia.design.mediumDateFormatter
import kotlinx.coroutines.flow.StateFlow
import app.fitia.design.fitiaPress
import app.fitia.navigation.RetainedPages

private enum class NutritionTab(
    @param:StringRes val label: Int,
    @param:StringRes val hint: Int,
) {
    Plan(R.string.nutrition_tab_plan, R.string.nutrition_tab_hint_plan),
    Log(R.string.nutrition_tab_log, R.string.nutrition_tab_hint_log),
}

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
    onFoodPhoto: (dayEpochDay: Long) -> Unit,
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

    var selectedTab by rememberSaveable { mutableIntStateOf(NutritionTab.Log.ordinal) }
    var openedTabs by remember { mutableStateOf(listOf(NutritionTab.Log)) }
    val tab = NutritionTab.entries[selectedTab]

    fun selectTab(next: NutritionTab) {
        selectedTab = next.ordinal
        if (next !in openedTabs) openedTabs = openedTabs + next
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            NutritionTabHeader(tab = tab, onSelect = ::selectTab)
            RetainedPages(
                pages = openedTabs,
                selected = tab,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds(),
                pageKey = { it.name },
            ) { page ->
                when (page) {
                    NutritionTab.Plan -> NutritionPlanPage(
                        plans = state.plans,
                        needsPlan = needsPlan,
                        longPressLabel = longPressLabel,
                        onOpenPlans = onOpenPlans,
                        onCreatePlanWizard = onCreatePlanWizard,
                        onOpenProfile = onOpenProfile,
                        onOpenTargets = onOpenTargets,
                        onPlanLongClick = { pending = NutritionItemAction.PlanItem(it) },
                    )
                    NutritionTab.Log -> NutritionLogPage(
                        state = state,
                        dateLabel = dateLabel,
                        needsPlan = needsPlan,
                        longPressLabel = longPressLabel,
                        onShiftDay = viewModel::shiftDay,
                        onOpenCalendar = onOpenCalendar,
                        onOpenPlanTab = { selectTab(NutritionTab.Plan) },
                        onFoodPhoto = { onFoodPhoto(state.dayEpochDay) },
                        onOpenFoods = onOpenFoods,
                        onAddEntry = onAddEntry,
                        onEditEntry = { editEntry = it },
                        onDeleteEntry = viewModel::deleteEntry,
                        onMealLongClick = { pending = NutritionItemAction.MealItem(it) },
                        onEntryLongClick = { pending = NutritionItemAction.EntryItem(it) },
                        onWaterLongClick = { pending = NutritionItemAction.WaterItem(it) },
                        onAddWater = viewModel::addWater,
                        onAddMeal = { showAddMeal = true },
                    )
                }
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

@Composable
private fun NutritionTabHeader(
    tab: NutritionTab,
    onSelect: (NutritionTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .background(MaterialTheme.colorScheme.background),
    ) {
        Text(
            text = stringResource(R.string.nutrition_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(
                start = FitiaSpace.screen,
                end = FitiaSpace.screen,
                top = FitiaSpace.screen,
            ),
        )
        Text(
            text = stringResource(tab.hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 2,
            maxLines = 2,
            modifier = Modifier.padding(
                start = FitiaSpace.screen,
                end = FitiaSpace.screen,
                top = FitiaSpace.related,
                bottom = FitiaSpace.stack,
            ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FitiaSpace.screen),
            horizontalArrangement = Arrangement.spacedBy(FitiaSpace.related),
        ) {
            NutritionTab.entries.forEach { item ->
                FitiaPillChip(
                    text = stringResource(item.label),
                    selected = item == tab,
                    onClick = { onSelect(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = FitiaSpace.stack)
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        )
    }
}

@Composable
private fun NutritionPlanPage(
    plans: List<NutritionPlan>,
    needsPlan: Boolean,
    longPressLabel: String,
    onOpenPlans: () -> Unit,
    onCreatePlanWizard: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTargets: () -> Unit,
    onPlanLongClick: (NutritionPlan) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = FitiaSpace.page(),
        verticalArrangement = Arrangement.spacedBy(FitiaSpace.stack),
    ) {
        if (needsPlan) {
            item(key = "need-plan") {
                FitiaCard {
                    Text(
                        text = stringResource(R.string.nutrition_need_plan_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.nutrition_need_plan_body),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = FitiaSpace.related, bottom = FitiaSpace.stack),
                    )
                    FitiaPrimaryButton(
                        text = stringResource(R.string.nutrition_create_plan_ai),
                        onClick = onCreatePlanWizard,
                    )
                }
            }
        } else {
            item(key = "plan-header") {
                FitiaSectionHeader(
                    title = stringResource(R.string.nutrition_section_plan),
                    actionLabel = stringResource(R.string.see_all),
                    onAction = onOpenPlans,
                )
            }
            items(plans.take(3), key = { it.id }) { plan ->
                FitiaCard(
                    onClick = onOpenPlans,
                    onLongClick = { onPlanLongClick(plan) },
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
                        modifier = Modifier.padding(top = FitiaSpace.related),
                    )
                }
            }
            item(key = "create-plan") {
                FitiaSecondaryButton(
                    text = stringResource(R.string.nutrition_create_plan_ai),
                    onClick = onCreatePlanWizard,
                )
            }
        }
        item(key = "profile-targets") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FitiaSpace.stack),
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
    }
}

@Composable
private fun NutritionLogPage(
    state: NutritionHomeUiState,
    dateLabel: String,
    needsPlan: Boolean,
    longPressLabel: String,
    onShiftDay: (Long) -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenPlanTab: () -> Unit,
    onFoodPhoto: () -> Unit,
    onOpenFoods: () -> Unit,
    onAddEntry: (Long) -> Unit,
    onEditEntry: (MealEntry) -> Unit,
    onDeleteEntry: (MealEntry) -> Unit,
    onMealLongClick: (Meal) -> Unit,
    onEntryLongClick: (MealEntry) -> Unit,
    onWaterLongClick: (WaterLog) -> Unit,
    onAddWater: (Int) -> Unit,
    onAddMeal: () -> Unit,
) {
    val totals = state.dayLog.totals
    val targets = state.targets
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = FitiaSpace.page(),
        verticalArrangement = Arrangement.spacedBy(FitiaSpace.stack),
    ) {
        item(key = "day") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(modifier = Modifier.fitiaPress(), onClick = { onShiftDay(-1L) }) {
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
                IconButton(modifier = Modifier.fitiaPress(), onClick = { onShiftDay(1L) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.nutrition_next_day),
                    )
                }
            }
        }
        item(key = "calendar") {
            FitiaSecondaryButton(
                text = stringResource(R.string.nutrition_calendar),
                onClick = onOpenCalendar,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (needsPlan) {
            item(key = "needs-plan") {
                FitiaCard(onClick = onOpenPlanTab) {
                    Text(
                        text = stringResource(R.string.nutrition_log_needs_plan),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item(key = "progress") {
            FitiaCard {
                Text(
                    text = stringResource(R.string.nutrition_today_progress),
                    style = MaterialTheme.typography.titleLarge,
                )
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
                        .padding(top = FitiaSpace.related),
                    horizontalArrangement = Arrangement.spacedBy(FitiaSpace.related),
                ) {
                    FitiaPillChip(
                        text = "+250 ml",
                        selected = false,
                        onClick = { onAddWater(250) },
                    )
                    FitiaPillChip(
                        text = "+500 ml",
                        selected = false,
                        onClick = { onAddWater(500) },
                    )
                }
                if (state.waterLogs.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.nutrition_water_logs),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = FitiaSpace.stack),
                    )
                    state.waterLogs.forEach { log ->
                        Text(
                            text = stringResource(R.string.nutrition_water_log, log.amountMl),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fitiaClickable(
                                    onClick = {},
                                    onLongClick = { onWaterLongClick(log) },
                                    onLongClickLabel = longPressLabel,
                                )
                                .padding(vertical = FitiaSpace.related),
                        )
                    }
                }
            }
        }
        item(key = "photo") {
            FitiaPrimaryButton(
                text = stringResource(R.string.nutrition_food_photo),
                onClick = onFoodPhoto,
            )
        }
        item(key = "foods") {
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
                    onTitleLongClick = { onMealLongClick(meal) },
                    onTitleLongClickLabel = longPressLabel,
                )
            }
            items(
                items = meal.entries,
                key = { entry -> "meal-${meal.id}-entry-${entry.id}" },
            ) { entry ->
                FitiaCard(
                    onClick = { onEditEntry(entry) },
                    onLongClick = { onEntryLongClick(entry) },
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
                        IconButton(modifier = Modifier.fitiaPress(), onClick = { onDeleteEntry(entry) }) {
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
                onClick = onAddMeal,
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
                verticalArrangement = Arrangement.spacedBy(FitiaSpace.related),
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
                modifier = Modifier.fitiaPress(),
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
            TextButton(modifier = Modifier.fitiaPress(), onClick = onDismiss) {
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
                verticalArrangement = Arrangement.spacedBy(FitiaSpace.related),
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
                modifier = Modifier.fitiaPress(),
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
            TextButton(modifier = Modifier.fitiaPress(), onClick = onDismiss) {
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
