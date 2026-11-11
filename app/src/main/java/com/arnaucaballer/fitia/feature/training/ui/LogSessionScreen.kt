package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaCard
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.design.FitiaSectionHeader
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory

@Composable
fun LogSessionScreen(
    onOpenSession: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: LogSessionViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            LogSessionViewModel(
                observeWorkoutsUseCase = c.observeWorkoutsUseCase,
                observeActiveSessionUseCase = c.observeActiveSessionUseCase,
                startSessionFromWorkoutUseCase = c.startSessionFromWorkoutUseCase,
                startBlankSessionUseCase = c.startBlankSessionUseCase,
            )
        },
    )
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    var showBlank by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sessionStarted.collect(onOpenSession)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val session = activeSession
        if (session != null) {
            item(key = "active") {
                FitiaCard(onClick = { onOpenSession(session.id) }) {
                    Text(
                        text = stringResource(R.string.training_log_continue),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = session.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }

        item(key = "blank") {
            FitiaPrimaryButton(
                text = stringResource(R.string.training_log_blank),
                onClick = { showBlank = true },
            )
        }

        item(key = "from-header") {
            FitiaSectionHeader(title = stringResource(R.string.training_log_from_workout))
        }

        if (workouts.isEmpty()) {
            item(key = "empty") {
                FitiaCard {
                    Text(
                        text = stringResource(R.string.training_log_empty_workouts),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(workouts, key = { it.id }) { workout ->
                FitiaCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = workout.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.startFromWorkout(workout.id) }) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = stringResource(R.string.training_start_session),
                            )
                        }
                    }
                    if (!workout.isGeneric) {
                        Text(
                            text = stringResource(R.string.training_workout_type_plan_only),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }

    if (showBlank) {
        NameInputDialog(
            title = stringResource(R.string.training_log_blank),
            label = stringResource(R.string.field_name),
            onDismiss = { showBlank = false },
            onConfirm = { name ->
                showBlank = false
                viewModel.startBlank(name)
            },
        )
    }
}
