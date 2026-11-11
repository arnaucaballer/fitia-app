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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaCardShape
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import com.arnaucaballer.fitia.feature.training.domain.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: ExercisesViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            ExercisesViewModel(
                observeExercisesUseCase = c.observeExercisesUseCase,
                observeMusclesUseCase = c.observeMusclesUseCase,
                saveExerciseUseCase = c.saveExerciseUseCase,
                saveMuscleUseCase = c.saveMuscleUseCase,
                deleteExerciseUseCase = c.deleteExerciseUseCase,
            )
        },
    )
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val muscles by viewModel.muscles.collectAsStateWithLifecycle()
    var showExerciseForm by rememberSaveable { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }
    val unassignedLabel = stringResource(R.string.training_exercises_unassigned)

    val sections = remember(exercises, unassignedLabel) {
        exercises
            .groupBy { exercise ->
                exercise.muscleName.ifBlank { unassignedLabel }
            }
            .entries
            .sortedWith(
                compareBy<Map.Entry<String, List<Exercise>>> {
                    if (it.key == unassignedLabel) 1 else 0
                }.thenBy { it.key.lowercase() },
            )
            .map { (muscleName, items) ->
                muscleName to items.sortedBy { it.name.lowercase() }
            }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.training_exercises_title)) },
                navigationIcon = {
                    if (onBack == null) return@TopAppBar
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingExercise = null
                showExerciseForm = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "hint") {
                Text(
                    text = stringResource(R.string.training_exercises_by_muscle_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item(key = "actions") {
                FitiaPrimaryButton(
                    text = stringResource(R.string.training_create_exercise),
                    onClick = {
                        editingExercise = null
                        showExerciseForm = true
                    },
                )
            }
            if (sections.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.training_empty_exercises),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(sections, key = { it.first }) { (muscleName, muscleExercises) ->
                MuscleExerciseSection(
                    muscleName = muscleName,
                    exercises = muscleExercises,
                    onEdit = { exercise ->
                        editingExercise = exercise
                        showExerciseForm = true
                    },
                    onDelete = { viewModel.deleteExercise(it) },
                )
            }
        }
    }

    if (showExerciseForm) {
        ExerciseFormDialog(
            title = stringResource(
                if (editingExercise == null) {
                    R.string.training_create_exercise
                } else {
                    R.string.training_edit_exercise
                },
            ),
            muscles = muscles,
            initialName = editingExercise?.name.orEmpty(),
            initialMuscleId = editingExercise?.muscleId,
            onDismiss = {
                showExerciseForm = false
                editingExercise = null
            },
            onConfirm = { name, muscleId ->
                viewModel.saveExercise(
                    name = name,
                    muscleId = muscleId,
                    exerciseId = editingExercise?.id ?: 0,
                )
                showExerciseForm = false
                editingExercise = null
            },
            onEnsureMuscle = viewModel::createMuscle,
        )
    }
}

@Composable
private fun MuscleExerciseSection(
    muscleName: String,
    exercises: List<Exercise>,
    onEdit: (Exercise) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var expanded by rememberSaveable(muscleName) { mutableStateOf(true) }
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
                .heightIn(min = 56.dp)
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(muscleName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.training_muscle_exercise_count, exercises.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = motion),
            exit = shrinkVertically(animationSpec = motion),
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                exercises.forEach { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(FitiaCardShape)
                            .clickable { onEdit(exercise) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onDelete(exercise.id) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                            )
                        }
                    }
                }
            }
        }
    }
}
