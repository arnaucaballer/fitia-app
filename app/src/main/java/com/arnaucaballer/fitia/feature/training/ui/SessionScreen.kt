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
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
                )
            }

            if (session?.status == SessionStatus.InProgress) {
                item {
                    Button(
                        onClick = viewModel::finish,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.training_finish_session))
                    }
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
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
        exercise.sets.forEach { set ->
            var weightText by rememberSaveable(set.id, set.weightKg) {
                mutableStateOf(set.weightKg?.toString().orEmpty())
            }
            var repsText by rememberSaveable(set.id, set.reps) {
                mutableStateOf(set.reps?.toString().orEmpty())
            }
            var completed by rememberSaveable(set.id, set.isCompleted) {
                mutableStateOf(set.isCompleted)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.training_set_label, set.setIndex + 1),
                    modifier = Modifier.weight(0.2f),
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    enabled = editable,
                    label = { Text(stringResource(R.string.field_weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(0.3f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it },
                    enabled = editable,
                    label = { Text(stringResource(R.string.field_reps)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.3f),
                    singleLine = true,
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
            }
            if (editable) {
                TextButton(
                    onClick = {
                        onSaveSet(
                            set.id,
                            set.setIndex,
                            weightText.replace(',', '.').toDoubleOrNull(),
                            repsText.toIntOrNull(),
                            completed,
                        )
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}
