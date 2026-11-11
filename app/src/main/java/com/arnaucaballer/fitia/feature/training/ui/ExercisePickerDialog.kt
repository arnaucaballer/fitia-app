package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.design.FitiaSecondaryButton
import com.arnaucaballer.fitia.feature.training.domain.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerDialog(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onPick: (Exercise) -> Unit,
    onCreateNew: (prefillName: String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val unassignedLabel = stringResource(R.string.training_exercises_unassigned)
    val filtered = remember(exercises, query) {
        val needle = query.trim()
        if (needle.isEmpty()) {
            exercises
        } else {
            exercises.filter { exercise ->
                exercise.name.contains(needle, ignoreCase = true) ||
                    exercise.primaryMuscleName.contains(needle, ignoreCase = true) ||
                    exercise.extraMuscleNames.any { it.contains(needle, ignoreCase = true) }
            }
        }
    }
    val sections = remember(filtered, unassignedLabel) {
        filtered
            .groupBy { exercise -> exercise.primaryMuscleName.ifBlank { unassignedLabel } }
            .entries
            .sortedWith(
                compareBy<Map.Entry<String, List<Exercise>>> {
                    if (it.key == unassignedLabel) 1 else 0
                }.thenBy { it.key.lowercase() },
            )
            .map { (muscleName, items) ->
                muscleName to items.sortedBy { it.name.lowercase() }
            }
    }
    val nothingMatches = exercises.isNotEmpty() && filtered.isEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (filtered.isNotEmpty()) Modifier.fillMaxHeight() else Modifier)
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.training_add_exercise),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.training_pick_exercise),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                exercises.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.training_picker_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                nothingMatches -> {
                    Text(
                        text = stringResource(R.string.training_picker_no_match),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        sections.forEach { (muscleName, muscleExercises) ->
                            item(key = "muscle-$muscleName") {
                                Text(
                                    text = muscleName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                )
                            }
                            items(muscleExercises, key = { it.id }) { exercise ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clickable { onPick(exercise) }
                                        .padding(vertical = 8.dp),
                                ) {
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (exercises.isNotEmpty()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.training_search_exercise)) },
                    singleLine = true,
                )
            }
            Text(
                text = stringResource(R.string.training_picker_create_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (exercises.isEmpty() || nothingMatches) {
                FitiaPrimaryButton(
                    text = stringResource(R.string.training_create_exercise),
                    onClick = { onCreateNew(query.trim()) },
                )
            } else {
                FitiaSecondaryButton(
                    text = stringResource(R.string.training_create_exercise),
                    onClick = { onCreateNew(query.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
}
