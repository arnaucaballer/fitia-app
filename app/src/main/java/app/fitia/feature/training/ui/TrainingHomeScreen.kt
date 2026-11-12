package app.fitia.feature.training.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.fitia.R
import app.fitia.design.FitiaCard
import app.fitia.design.FitiaCardShape
import app.fitia.design.FitiaPrimaryButton
import app.fitia.design.FitiaSecondaryButton
import app.fitia.design.FitiaSectionHeader
import app.fitia.design.ItemActionsDialog
import app.fitia.design.fitiaClickable
import app.fitia.design.fitiaPress
import app.fitia.di.fitiaContainer
import app.fitia.di.fitiaViewModelFactory
import app.fitia.feature.training.domain.TrainingPlan
import app.fitia.feature.training.domain.TrainingSession
import app.fitia.feature.training.domain.Workout
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TrainingHomeScreen(
    onOpenPlans: () -> Unit,
    onOpenWorkouts: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPlan: (Long) -> Unit,
    onOpenWorkout: (Long) -> Unit,
    onOpenSession: (Long) -> Unit,
    onGeneratePlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: TrainingHomeViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            TrainingHomeViewModel(
                observePlansUseCase = c.observePlansUseCase,
                observeGenericWorkoutsUseCase = c.observeGenericWorkoutsUseCase,
                observeActiveSessionUseCase = c.observeActiveSessionUseCase,
                getPlanUseCase = c.getPlanUseCase,
                savePlanUseCase = c.savePlanUseCase,
                deletePlanUseCase = c.deletePlanUseCase,
                getWorkoutUseCase = c.getWorkoutUseCase,
                saveWorkoutUseCase = c.saveWorkoutUseCase,
                deleteWorkoutUseCase = c.deleteWorkoutUseCase,
                getSessionUseCase = c.getSessionUseCase,
                deleteSessionUseCase = c.deleteSessionUseCase,
                restoreSessionUseCase = c.restoreSessionUseCase,
                renameSessionUseCase = c.renameSessionUseCase,
            )
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val plans = state.plans
    val genericWorkouts = state.genericWorkouts
    val activeSession = state.activeSession
    val snackbar = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.deleted_snackbar)
    val undoLabel = stringResource(R.string.action_undo)
    val longPressLabel = stringResource(R.string.long_press_action)
    var pending by remember { mutableStateOf<TrainingItemAction?>(null) }
    var rename by remember { mutableStateOf<TrainingRename?>(null) }

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
        item(key = "header") {
            Text(
                text = stringResource(R.string.training_title),
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = stringResource(R.string.training_home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
        }

        if (activeSession != null) {
            item(key = "active-session-${activeSession.id}") {
                FitiaCard(
                    onClick = { onOpenSession(activeSession.id) },
                    onLongClick = { pending = TrainingItemAction.SessionItem(activeSession) },
                    onLongClickLabel = longPressLabel,
                ) {
                    Text(
                        text = stringResource(R.string.training_active_session),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = activeSession.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }

        item(key = "plans-header") {
            FitiaSectionHeader(
                title = stringResource(R.string.training_section_plans),
                actionLabel = stringResource(R.string.see_all),
                onAction = onOpenPlans,
            )
        }

        if (plans.isEmpty()) {
            item(key = "plans-empty") {
                FeaturedPlanEmptyCard(onGeneratePlan = onGeneratePlan)
            }
        } else {
            items(
                items = plans,
                key = { plan -> "plan-${plan.id}" },
            ) { plan ->
                FeaturedPlanCard(
                    plan = plan,
                    onClick = { onOpenPlan(plan.id) },
                    onLongClick = { pending = TrainingItemAction.PlanItem(plan) },
                    onOpenWorkout = onOpenWorkout,
                    onWorkoutLongClick = { workout ->
                        pending = TrainingItemAction.PlanWorkout(plan.id, workout)
                    },
                    longPressLabel = longPressLabel,
                )
            }
            item(key = "generate-plan") {
                FitiaSecondaryButton(
                    text = stringResource(R.string.training_generate_plan_ai),
                    onClick = onGeneratePlan,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item(key = "workouts-header") {
            FitiaSectionHeader(
                title = stringResource(R.string.training_section_generic_workouts),
                actionLabel = stringResource(R.string.see_all),
                onAction = onOpenWorkouts,
            )
        }

        if (genericWorkouts.isEmpty()) {
            item(key = "workouts-empty") {
                FitiaCard {
                    Text(
                        text = stringResource(R.string.training_empty_workouts),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(
                items = genericWorkouts.take(5),
                key = { workout -> "workout-${workout.id}" },
            ) { workout ->
                FitiaCard(
                    onClick = { onOpenWorkout(workout.id) },
                    onLongClick = { pending = TrainingItemAction.WorkoutItem(workout) },
                    onLongClickLabel = longPressLabel,
                ) {
                    Text(workout.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.training_workout_generic_badge),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item(key = "secondary-actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FitiaSecondaryButton(
                    text = stringResource(R.string.training_open_exercises),
                    onClick = onOpenExercises,
                    modifier = Modifier.weight(1f),
                )
                FitiaSecondaryButton(
                    text = stringResource(R.string.training_open_history),
                    onClick = onOpenHistory,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    }

    when (val action = pending) {
        null -> Unit
        is TrainingItemAction.PlanItem -> ItemActionsDialog(
            title = action.plan.name,
            onDismiss = { pending = null },
            onRename = {
                pending = null
                rename = TrainingRename.Plan(action.plan.id, action.plan.name)
            },
            onDelete = {
                pending = null
                viewModel.deletePlan(action.plan.id)
            },
        )
        is TrainingItemAction.WorkoutItem -> ItemActionsDialog(
            title = action.workout.name,
            onDismiss = { pending = null },
            onRename = {
                pending = null
                rename = TrainingRename.Workout(action.workout.id, action.workout.name)
            },
            onDelete = {
                pending = null
                viewModel.deleteWorkout(action.workout.id)
            },
        )
        is TrainingItemAction.SessionItem -> ItemActionsDialog(
            title = action.session.name,
            onDismiss = { pending = null },
            onRename = {
                pending = null
                rename = TrainingRename.Session(action.session.id, action.session.name)
            },
            onDelete = {
                pending = null
                viewModel.deleteSession(action.session.id)
            },
        )
        is TrainingItemAction.PlanWorkout -> ItemActionsDialog(
            title = action.workout.name,
            onDismiss = { pending = null },
            onRename = {
                pending = null
                rename = TrainingRename.Workout(action.workout.id, action.workout.name)
            },
            onDelete = {
                pending = null
                viewModel.removeWorkoutFromPlan(action.planId, action.workout.id)
            },
        )
    }

    val renameTarget = rename
    if (renameTarget != null) {
        NameInputDialog(
            title = stringResource(renameTarget.titleRes),
            initialValue = renameTarget.currentName,
            label = stringResource(R.string.field_name),
            onDismiss = { rename = null },
            onConfirm = { name ->
                when (renameTarget) {
                    is TrainingRename.Plan -> viewModel.renamePlan(renameTarget.id, name)
                    is TrainingRename.Workout -> viewModel.renameWorkout(renameTarget.id, name)
                    is TrainingRename.Session -> viewModel.renameSession(renameTarget.id, name)
                }
                rename = null
            },
        )
    }
}

@Composable
private fun FeaturedPlanCard(
    plan: TrainingPlan,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onOpenWorkout: (Long) -> Unit,
    onWorkoutLongClick: (Workout) -> Unit,
    longPressLabel: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .fitiaPress()
            .clip(FitiaCardShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = FitiaCardShape,
            )
            .fitiaClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = longPressLabel,
                press = false,
            ),
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Text(
                text = stringResource(R.string.training_plan_badge),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = plan.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = stringResource(
                    R.string.training_plan_workouts_count,
                    plan.workouts.size,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp),
            )
            if (plan.workouts.isEmpty()) {
                Text(
                    text = stringResource(R.string.training_plan_no_workouts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    plan.workouts.forEachIndexed { index, workout ->
                        val typeLabel = if (workout.isGeneric) {
                            stringResource(R.string.training_plan_item_generic)
                        } else {
                            stringResource(R.string.training_plan_item_owned)
                        }
                        Text(
                            text = "${index + 1}. ${workout.name} · $typeLabel",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.fitiaClickable(
                                onClick = { onOpenWorkout(workout.id) },
                                onLongClick = { onWorkoutLongClick(workout) },
                                onLongClickLabel = longPressLabel,
                                press = false,
                            ),
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.training_plan_open_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun FeaturedPlanEmptyCard(onGeneratePlan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FitiaCardShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                shape = FitiaCardShape,
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.training_plan_badge),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = stringResource(R.string.training_empty_plans),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        FitiaPrimaryButton(
            text = stringResource(R.string.training_generate_plan_ai),
            onClick = onGeneratePlan,
        )
    }
}

private sealed interface TrainingItemAction {
    data class PlanItem(val plan: TrainingPlan) : TrainingItemAction
    data class WorkoutItem(val workout: Workout) : TrainingItemAction
    data class SessionItem(val session: TrainingSession) : TrainingItemAction
    data class PlanWorkout(val planId: Long, val workout: Workout) : TrainingItemAction
}

private sealed interface TrainingRename {
    val id: Long
    val currentName: String
    val titleRes: Int

    data class Plan(
        override val id: Long,
        override val currentName: String,
    ) : TrainingRename {
        override val titleRes: Int = R.string.training_rename_plan
    }

    data class Workout(
        override val id: Long,
        override val currentName: String,
    ) : TrainingRename {
        override val titleRes: Int = R.string.training_rename_workout
    }

    data class Session(
        override val id: Long,
        override val currentName: String,
    ) : TrainingRename {
        override val titleRes: Int = R.string.training_rename_session
    }
}
