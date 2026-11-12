package app.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.fitia.R
import app.fitia.design.FitiaPrimaryButton
import app.fitia.di.fitiaContainer
import app.fitia.di.fitiaViewModelFactory
import app.fitia.design.fitiaPress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    onBack: (() -> Unit)? = null,
    onOpenWorkout: (Long) -> Unit,
    onOpenSession: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: WorkoutsViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            WorkoutsViewModel(
                observeGenericWorkoutsUseCase = c.observeGenericWorkoutsUseCase,
                saveWorkoutUseCase = c.saveWorkoutUseCase,
                deleteWorkoutUseCase = c.deleteWorkoutUseCase,
                startSessionFromWorkoutUseCase = c.startSessionFromWorkoutUseCase,
            )
        },
    )
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    var showCreate by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sessionStarted.collect(onOpenSession)
    }
    LaunchedEffect(Unit) {
        viewModel.created.collect(onOpenWorkout)
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text(stringResource(R.string.training_workouts_title)) },
                    navigationIcon = {
                        IconButton(modifier = Modifier.fitiaPress(), onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "create") {
                FitiaPrimaryButton(
                    text = stringResource(R.string.training_new_workout),
                    onClick = { showCreate = true },
                )
            }
            if (workouts.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.training_empty_workouts),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(workouts, key = { it.id }) { workout ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWorkout(workout.id) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(workout.name, style = MaterialTheme.typography.titleMedium)
                        if (workout.notes.isNotBlank()) {
                            Text(
                                text = workout.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(modifier = Modifier.fitiaPress(), onClick = { viewModel.startSession(workout.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = stringResource(R.string.training_start_session),
                        )
                    }
                    IconButton(modifier = Modifier.fitiaPress(), onClick = { viewModel.deleteWorkout(workout.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                        )
                    }
                }
            }
        }
    }

    if (showCreate) {
        NameInputDialog(
            title = stringResource(R.string.training_create_workout),
            label = stringResource(R.string.field_name),
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                viewModel.createWorkout(name)
                showCreate = false
            },
        )
    }
}
