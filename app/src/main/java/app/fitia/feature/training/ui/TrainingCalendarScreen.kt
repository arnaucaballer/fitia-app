package app.fitia.feature.training.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.fitia.R
import app.fitia.design.FitiaCard
import app.fitia.design.FitiaDangerButton
import app.fitia.design.FitiaDeleteDialog
import app.fitia.design.FitiaPrimaryButton
import app.fitia.design.FitiaSecondaryButton
import app.fitia.di.fitiaContainer
import app.fitia.di.fitiaViewModelFactory
import app.fitia.feature.training.domain.SessionStatus
import app.fitia.feature.training.domain.TrainingSession
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import app.fitia.design.fullDateFormatter
import app.fitia.design.shortTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import app.fitia.design.fitiaPress

@Composable
fun TrainingCalendarScreen(
    onOpenSession: (Long) -> Unit,
    highlightWeeklySetup: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: TrainingCalendarViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            TrainingCalendarViewModel(
                observeSessionsUseCase = c.observeSessionsUseCase,
                observeWorkoutSchedulesUseCase = c.observeWorkoutSchedulesUseCase,
                observeWorkoutsUseCase = c.observeWorkoutsUseCase,
                observeActiveSessionUseCase = c.observeActiveSessionUseCase,
                observeActiveTrainingPlanUseCase = c.observeActiveTrainingPlanUseCase,
                observePlanWeeklyScheduleUseCase = c.observePlanWeeklyScheduleUseCase,
                getPlanUseCase = c.getPlanUseCase,
                setWorkoutScheduleUseCase = c.setWorkoutScheduleUseCase,
                clearWorkoutScheduleUseCase = c.clearWorkoutScheduleUseCase,
                clearCalendarUseCase = c.clearCalendarUseCase,
                setPlanWeeklySlotUseCase = c.setPlanWeeklySlotUseCase,
                setActiveTrainingPlanUseCase = c.setActiveTrainingPlanUseCase,
                startSessionFromWorkoutUseCase = c.startSessionFromWorkoutUseCase,
                startBlankSessionUseCase = c.startBlankSessionUseCase,
            )
        },
    )
    val activePlan by viewModel.activePlan.collectAsStateWithLifecycle()
    val weeklySlots by viewModel.weeklySlots.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()

    var showAssign by rememberSaveable { mutableStateOf(false) }
    var showTrainChoice by rememberSaveable { mutableStateOf(false) }
    var showBlank by rememberSaveable { mutableStateOf(false) }
    var showClearCalendar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sessionStarted.collect(onOpenSession)
    }
    LaunchedEffect(Unit) {
        viewModel.needsWorkoutChoice.collect { showTrainChoice = true }
    }

    val sessionsByDay = remember(sessions) {
        sessions.groupBy { it.localEpochDay() }
    }
    val schedulesByDay = remember(schedules) {
        schedules.associateBy { it.dayEpochDay }
    }
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now().toEpochDay()) }
    val today = LocalDate.now()
    val locale = Locale.getDefault()
    val monthLabel = month.month.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() } +
        " ${month.year}"
    val cells = remember(month) { buildTrainingMonthCells(month) }
    val daySessions = sessionsByDay[selectedDay].orEmpty()
        .sortedByDescending { it.startedAtEpochMs }
    val daySchedule = schedulesByDay[selectedDay]
    val timeFormat = shortTimeFormatter

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (highlightWeeklySetup && activePlan != null) {
            item(key = "weekly-highlight") {
                Text(
                    text = stringResource(R.string.training_weekly_welcome),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        activePlan?.let { plan ->
            item(key = "weekly-setup") {
                PlanWeeklySetupSection(
                    plan = plan,
                    weeklySlots = weeklySlots,
                    onAssign = viewModel::setWeeklySlot,
                    onUnfollowPlan = viewModel::stopFollowingPlan,
                )
            }
        }

        item(key = "month") {
            FitiaCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(modifier = Modifier.fitiaPress(), onClick = { month = month.minusMonths(1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.nutrition_prev_day),
                        )
                    }
                    Text(monthLabel, style = MaterialTheme.typography.titleLarge)
                    IconButton(modifier = Modifier.fitiaPress(), onClick = { month = month.plusMonths(1) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.nutrition_next_day),
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    DayOfWeek.entries.forEach { dow ->
                        Text(
                            text = dow.getDisplayName(TextStyle.NARROW, locale),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        week.forEach { date ->
                            val epochDay = date?.toEpochDay()
                            val hasSession = epochDay != null && epochDay in sessionsByDay
                            val hasSchedule = epochDay != null && epochDay in schedulesByDay
                            val selected = epochDay == selectedDay
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            date == null -> MaterialTheme.colorScheme.surfaceVariant
                                            hasSession -> MaterialTheme.colorScheme.primary
                                            hasSchedule -> MaterialTheme.colorScheme.tertiary
                                            date == today -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        },
                                    )
                                    .then(
                                        if (selected) {
                                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .then(
                                        if (date != null) {
                                            Modifier.clickable { selectedDay = date.toEpochDay() }
                                        } else {
                                            Modifier
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (date != null) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        color = when {
                                            hasSession -> MaterialTheme.colorScheme.onPrimary
                                            hasSchedule -> MaterialTheme.colorScheme.onTertiary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "clear-calendar") {
            FitiaDangerButton(
                text = stringResource(R.string.training_calendar_clear),
                onClick = { showClearCalendar = true },
            )
        }

        item(key = "day-title") {
            Text(
                text = fullDateFormatter.format(LocalDate.ofEpochDay(selectedDay)),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        item(key = "schedule") {
            FitiaCard {
                Text(
                    text = stringResource(R.string.training_schedule_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (daySchedule != null) {
                    Text(
                        text = daySchedule.workoutName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FitiaSecondaryButton(
                            text = stringResource(R.string.training_schedule_change),
                            onClick = { showAssign = true },
                            modifier = Modifier.weight(1f),
                        )
                        FitiaSecondaryButton(
                            text = stringResource(R.string.training_schedule_clear),
                            onClick = { viewModel.clearSchedule(selectedDay) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.training_schedule_empty_day),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    FitiaSecondaryButton(
                        text = stringResource(R.string.training_schedule_assign_day),
                        onClick = { showAssign = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    )
                }
            }
        }

        item(key = "train") {
            val active = activeSession
            if (active != null) {
                Text(
                    text = stringResource(R.string.training_train_active_hint, active.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            FitiaPrimaryButton(
                text = stringResource(R.string.training_train),
                onClick = {
                    viewModel.train(
                        dayEpochDay = selectedDay,
                        scheduledWorkoutId = daySchedule?.workoutId,
                    )
                },
            )
        }

        item(key = "sessions-title") {
            Text(
                text = stringResource(R.string.training_calendar_sessions),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (daySessions.isEmpty()) {
            item(key = "day-empty") {
                FitiaCard {
                    Text(
                        text = stringResource(R.string.training_calendar_empty_day),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(daySessions, key = { it.id }) { session ->
                FitiaCard(onClick = { onOpenSession(session.id) }) {
                    Text(session.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = timeFormat.format(
                            Instant.ofEpochMilli(session.startedAtEpochMs)
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime(),
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = if (session.status == SessionStatus.Completed) {
                            stringResource(R.string.training_status_completed)
                        } else {
                            stringResource(R.string.training_status_in_progress)
                        },
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }

    if (showClearCalendar) {
        FitiaDeleteDialog(
            title = stringResource(R.string.training_calendar_clear_title),
            message = stringResource(R.string.training_calendar_clear_body),
            onDismiss = { showClearCalendar = false },
            onConfirm = {
                showClearCalendar = false
                viewModel.clearCalendar()
            },
        )
    }

    if (showAssign) {
        WorkoutDayAssignDialog(
            workouts = workouts,
            onDismiss = { showAssign = false },
            onPick = { workout ->
                showAssign = false
                viewModel.assignWorkout(selectedDay, workout.id)
            },
        )
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

private fun TrainingSession.localEpochDay(): Long =
    Instant.ofEpochMilli(startedAtEpochMs)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toEpochDay()

private fun buildTrainingMonthCells(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val lead = first.dayOfWeek.value - 1
    val cells = MutableList<LocalDate?>(lead) { null }
    for (day in 1..month.lengthOfMonth()) {
        cells += month.atDay(day)
    }
    while (cells.size % 7 != 0) {
        cells += null
    }
    return cells
}
