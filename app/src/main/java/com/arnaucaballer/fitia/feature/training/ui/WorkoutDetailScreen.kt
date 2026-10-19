package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import com.arnaucaballer.fitia.feature.training.domain.WorkoutExercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onBack: () -> Unit,
    onOpenSession: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: WorkoutDetailViewModel = viewModel(
        key = "workout-$workoutId",
        factory = fitiaViewModelFactory(container) { c ->
            WorkoutDetailViewModel(
                workoutId = workoutId,
                getWorkoutUseCase = c.getWorkoutUseCase,
                observeExercisesUseCase = c.observeExercisesUseCase,
                saveWorkoutUseCase = c.saveWorkoutUseCase,
                startSessionFromWorkoutUseCase = c.startSessionFromWorkoutUseCase,
            )
        },
    )
    val workout by viewModel.workout.collectAsStateWithLifecycle()
    val library by viewModel.exercises.collectAsStateWithLifecycle()
    var showPicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sessionStarted.collect(onOpenSession)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(workout?.name ?: stringResource(R.string.training_workout_detail)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::startSession) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = stringResource(R.string.training_start_session),
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Button(onClick = { showPicker = true }) {
                    Text(stringResource(R.string.training_add_exercise))
                }
            }

            val exercises = workout?.exercises.orEmpty()
            if (exercises.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.training_workout_no_exercises),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(exercises, key = { it.sortOrder }) { exercise ->
                WorkoutExerciseEditor(
                    exercise = exercise,
                    onAddSet = { viewModel.addSet(exercise.sortOrder) },
                    onRemove = { viewModel.removeExercise(exercise.sortOrder) },
                    onUpdateSet = { setIndex, weight, reps ->
                        viewModel.updateSet(exercise.sortOrder, setIndex, weight, reps)
                    },
                )
            }
        }
    }

    if (showPicker) {
        ExercisePickerDialog(
            exercises = library,
            onDismiss = { showPicker = false },
            onPick = { exercise ->
                viewModel.addExercise(exercise)
                showPicker = false
            },
        )
    }
}

@Composable
private fun WorkoutExerciseEditor(
    exercise: WorkoutExercise,
    onAddSet: () -> Unit,
    onRemove: () -> Unit,
    onUpdateSet: (setIndex: Int, weightKg: Double?, reps: Int?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = exercise.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                )
            }
        }
        exercise.setTargets.forEach { target ->
            SetTargetRow(
                setNumber = target.setIndex + 1,
                weightKg = target.targetWeightKg,
                reps = target.targetReps,
                onCommit = { weight, reps ->
                    onUpdateSet(target.setIndex, weight, reps)
                },
            )
        }
        TextButton(onClick = onAddSet) {
            Text(stringResource(R.string.training_add_set))
        }
    }
}

@Composable
private fun SetTargetRow(
    setNumber: Int,
    weightKg: Double?,
    reps: Int?,
    onCommit: (weightKg: Double?, reps: Int?) -> Unit,
) {
    var weightText by rememberSaveable(setNumber, weightKg) {
        mutableStateOf(weightKg?.toString().orEmpty())
    }
    var repsText by rememberSaveable(setNumber, reps) {
        mutableStateOf(reps?.toString().orEmpty())
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.training_set_label, setNumber),
            modifier = Modifier.weight(0.25f),
        )
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text(stringResource(R.string.field_weight_kg)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(0.35f),
            singleLine = true,
        )
        OutlinedTextField(
            value = repsText,
            onValueChange = { repsText = it },
            label = { Text(stringResource(R.string.field_reps)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.35f),
            singleLine = true,
        )
        TextButton(
            onClick = {
                onCommit(
                    weightText.replace(',', '.').toDoubleOrNull(),
                    repsText.toIntOrNull(),
                )
            },
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}
