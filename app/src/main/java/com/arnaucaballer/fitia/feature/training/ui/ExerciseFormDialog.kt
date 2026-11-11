package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaPill
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.feature.training.domain.ExerciseMuscle
import com.arnaucaballer.fitia.feature.training.domain.Muscle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFormDialog(
    title: String,
    muscles: List<Muscle>,
    initialName: String = "",
    initialMuscleIds: List<Long> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, muscleIds: List<Long>) -> Unit,
    onEnsureMuscle: (String) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var selected by remember(initialMuscleIds) {
        mutableStateOf(
            initialMuscleIds.map { ExerciseMuscle(id = it, name = "") },
        )
    }
    var pendingMuscleName by remember { mutableStateOf<String?>(null) }
    var showMap by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Fill in names as soon as the muscles flow knows them.
    LaunchedEffect(muscles, selected) {
        if (selected.isEmpty()) return@LaunchedEffect
        val names = muscles.associate { it.id to it.name }
        if (selected.all { it.name.isNotBlank() || names[it.id] == null }) return@LaunchedEffect
        selected = selected.map { item ->
            if (item.name.isBlank()) item.copy(name = names[item.id].orEmpty()) else item
        }
    }

    // A muscle created from the picker is added once the flow emits it.
    LaunchedEffect(muscles, pendingMuscleName) {
        val wanted = pendingMuscleName ?: return@LaunchedEffect
        val created = muscles.firstOrNull { it.name.equals(wanted, ignoreCase = true) }
            ?: return@LaunchedEffect
        if (selected.none { it.id == created.id }) {
            selected = selected + ExerciseMuscle(created.id, created.name)
        }
        pendingMuscleName = null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .imePadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.field_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                MuscleSelectionField(
                    muscles = selected,
                    onOpen = { showMap = true },
                    onRemove = { id -> selected = selected.filterNot { it.id == id } },
                )
                if (selected.size > 1) {
                    Text(
                        text = stringResource(R.string.training_muscle_primary_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FitiaPrimaryButton(
                    text = stringResource(R.string.action_save),
                    onClick = { onConfirm(name.trim(), selected.map { it.id }) },
                    enabled = name.isNotBlank() && selected.isNotEmpty(),
                )
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    }

    if (showMap) {
        MusclePickerDialog(
            muscles = muscles,
            onDismiss = { showMap = false },
            onPick = { muscleName ->
                val existing = muscles.firstOrNull {
                    it.name.equals(muscleName, ignoreCase = true)
                }
                if (existing != null) {
                    if (selected.none { it.id == existing.id }) {
                        selected = selected + ExerciseMuscle(existing.id, existing.name)
                    }
                } else {
                    pendingMuscleName = muscleName
                    onEnsureMuscle(muscleName)
                }
                showMap = false
            },
        )
    }
}

@Composable
private fun MuscleSelectionField(
    muscles: List<ExerciseMuscle>,
    onOpen: () -> Unit,
    onRemove: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.training_muscles_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (muscles.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessibilityNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.training_select_muscle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Column
        }
        muscles.forEachIndexed { index, muscle ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = muscle.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                if (index == 0) {
                    Text(
                        text = stringResource(R.string.training_muscle_primary),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .clip(FitiaPill)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                IconButton(
                    onClick = { onRemove(muscle.id) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.training_remove_muscle),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FitiaPill)
                .clickable(onClick = onOpen)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.training_add_muscle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
