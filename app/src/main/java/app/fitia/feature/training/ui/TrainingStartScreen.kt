package app.fitia.feature.training.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import app.fitia.R
import app.fitia.design.FitiaCard
import app.fitia.design.FitiaPrimaryButton
import app.fitia.design.FitiaSecondaryButton
import app.fitia.di.fitiaContainer
import app.fitia.di.fitiaViewModelFactory
import java.time.LocalDate
import app.fitia.design.fullDateFormatter

@Composable
fun TrainingStartScreen(
    onOpenSession: (Long) -> Unit,
    onOpenWeeklyCalendar: () -> Unit,
    onPickPlanToFollow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: StartTrainingViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            StartTrainingViewModel(
                observeActiveTrainingPlanUseCase = c.observeActiveTrainingPlanUseCase,
                observeWorkoutSchedulesUseCase = c.observeWorkoutSchedulesUseCase,
                observeWorkoutsUseCase = c.observeWorkoutsUseCase,
                observeActiveSessionUseCase = c.observeActiveSessionUseCase,
                getPlanUseCase = c.getPlanUseCase,
                startSessionFromWorkoutUseCase = c.startSessionFromWorkoutUseCase,
                startBlankSessionUseCase = c.startBlankSessionUseCase,
                setActiveTrainingPlanUseCase = c.setActiveTrainingPlanUseCase,
            )
        },
    )
    val activePlan by viewModel.activePlan.collectAsStateWithLifecycle()
    val todaySchedule by viewModel.todaySchedule.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()

    var showTrainChoice by rememberSaveable { mutableStateOf(false) }
    var showBlank by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sessionStarted.collect(onOpenSession)
    }
    LaunchedEffect(Unit) {
        viewModel.needsWorkoutChoice.collect { showTrainChoice = true }
    }

    val dateLabel = fullDateFormatter.format(LocalDate.now())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(dateLabel, style = MaterialTheme.typography.titleLarge)
        }

        item {
            FitiaCard {
                Text(
                    text = stringResource(R.string.training_start_today_workout),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = todaySchedule?.workoutName
                        ?: stringResource(R.string.training_start_no_workout_today),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
                activeSession?.let { session ->
                    Text(
                        text = stringResource(R.string.training_train_active_hint, session.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                FitiaPrimaryButton(
                    text = stringResource(R.string.training_start_train),
                    onClick = viewModel::trainToday,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        item {
            FitiaCard {
                Text(
                    text = stringResource(R.string.training_follow_plan_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (activePlan != null) {
                    Text(
                        text = stringResource(R.string.training_follow_plan_active, activePlan!!.name),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    FitiaSecondaryButton(
                        text = stringResource(R.string.training_weekly_open),
                        onClick = onOpenWeeklyCalendar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    )
                    FitiaSecondaryButton(
                        text = stringResource(R.string.training_unfollow_plan),
                        onClick = viewModel::stopFollowingPlan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.training_follow_plan_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    FitiaSecondaryButton(
                        text = stringResource(R.string.training_follow_plan_choose),
                        onClick = onPickPlanToFollow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    )
                }
            }
        }
    }

    if (showTrainChoice) {
        WorkoutTrainChoiceDialog(
            workouts = workouts,
            onDismiss = { showTrainChoice = false },
            onStartWorkout = { workout ->
                showTrainChoice = false
                viewModel.startFromWorkout(workout.id)
            },
            onStartBlank = {
                showTrainChoice = false
                showBlank = true
            },
        )
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
