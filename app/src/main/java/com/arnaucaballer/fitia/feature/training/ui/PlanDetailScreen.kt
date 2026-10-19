package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.arnaucaballer.fitia.feature.training.domain.Workout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: Long,
    onBack: () -> Unit,
    onOpenWorkout: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: PlanDetailViewModel = viewModel(
        key = "plan-$planId",
        factory = fitiaViewModelFactory(container) { c ->
            PlanDetailViewModel(
                planId = planId,
                getPlanUseCase = c.getPlanUseCase,
                observeWorkoutsUseCase = c.observeWorkoutsUseCase,
                savePlanUseCase = c.savePlanUseCase,
            )
        },
    )
    val plan by viewModel.plan.collectAsStateWithLifecycle()
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    var showPicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(plan?.name ?: stringResource(R.string.training_plan_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Button(onClick = { showPicker = true }) {
                    Text(stringResource(R.string.training_add_workout_to_plan))
                }
            }
            val planWorkouts = plan?.workouts.orEmpty()
            if (planWorkouts.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.training_plan_no_workouts),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(planWorkouts, key = { it.id }) { workout ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWorkout(workout.id) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(workout.name, style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { viewModel.removeWorkout(workout.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                        )
                    }
                }
            }
        }
    }

    if (showPicker) {
        WorkoutPickerDialog(
            workouts = workouts,
            onDismiss = { showPicker = false },
            onPick = { workout ->
                viewModel.addWorkout(workout)
                showPicker = false
            },
        )
    }
}

@Composable
private fun WorkoutPickerDialog(
    workouts: List<Workout>,
    onDismiss: () -> Unit,
    onPick: (Workout) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.training_pick_workout)) },
        text = {
            if (workouts.isEmpty()) {
                Text(stringResource(R.string.training_empty_workouts))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(workouts, key = { it.id }) { workout ->
                        Text(
                            text = workout.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(workout) },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
