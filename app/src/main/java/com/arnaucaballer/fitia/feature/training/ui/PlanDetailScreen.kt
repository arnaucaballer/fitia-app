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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.design.FitiaSecondaryButton
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
                observeGenericWorkoutsUseCase = c.observeGenericWorkoutsUseCase,
                savePlanUseCase = c.savePlanUseCase,
                trainingRepository = c.trainingRepository,
            )
        },
    )
    val plan by viewModel.plan.collectAsStateWithLifecycle()
    val generics by viewModel.genericWorkouts.collectAsStateWithLifecycle()
    var showCreateOwned by rememberSaveable { mutableStateOf(false) }
    var showLinkGeneric by rememberSaveable { mutableStateOf(false) }
    var personalizeCandidate by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.createdWorkout.collect(onOpenWorkout)
    }

    val availableGenerics = generics.filter { generic ->
        plan?.workouts?.none { it.id == generic.id } != false
    }
    val personalizeWorkout = generics.firstOrNull { it.id == personalizeCandidate }

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
                Text(
                    text = stringResource(R.string.training_plan_model_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val planNotes = plan?.notes.orEmpty()
            if (planNotes.isNotBlank()) {
                item {
                    Text(
                        text = planNotes,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            item {
                FitiaPrimaryButton(
                    text = stringResource(R.string.training_create_plan_workout),
                    onClick = { showCreateOwned = true },
                )
            }
            item {
                FitiaSecondaryButton(
                    text = stringResource(R.string.training_link_generic_workout),
                    onClick = { showLinkGeneric = true },
                    modifier = Modifier.fillMaxWidth(),
                )
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
                PlanWorkoutRow(
                    workout = workout,
                    onOpen = { onOpenWorkout(workout.id) },
                    onRemove = { viewModel.removeWorkout(workout.id) },
                )
            }
        }
    }

    if (showCreateOwned) {
        NameInputDialog(
            title = stringResource(R.string.training_create_plan_workout),
            label = stringResource(R.string.field_name),
            onDismiss = { showCreateOwned = false },
            onConfirm = { name ->
                showCreateOwned = false
                viewModel.createPlanOwnedWorkout(name)
            },
        )
    }

    if (showLinkGeneric) {
        GenericWorkoutPickerDialog(
            workouts = availableGenerics,
            onDismiss = { showLinkGeneric = false },
            onLink = { workout ->
                viewModel.linkGenericWorkout(workout)
                showLinkGeneric = false
            },
            onPersonalize = { workout ->
                showLinkGeneric = false
                personalizeCandidate = workout.id
            },
        )
    }

    if (personalizeWorkout != null) {
        AlertDialog(
            onDismissRequest = { personalizeCandidate = null },
            title = { Text(stringResource(R.string.training_personalize_workout)) },
            text = {
                Text(
                    stringResource(
                        R.string.training_personalize_workout_body,
                        personalizeWorkout.name,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.personalizeGeneric(personalizeWorkout)
                        personalizeCandidate = null
                    },
                ) {
                    Text(stringResource(R.string.training_personalize_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { personalizeCandidate = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun PlanWorkoutRow(
    workout: Workout,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(workout.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (workout.isGeneric) {
                    stringResource(R.string.training_workout_type_generic)
                } else {
                    stringResource(R.string.training_workout_type_plan_only)
                },
                color = if (workout.isGeneric) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.action_delete),
            )
        }
    }
}

@Composable
private fun GenericWorkoutPickerDialog(
    workouts: List<Workout>,
    onDismiss: () -> Unit,
    onLink: (Workout) -> Unit,
    onPersonalize: (Workout) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.training_link_generic_workout)) },
        text = {
            if (workouts.isEmpty()) {
                Text(stringResource(R.string.training_empty_generics_for_plan))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(workouts, key = { it.id }) { workout ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            Text(workout.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stringResource(R.string.training_workout_type_generic),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 6.dp),
                            ) {
                                TextButton(onClick = { onLink(workout) }) {
                                    Text(stringResource(R.string.training_link_as_generic))
                                }
                                TextButton(onClick = { onPersonalize(workout) }) {
                                    Text(stringResource(R.string.training_personalize_confirm))
                                }
                            }
                        }
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
