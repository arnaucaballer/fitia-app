package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaCard
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.design.FitiaSecondaryButton
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.Workout
import com.arnaucaballer.fitia.feature.training.domain.WorkoutExercise
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateTrainingPlanScreen(
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    var goal by rememberSaveable { mutableStateOf("Hipertrofia") }
    var days by rememberSaveable { mutableStateOf("4") }
    var level by rememberSaveable { mutableStateOf("Intermedio") }
    var userDescription by rememberSaveable { mutableStateOf("") }
    var draft by remember { mutableStateOf<TrainingPlan?>(null) }
    var previewName by rememberSaveable { mutableStateOf("") }
    var previewNotes by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun syncPreviewFields(plan: TrainingPlan) {
        draft = plan
        previewName = plan.name
        previewNotes = plan.notes
    }

    fun loadDraft() {
        loading = true
        error = null
        scope.launch {
            runCatching {
                container.trainingPlanGenerator.generateDraft(
                    goal = goal,
                    daysPerWeek = days.toIntOrNull()?.coerceIn(1, 7) ?: 4,
                    level = level,
                    userDescription = userDescription,
                )
            }.onSuccess { plan ->
                loading = false
                syncPreviewFields(plan)
            }.onFailure { t ->
                loading = false
                error = t.message ?: "Error"
            }
        }
    }

    val showingPreview = draft != null

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (showingPreview) {
                                R.string.training_plan_preview_title
                            } else {
                                R.string.training_generate_plan_ai
                            },
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showingPreview) {
                                draft = null
                                error = null
                            } else {
                                onBack()
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
            if (showingPreview) {
                TrainingPlanPreviewStep(
                    previewName = previewName,
                    onNameChange = { previewName = it },
                    previewNotes = previewNotes,
                    onNotesChange = { previewNotes = it },
                    plan = draft!!,
                    loading = loading,
                    error = error,
                    onEditForm = {
                        draft = null
                        error = null
                    },
                    onRegenerate = { loadDraft() },
                    onConfirm = {
                        loading = true
                        error = null
                        val toSave = draft!!.copy(
                            name = previewName.trim().ifBlank { draft!!.name },
                            notes = previewNotes.trim(),
                        )
                        scope.launch {
                            runCatching {
                                container.trainingPlanGenerator.persistGeneratedPlan(toSave)
                            }.onSuccess { id ->
                                loading = false
                                onCreated(id)
                            }.onFailure { t ->
                                loading = false
                                error = t.message ?: "Error"
                            }
                        }
                    },
                )
            } else {
                TrainingPlanFormStep(
                    goal = goal,
                    onGoalChange = { goal = it },
                    days = days,
                    onDaysChange = { days = it.filter { c -> c.isDigit() }.take(1) },
                    level = level,
                    onLevelChange = { level = it },
                    userDescription = userDescription,
                    onDescriptionChange = { userDescription = it },
                    loading = loading,
                    error = error,
                    onPreview = { loadDraft() },
                )
            }
        }
    }
}

@Composable
private fun TrainingPlanFormStep(
    goal: String,
    onGoalChange: (String) -> Unit,
    days: String,
    onDaysChange: (String) -> Unit,
    level: String,
    onLevelChange: (String) -> Unit,
    userDescription: String,
    onDescriptionChange: (String) -> Unit,
    loading: Boolean,
    error: String?,
    onPreview: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.training_generate_plan_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
        value = goal,
        onValueChange = onGoalChange,
        label = { Text(stringResource(R.string.training_goal_label)) },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = days,
        onValueChange = onDaysChange,
        label = { Text(stringResource(R.string.training_days_label)) },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = level,
        onValueChange = onLevelChange,
        label = { Text(stringResource(R.string.training_level_label)) },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = userDescription,
        onValueChange = onDescriptionChange,
        label = { Text(stringResource(R.string.training_plan_description_label)) },
        placeholder = { Text(stringResource(R.string.training_plan_description_hint)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (loading) {
            CircularProgressIndicator()
        } else {
            FitiaPrimaryButton(
                text = stringResource(R.string.training_generate_preview),
                onClick = onPreview,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TrainingPlanPreviewStep(
    previewName: String,
    onNameChange: (String) -> Unit,
    previewNotes: String,
    onNotesChange: (String) -> Unit,
    plan: TrainingPlan,
    loading: Boolean,
    error: String?,
    onEditForm: () -> Unit,
    onRegenerate: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.training_plan_preview_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
        value = previewName,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.training_plan_field_name)) },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = previewNotes,
        onValueChange = onNotesChange,
        label = { Text(stringResource(R.string.training_plan_field_description)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        Text(
            text = stringResource(R.string.training_plan_preview_workouts),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
        )
        plan.workouts.forEach { workout ->
            TrainingPlanPreviewWorkoutCard(workout = workout)
        }
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (!loading) {
            FitiaSecondaryButton(
                text = stringResource(R.string.training_edit_form),
                onClick = onEditForm,
                modifier = Modifier.fillMaxWidth(),
            )
            FitiaSecondaryButton(
                text = stringResource(R.string.training_regenerate_plan),
                onClick = onRegenerate,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (loading) {
            CircularProgressIndicator()
        } else {
            FitiaPrimaryButton(
                text = stringResource(R.string.training_save_plan),
                onClick = onConfirm,
                enabled = previewName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TrainingPlanPreviewWorkoutCard(workout: Workout) {
    FitiaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(workout.name, style = MaterialTheme.typography.titleSmall)
            if (workout.notes.isNotBlank()) {
                Text(
                    workout.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            workout.exercises.forEach { exercise ->
                Text(
                    text = stringResource(
                        R.string.training_plan_preview_exercise_line,
                        exercise.exerciseName,
                        previewSetsLabel(exercise),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun previewSetsLabel(exercise: WorkoutExercise): String {
    val count = exercise.setTargets.size
    if (count == 0) return stringResource(R.string.training_no_sets_short)
    val reps = exercise.setTargets.firstOrNull()?.targetReps
    return when {
        count == 1 && reps != null -> stringResource(R.string.training_preview_one_set_reps, reps)
        count > 1 && reps != null -> stringResource(R.string.training_preview_many_sets_reps, count, reps)
        count == 1 -> stringResource(R.string.training_sets_summary_one)
        else -> stringResource(R.string.training_sets_summary_many, count)
    }
}
