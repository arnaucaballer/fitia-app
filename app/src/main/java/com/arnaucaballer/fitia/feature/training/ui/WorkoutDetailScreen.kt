package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.flowOf
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaCard
import com.arnaucaballer.fitia.design.FitiaCardShape
import com.arnaucaballer.fitia.design.FitiaDeleteDialog
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.feature.training.domain.SetTarget
import kotlin.math.roundToInt
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import com.arnaucaballer.fitia.feature.training.domain.WorkoutExercise
import com.arnaucaballer.fitia.design.formatDateTime

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
                getLatestSessionForWorkoutUseCase = c.getLatestSessionForWorkoutUseCase,
                observeExercisesUseCase = c.observeExercisesUseCase,
                observeSessionsUseCase = c.observeSessionsUseCase,
                observeMusclesUseCase = c.observeMusclesUseCase,
                saveExerciseUseCase = c.saveExerciseUseCase,
                saveMuscleUseCase = c.saveMuscleUseCase,
                saveWorkoutUseCase = c.saveWorkoutUseCase,
                updateWorkoutSetTargetUseCase = c.updateWorkoutSetTargetUseCase,
                deleteWorkoutUseCase = c.deleteWorkoutUseCase,
                startSessionFromWorkoutUseCase = c.startSessionFromWorkoutUseCase,
            )
        },
    )
    val workout by viewModel.workout.collectAsStateWithLifecycle()
    val lastSessionAtEpochMs by viewModel.lastSessionAtEpochMs.collectAsStateWithLifecycle()
    val previousSessions by viewModel.previousSessions.collectAsStateWithLifecycle()
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var showCreateExercise by rememberSaveable { mutableStateOf(false) }
    var newExerciseName by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    val showLibrary = showPicker || showCreateExercise
    val library by remember(showLibrary) {
        if (showLibrary) viewModel.exercises else flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())
    val muscles by remember(showCreateExercise) {
        if (showCreateExercise) viewModel.muscles else flowOf(emptyList())
    }.collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(Unit) {
        viewModel.sessionStarted.collect(onOpenSession)
    }
    LaunchedEffect(Unit) {
        viewModel.deleted.collect { onBack() }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.training_delete_workout),
                        )
                    }
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
        val density = LocalDensity.current
        val ime = WindowInsets.ime
        val keyboardOpen by remember(ime, density) {
            derivedStateOf { ime.getBottom(density) > 0 }
        }
        val listBottom = if (keyboardOpen) 80.dp else 24.dp
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 8.dp,
                bottom = listBottom,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                val current = workout
                if (current != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (current.isGeneric) {
                                stringResource(R.string.training_workout_type_generic)
                            } else {
                                stringResource(R.string.training_workout_type_plan_only)
                            },
                            color = if (current.isGeneric) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                        val last = lastSessionAtEpochMs
                        if (last != null) {
                            Text(
                                text = stringResource(
                                    R.string.training_last_session_values,
                                    formatDateTime(last),
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            item {
                FitiaPrimaryButton(
                    text = stringResource(R.string.training_add_exercise),
                    onClick = { showPicker = true },
                )
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
                val sortOrder = exercise.sortOrder
                WorkoutExerciseEditor(
                    exercise = exercise,
                    onAddSet = remember(sortOrder) { { viewModel.addSet(sortOrder) } },
                    onRemoveSet = remember(sortOrder) { { setIndex -> viewModel.removeSet(sortOrder, setIndex) } },
                    onRemove = remember(sortOrder) { { viewModel.removeExercise(sortOrder) } },
                    onUpdateSet = remember(sortOrder) { { setIndex, weight, reps ->
                        viewModel.updateSet(sortOrder, setIndex, weight, reps)
                    } },
                )
            }

            if (previousSessions.isNotEmpty()) {
                item(key = "previous-header") {
                    Text(
                        text = stringResource(R.string.training_previous_sessions),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(previousSessions, key = { "prev-${it.id}" }) { session ->
                    FitiaCard(onClick = { onOpenSession(session.id) }) {
                        Text(session.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = formatDateTime(session.startedAtEpochMs),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
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
            onCreateNew = { prefillName ->
                newExerciseName = prefillName
                showPicker = false
                showCreateExercise = true
            },
        )
    }

    if (showCreateExercise) {
        ExerciseFormDialog(
            title = stringResource(R.string.training_create_exercise),
            muscles = muscles,
            initialName = newExerciseName,
            onDismiss = {
                showCreateExercise = false
                newExerciseName = ""
            },
            onConfirm = { name, muscleIds ->
                viewModel.addNewExercise(name, muscleIds)
                showCreateExercise = false
                newExerciseName = ""
            },
            onEnsureMuscle = viewModel::createMuscle,
        )
    }

    if (showDeleteConfirm) {
        FitiaDeleteDialog(
            title = stringResource(R.string.training_delete_workout),
            message = stringResource(R.string.training_delete_workout_confirm),
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteWorkout()
            },
        )
    }
}

@Composable
private fun WorkoutExerciseEditor(
    exercise: WorkoutExercise,
    onAddSet: () -> Unit,
    onRemoveSet: (setIndex: Int) -> Unit,
    onRemove: () -> Unit,
    onUpdateSet: (setIndex: Int, weightKg: Double?, reps: Int?) -> Unit,
) {
    var expanded by rememberSaveable(exercise.id, exercise.sortOrder) { mutableStateOf(false) }
    val motion = spring<IntSize>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FitiaCardShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable { expanded = !expanded }
                .padding(start = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(
                    if (expanded) R.string.training_collapse_sets else R.string.training_expand_sets,
                ),
                modifier = Modifier.padding(8.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
            ) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = setSummary(exercise.setTargets),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = motion),
            exit = shrinkVertically(animationSpec = motion),
        ) {
            Column(
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
            ) {
                if (exercise.setTargets.isEmpty()) {
                    Text(
                        text = stringResource(R.string.training_no_sets),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                exercise.setTargets.forEach { target ->
                    val setIndex = target.setIndex
                    SetTargetRow(
                        setNumber = setIndex + 1,
                        weightKg = target.targetWeightKg,
                        reps = target.targetReps,
                        onCommit = remember(setIndex) { { weight, reps -> onUpdateSet(setIndex, weight, reps) } },
                        onRemove = remember(setIndex) { { onRemoveSet(setIndex) } },
                    )
                }
                TextButton(onClick = onAddSet) {
                    Text(stringResource(R.string.training_add_set))
                }
            }
        }
    }
}

@Composable
private fun setSummary(targets: List<SetTarget>): String {
    if (targets.isEmpty()) return stringResource(R.string.training_no_sets_short)
    val countLabel = if (targets.size == 1) {
        stringResource(R.string.training_sets_summary_one)
    } else {
        stringResource(R.string.training_sets_summary_many, targets.size)
    }
    val first = targets.first()
    val weight = first.targetWeightKg
    val reps = first.targetReps
    val uniform = targets.all {
        it.targetWeightKg == weight && it.targetReps == reps
    }
    return if (uniform && weight != null && reps != null) {
        stringResource(R.string.training_sets_summary_detail, countLabel, formatKg(weight), reps)
    } else {
        countLabel
    }
}

private fun formatKg(value: Double): String {
    val rounded = (value * 10.0).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

@Composable
private fun SetTargetRow(
    setNumber: Int,
    weightKg: Double?,
    reps: Int?,
    onCommit: (weightKg: Double?, reps: Int?) -> Unit,
    onRemove: () -> Unit,
) {
    var weightText by rememberSaveable(setNumber) {
        mutableStateOf(weightKg?.toString().orEmpty())
    }
    var repsText by rememberSaveable(setNumber) {
        mutableStateOf(reps?.toString().orEmpty())
    }

    val save = {
        onCommit(
            weightText.replace(',', '.').toDoubleOrNull(),
            repsText.toIntOrNull(),
        )
    }

    SetAutosaveEffect(
        setKey = setNumber,
        weightText = weightText,
        repsText = repsText,
        onSave = { weight, reps -> onCommit(weight, reps) },
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.training_set_label, setNumber),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.training_remove_set),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text(stringResource(R.string.field_weight_kg)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 0.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = repsText,
                onValueChange = { repsText = it },
                label = { Text(stringResource(R.string.field_reps)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { save() }),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 0.dp),
                singleLine = true,
            )
        }
    }
}
