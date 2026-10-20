package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaCard
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.design.FitiaSecondaryButton
import com.arnaucaballer.fitia.design.FitiaSectionHeader
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory

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
            )
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val plans = state.plans
    val genericWorkouts = state.genericWorkouts
    val activeSession = state.activeSession

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
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
                FitiaCard(onClick = { onOpenSession(activeSession.id) }) {
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

        item(key = "generate-plan") {
            FitiaPrimaryButton(
                text = stringResource(R.string.training_generate_plan_ai),
                onClick = onGeneratePlan,
            )
        }

        if (plans.isEmpty()) {
            item(key = "plans-empty") {
                FitiaCard {
                    Text(
                        text = stringResource(R.string.training_empty_plans),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(
                items = plans,
                key = { plan -> "plan-${plan.id}" },
            ) { plan ->
                FitiaCard(onClick = { onOpenPlan(plan.id) }) {
                    Text(plan.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = stringResource(
                            R.string.training_plan_workouts_count,
                            plan.workouts.size,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
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
                FitiaCard(onClick = { onOpenWorkout(workout.id) }) {
                    Text(workout.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.training_workout_generic_badge),
                        color = MaterialTheme.colorScheme.primary,
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
