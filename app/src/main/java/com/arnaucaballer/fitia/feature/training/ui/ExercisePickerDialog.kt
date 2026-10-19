package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.feature.training.domain.Exercise

@Composable
fun ExercisePickerDialog(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onPick: (Exercise) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.training_pick_exercise)) },
        text = {
            if (exercises.isEmpty()) {
                Text(stringResource(R.string.training_empty_exercises))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(exercises, key = { it.id }) { exercise ->
                        Text(
                            text = exercise.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(exercise) },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
