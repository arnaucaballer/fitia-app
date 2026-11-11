package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import com.arnaucaballer.fitia.feature.training.domain.SessionExercise
import com.arnaucaballer.fitia.feature.training.domain.SessionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    sessionId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: SessionViewModel = viewModel(
        key = "session-$sessionId",
        factory = fitiaViewModelFactory(container) { c ->
            SessionViewModel(
                sessionId = sessionId,
                getSessionUseCase = c.getSessionUseCase,
                logSessionSetUseCase = c.logSessionSetUseCase,
                deleteSessionSetUseCase = c.deleteSessionSetUseCase,
                finishSessionUseCase = c.finishSessionUseCase,
            )
        },
    )
    val session by viewModel.session.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.finished.collect { onBack() }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(session?.name ?: stringResource(R.string.training_session_title)) },
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
        val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
        val listBottom = with(LocalDensity.current) {
            if (imeBottom > 0) 80.dp else 24.dp
        }
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
            if (session?.status == SessionStatus.InProgress) {
                item(key = "autosave-hint") {
                    Text(
                        text = stringResource(R.string.training_sets_autosave_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(session?.exercises.orEmpty(), key = { it.id }) { exercise ->
                SessionExerciseBlock(
                    exercise = exercise,
                    editable = session?.status == SessionStatus.InProgress,
                    onSaveSet = { setId, setIndex, weight, reps, completed ->
                        viewModel.saveSet(
                            sessionExerciseId = exercise.id,
                            setId = setId,
                            setIndex = setIndex,
                            weightKg = weight,
                            reps = reps,
                            isCompleted = completed,
                        )
                    },
                    onAddSet = { viewModel.addSet(exercise.id) },
                    onRemoveSet = { setId -> viewModel.removeSet(setId) },
                )
            }

            if (session?.status == SessionStatus.InProgress) {
                item {
                    FitiaPrimaryButton(
                        text = stringResource(R.string.training_finish_session),
                        onClick = viewModel::finish,
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionExerciseBlock(
    exercise: SessionExercise,
    editable: Boolean,
    onSaveSet: (
        setId: Long,
        setIndex: Int,
        weightKg: Double?,
        reps: Int?,
        isCompleted: Boolean,
    ) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (setId: Long) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
        if (exercise.sets.isEmpty() && editable) {
            Text(
                text = stringResource(R.string.training_no_sets),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        exercise.sets.forEach { set ->
            var weightText by rememberSaveable(set.id) {
                mutableStateOf(set.weightKg?.toString().orEmpty())
            }
            var repsText by rememberSaveable(set.id) {
                mutableStateOf(set.reps?.toString().orEmpty())
            }
            var completed by rememberSaveable(set.id) {
                mutableStateOf(set.isCompleted)
            }

            val save = {
                onSaveSet(
                    set.id,
                    set.setIndex,
                    weightText.replace(',', '.').toDoubleOrNull(),
                    repsText.toIntOrNull(),
                    completed,
                )
            }

            SetAutosaveEffect(
                setKey = set.id,
                weightText = weightText,
                repsText = repsText,
                onSave = { weight, reps ->
                    onSaveSet(set.id, set.setIndex, weight, reps, completed)
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.training_set_label, set.setIndex + 1),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Checkbox(
                        checked = completed,
                        enabled = editable,
                        onCheckedChange = { checked ->
                            completed = checked
                            onSaveSet(
                                set.id,
                                set.setIndex,
                                weightText.replace(',', '.').toDoubleOrNull(),
                                repsText.toIntOrNull(),
                                checked,
                            )
                        },
                    )
                    if (editable) {
                        IconButton(onClick = { onRemoveSet(set.id) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.training_remove_set),
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        enabled = editable,
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
                        enabled = editable,
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
        if (editable) {
            TextButton(onClick = onAddSet) {
                Text(stringResource(R.string.training_add_set))
            }
        }
    }
}
