package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
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
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: TrainingHomeViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            TrainingHomeViewModel(
                observePlansUseCase = c.observePlansUseCase,
                observeWorkoutsUseCase = c.observeWorkoutsUseCase,
                observeActiveSessionUseCase = c.observeActiveSessionUseCase,
            )
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.training_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.training_home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        state.activeSession?.let { session ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSession(session.id) },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.training_active_session),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = session.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.training_section_plans),
                actionLabel = stringResource(R.string.action_see_all),
                onAction = onOpenPlans,
            )
        }
        if (state.plans.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.training_empty_plans),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.plans.take(5), key = { it.id }) { plan ->
                SimpleListRow(
                    title = plan.name,
                    subtitle = plan.notes.ifBlank {
                        stringResource(R.string.training_plan_open_hint)
                    },
                    onClick = { onOpenPlan(plan.id) },
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.training_section_workouts),
                actionLabel = stringResource(R.string.action_see_all),
                onAction = onOpenWorkouts,
            )
        }
        if (state.workouts.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.training_empty_workouts),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.workouts.take(5), key = { it.id }) { workout ->
                SimpleListRow(
                    title = workout.name,
                    subtitle = workout.notes.ifBlank {
                        stringResource(R.string.training_workout_no_notes)
                    },
                    onClick = { onOpenWorkout(workout.id) },
                )
            }
        }

        item {
            TextButton(onClick = onOpenExercises) {
                Text(stringResource(R.string.training_open_exercises))
            }
            TextButton(onClick = onOpenHistory) {
                Text(stringResource(R.string.training_open_history))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        TextButton(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun SimpleListRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
