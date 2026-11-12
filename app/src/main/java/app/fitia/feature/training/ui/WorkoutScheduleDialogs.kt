package app.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fitia.R
import app.fitia.design.FitiaSecondaryButton
import app.fitia.feature.training.domain.Workout
import app.fitia.design.fitiaPress

@Composable
fun WorkoutDayAssignDialog(
    workouts: List<Workout>,
    onDismiss: () -> Unit,
    onPick: (Workout) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.training_schedule_pick)) },
        text = {
            if (workouts.isEmpty()) {
                Text(stringResource(R.string.training_schedule_empty_workouts))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(workouts, key = { it.id }) { workout ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(workout) }
                                .padding(vertical = 10.dp),
                        ) {
                            Text(workout.name, style = MaterialTheme.typography.titleMedium)
                            if (!workout.isGeneric) {
                                Text(
                                    text = stringResource(R.string.training_workout_type_plan_only),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(modifier = Modifier.fitiaPress(), onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
fun WorkoutTrainChoiceDialog(
    workouts: List<Workout>,
    onDismiss: () -> Unit,
    onStartWorkout: (Workout) -> Unit,
    onStartBlank: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.training_train_choose_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.training_train_choose_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (workouts.isEmpty()) {
                    Text(stringResource(R.string.training_log_empty_workouts))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        items(workouts, key = { it.id }) { workout ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onStartWorkout(workout) }
                                    .padding(vertical = 10.dp),
                            ) {
                                Text(workout.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (workout.isGeneric) {
                                        stringResource(R.string.training_workout_type_generic)
                                    } else {
                                        stringResource(R.string.training_workout_type_plan_only)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (workout.isGeneric) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                )
                            }
                        }
                    }
                }
                FitiaSecondaryButton(
                    text = stringResource(R.string.training_log_blank),
                    onClick = onStartBlank,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(modifier = Modifier.fitiaPress(), onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
