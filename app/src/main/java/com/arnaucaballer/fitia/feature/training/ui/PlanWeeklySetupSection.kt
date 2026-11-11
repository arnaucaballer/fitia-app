package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaCard
import com.arnaucaballer.fitia.design.FitiaSecondaryButton
import com.arnaucaballer.fitia.feature.training.domain.PlanWeeklySlot
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.Workout
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PlanWeeklySetupSection(
    plan: TrainingPlan,
    weeklySlots: List<PlanWeeklySlot>,
    onAssign: (dayOfWeek: Int, workoutId: Long?) -> Unit,
    onUnfollowPlan: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var pickDay by rememberSaveable { mutableIntStateOf(0) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val slotByDay = remember(weeklySlots) { weeklySlots.associateBy { it.dayOfWeek } }
    val locale = Locale.getDefault()

    FitiaCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.training_weekly_title, plan.name),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.training_weekly_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (onUnfollowPlan != null) {
            FitiaSecondaryButton(
                text = stringResource(R.string.training_unfollow_plan),
                onClick = onUnfollowPlan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
        }
        DayOfWeek.entries.forEach { dow ->
            val slot = slotByDay[dow.value]
            val label = dow.getDisplayName(TextStyle.FULL, locale)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        pickDay = dow.value
                        showPicker = true
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(0.42f),
                )
                Text(
                    text = slot?.workoutName ?: stringResource(R.string.training_weekly_rest),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (slot?.workoutId != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(0.58f),
                )
            }
        }
    }

    if (showPicker && pickDay in DayOfWeek.MONDAY.value..DayOfWeek.SUNDAY.value) {
        WeeklyWorkoutPickerDialog(
            planWorkouts = plan.workouts,
            onDismiss = { showPicker = false },
            onPickRest = {
                showPicker = false
                onAssign(pickDay, null)
            },
            onPick = { workout ->
                showPicker = false
                onAssign(pickDay, workout.id)
            },
        )
    }
}

@Composable
private fun WeeklyWorkoutPickerDialog(
    planWorkouts: List<Workout>,
    onDismiss: () -> Unit,
    onPickRest: () -> Unit,
    onPick: (Workout) -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.training_weekly_pick)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.training_weekly_pick_rest),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onPickRest)
                        .padding(vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                planWorkouts.forEach { workout ->
                    Text(
                        text = workout.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(workout) }
                            .padding(vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
