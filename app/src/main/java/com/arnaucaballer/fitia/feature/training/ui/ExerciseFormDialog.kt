package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.feature.training.domain.Muscle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseFormDialog(
    title: String,
    muscles: List<Muscle>,
    initialName: String = "",
    initialMuscleId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, muscleId: Long?) -> Unit,
    onEnsureMuscle: (String) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var muscleId by remember(initialMuscleId) { mutableStateOf(initialMuscleId) }
    var selectedMuscleName by remember(initialMuscleId) { mutableStateOf("") }
    var showMap by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(muscles, muscleId, selectedMuscleName) {
        val byId = muscles.firstOrNull { it.id == muscleId }
        if (byId != null) {
            selectedMuscleName = byId.name
            return@LaunchedEffect
        }
        val byName = muscles.firstOrNull { it.name.equals(selectedMuscleName, ignoreCase = true) }
        if (byName != null) muscleId = byName.id
    }

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
                .navigationBarsPadding()
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
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedMuscleName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.training_muscle_label)) },
                    placeholder = { Text(stringResource(R.string.training_select_muscle)) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.AccessibilityNew,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showMap = true },
                )
            }
            FitiaPrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = { onConfirm(name.trim(), muscleId) },
                enabled = name.isNotBlank() && muscleId != null,
            )
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }

    if (showMap) {
        MusclePickerDialog(
            muscles = muscles,
            onDismiss = { showMap = false },
            onPick = { muscleName ->
                selectedMuscleName = muscleName
                val existing = muscles.firstOrNull { it.name.equals(muscleName, ignoreCase = true) }
                if (existing != null) {
                    muscleId = existing.id
                } else {
                    muscleId = null
                    onEnsureMuscle(muscleName)
                }
                showMap = false
            },
        )
    }
}
