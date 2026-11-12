package app.fitia.feature.training.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.fitia.R
import app.fitia.design.FitiaPillChip
import app.fitia.design.FitiaSpace
import app.fitia.navigation.RetainedPages

private enum class TrainingTab(
    @param:StringRes val label: Int,
    @param:StringRes val hint: Int,
) {
    Start(R.string.training_tab_start, R.string.training_tab_hint_start),
    Calendar(R.string.training_tab_calendar, R.string.training_tab_hint_calendar),
    Workouts(R.string.training_tab_workouts, R.string.training_tab_hint_workouts),
    Plans(R.string.training_tab_plans, R.string.training_tab_hint_plans),
    Exercises(R.string.training_tab_exercises, R.string.training_tab_hint_exercises),
    Log(R.string.training_tab_log, R.string.training_tab_hint_log),
}

@Composable
fun TrainingHubScreen(
    onOpenPlan: (Long) -> Unit,
    onOpenWorkout: (Long) -> Unit,
    onOpenSession: (Long) -> Unit,
    onGeneratePlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by rememberSaveable { mutableIntStateOf(TrainingTab.Start.ordinal) }
    var highlightWeeklySetup by rememberSaveable { mutableStateOf(false) }
    var opened by remember { mutableStateOf(listOf(TrainingTab.entries[selected])) }
    val tab = TrainingTab.entries[selected]
    val openPlanState = rememberUpdatedState(onOpenPlan)
    val openWorkoutState = rememberUpdatedState(onOpenWorkout)
    val openSessionState = rememberUpdatedState(onOpenSession)
    val generatePlanState = rememberUpdatedState(onGeneratePlan)
    val openPlan = remember { { id: Long -> openPlanState.value(id) } }
    val openWorkout = remember { { id: Long -> openWorkoutState.value(id) } }
    val openSession = remember { { id: Long -> openSessionState.value(id) } }
    val generatePlan = remember { { generatePlanState.value() } }
    val openCalendar = remember {
        {
            highlightWeeklySetup = true
            selected = TrainingTab.Calendar.ordinal
            val calendar = TrainingTab.Calendar
            if (calendar !in opened) opened = opened + calendar
        }
    }
    val openPlans = remember {
        {
            selected = TrainingTab.Plans.ordinal
            val plans = TrainingTab.Plans
            if (plans !in opened) opened = opened + plans
        }
    }

    fun select(next: TrainingTab) {
        selected = next.ordinal
        if (next !in opened) opened = opened + next
        if (next != TrainingTab.Calendar) highlightWeeklySetup = false
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .background(MaterialTheme.colorScheme.background),
        ) {
            Text(
                text = stringResource(R.string.training_title),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(
                    start = FitiaSpace.screen,
                    end = FitiaSpace.screen,
                    top = FitiaSpace.screen,
                ),
            )
            Text(
                text = stringResource(tab.hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2,
                maxLines = 2,
                modifier = Modifier.padding(
                    start = FitiaSpace.screen,
                    end = FitiaSpace.screen,
                    top = FitiaSpace.related,
                    bottom = FitiaSpace.stack,
                ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FitiaSpace.screen),
                verticalArrangement = Arrangement.spacedBy(FitiaSpace.related),
            ) {
                TrainingTab.entries.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FitiaSpace.related),
                    ) {
                        row.forEach { item ->
                            FitiaPillChip(
                                text = stringResource(item.label),
                                selected = item == tab,
                                onClick = { select(item) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            )
        }
        RetainedPages(
            pages = opened,
            selected = tab,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds(),
            pageKey = { it.name },
        ) { page ->
            when (page) {
                TrainingTab.Start -> TrainingStartScreen(
                    onOpenSession = openSession,
                    onOpenWeeklyCalendar = openCalendar,
                    onPickPlanToFollow = openPlans,
                    modifier = Modifier.fillMaxSize(),
                )
                TrainingTab.Calendar -> TrainingCalendarScreen(
                    onOpenSession = openSession,
                    highlightWeeklySetup = highlightWeeklySetup,
                    modifier = Modifier.fillMaxSize(),
                )
                TrainingTab.Workouts -> WorkoutsScreen(
                    onOpenWorkout = openWorkout,
                    onOpenSession = openSession,
                    modifier = Modifier.fillMaxSize(),
                )
                TrainingTab.Plans -> PlansScreen(
                    onOpenPlan = openPlan,
                    onGeneratePlan = generatePlan,
                    onFollowPlan = openCalendar,
                    modifier = Modifier.fillMaxSize(),
                )
                TrainingTab.Exercises -> ExercisesScreen(modifier = Modifier.fillMaxSize())
                TrainingTab.Log -> LogSessionScreen(
                    onOpenSession = openSession,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
