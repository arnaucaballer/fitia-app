package com.arnaucaballer.fitia.feature.training.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R

private enum class TrainingTab(
    @StringRes val label: Int,
    @StringRes val hint: Int,
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
    val tab = TrainingTab.entries[selected]

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.training_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp),
        )
        Text(
            text = stringResource(tab.hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
        )
        ScrollableTabRow(
            selectedTabIndex = selected,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            edgePadding = 16.dp,
            indicator = { _ -> },
            divider = {},
        ) {
            TrainingTab.entries.forEachIndexed { index, item ->
                val isSelected = index == selected
                Tab(
                    selected = isSelected,
                    onClick = {
                        selected = index
                        if (index != TrainingTab.Calendar.ordinal) {
                            highlightWeeklySetup = false
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = {
                        Text(
                            text = stringResource(item.label),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }
        }
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                TrainingTab.Start -> TrainingStartScreen(
                    onOpenSession = onOpenSession,
                    onOpenWeeklyCalendar = {
                        highlightWeeklySetup = true
                        selected = TrainingTab.Calendar.ordinal
                    },
                    onPickPlanToFollow = {
                        selected = TrainingTab.Plans.ordinal
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                TrainingTab.Calendar -> TrainingCalendarScreen(
                    onOpenSession = onOpenSession,
                    highlightWeeklySetup = highlightWeeklySetup,
                    modifier = Modifier.fillMaxSize(),
                )
                TrainingTab.Workouts -> WorkoutsScreen(
                    onOpenWorkout = onOpenWorkout,
                    onOpenSession = onOpenSession,
                    modifier = Modifier.fillMaxSize(),
                )
                TrainingTab.Plans -> PlansScreen(
                    onOpenPlan = onOpenPlan,
                    onGeneratePlan = onGeneratePlan,
                    onFollowPlan = {
                        highlightWeeklySetup = true
                        selected = TrainingTab.Calendar.ordinal
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                TrainingTab.Exercises -> ExercisesScreen(modifier = Modifier.fillMaxSize())
                TrainingTab.Log -> LogSessionScreen(
                    onOpenSession = onOpenSession,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
