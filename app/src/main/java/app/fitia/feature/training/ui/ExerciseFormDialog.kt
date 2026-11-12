package app.fitia.feature.training.ui

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.fitia.R
import app.fitia.design.FitiaPill
import app.fitia.design.FitiaPrimaryButton
import app.fitia.design.FitiaSecondaryButton
import app.fitia.design.FitiaSpace
import app.fitia.feature.training.domain.ExerciseMuscle
import app.fitia.feature.training.domain.Muscle
import app.fitia.design.fitiaPress

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
    onSuggestMuscles: (String, (Result<List<ExerciseMuscle>>) -> Unit) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var selected by remember(initialMuscleIds) {
        mutableStateOf(
            initialMuscleIds.map { ExerciseMuscle(id = it, name = "") },
        )
    }
    var pendingMuscleName by remember { mutableStateOf<String?>(null) }
    var pendingReplaceId by remember { mutableStateOf<Long?>(null) }
    var replaceTargetId by remember { mutableStateOf<Long?>(null) }
    var suggesting by remember { mutableStateOf(false) }
    var suggestError by remember { mutableStateOf<String?>(null) }
    var showMap by remember { mutableStateOf(false) }
    val emptySuggestion = stringResource(R.string.training_suggest_muscles_empty)
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
        selected = selected.withMuscle(pendingReplaceId, ExerciseMuscle(created.id, created.name))
        pendingMuscleName = null
        pendingReplaceId = null
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
                    .padding(FitiaSpace.page()),
                verticalArrangement = Arrangement.spacedBy(FitiaSpace.stack),
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
                    exerciseName = name,
                    suggesting = suggesting,
                    error = suggestError,
                    onSuggest = {
                        suggesting = true
                        suggestError = null
                        onSuggestMuscles(name.trim()) { result ->
                            suggesting = false
                            result.onSuccess { found ->
                                if (found.isEmpty()) {
                                    suggestError = emptySuggestion
                                } else {
                                    selected = found
                                }
                            }.onFailure { error ->
                                suggestError = error.message ?: emptySuggestion
                            }
                        }
                    },
                    onOpen = {
                        replaceTargetId = null
                        showMap = true
                    },
                    onChange = { id ->
                        replaceTargetId = id
                        showMap = true
                    },
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
                TextButton(modifier = Modifier.fitiaPress(), onClick = onDismiss) {
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
                val replaceId = replaceTargetId
                replaceTargetId = null
                suggestError = null
                val existing = muscles.firstOrNull {
                    it.name.equals(muscleName, ignoreCase = true)
                }
                if (existing != null) {
                    selected = selected.withMuscle(
                        replaceId,
                        ExerciseMuscle(existing.id, existing.name),
                    )
                } else {
                    pendingMuscleName = muscleName
                    pendingReplaceId = replaceId
                    onEnsureMuscle(muscleName)
                }
                showMap = false
            },
        )
    }
}

private fun List<ExerciseMuscle>.withMuscle(replaceId: Long?, muscle: ExerciseMuscle): List<ExerciseMuscle> {
    if (replaceId == null) {
        if (any { it.id == muscle.id }) return this
        return this + muscle
    }
    val withoutTarget = filterNot { it.id == replaceId }
    if (withoutTarget.any { it.id == muscle.id }) return withoutTarget
    return map { item -> if (item.id == replaceId) muscle else item }
}

@Composable
private fun MuscleSelectionField(
    muscles: List<ExerciseMuscle>,
    exerciseName: String,
    suggesting: Boolean,
    error: String?,
    onSuggest: () -> Unit,
    onOpen: () -> Unit,
    onChange: (Long) -> Unit,
    onRemove: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = FitiaSpace.card, vertical = FitiaSpace.stack),
        verticalArrangement = Arrangement.spacedBy(FitiaSpace.stack),
    ) {
        Text(
            text = stringResource(R.string.training_muscles_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (exerciseName.isNotBlank()) {
            FitiaSecondaryButton(
                text = stringResource(
                    if (suggesting) {
                        R.string.training_suggest_muscles_loading
                    } else {
                        R.string.training_suggest_muscles
                    },
                ),
                onClick = onSuggest,
                enabled = !suggesting,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.training_suggest_muscles_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
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
        val changeLabel = stringResource(R.string.training_change_muscle)
        muscles.forEachIndexed { index, muscle ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = { onChange(muscle.id) })
                    .semantics { contentDescription = changeLabel }
                    .padding(vertical = 4.dp),
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
                    modifier = Modifier.size(36.dp).fitiaPress(),
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
