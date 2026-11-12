package app.fitia.feature.training.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

private const val AUTOSAVE_DELAY_MS = 600L

/**
 * Saves a set on its own once the user stops typing, so no save button is needed.
 * Only writes when the values really changed, which keeps the list from refreshing
 * on every recomposition.
 */
@Composable
internal fun SetAutosaveEffect(
    setKey: Any,
    weightText: String,
    repsText: String,
    onSave: (weightKg: Double?, reps: Int?) -> Unit,
) {
    val parsedWeight = weightText.replace(',', '.').toDoubleOrNull()
    val parsedReps = repsText.toIntOrNull()
    val save by rememberUpdatedState(onSave)
    var lastSaved by remember(setKey) { mutableStateOf(parsedWeight to parsedReps) }

    LaunchedEffect(setKey, parsedWeight, parsedReps) {
        val next = parsedWeight to parsedReps
        if (next == lastSaved) return@LaunchedEffect
        delay(AUTOSAVE_DELAY_MS)
        lastSaved = next
        save(next.first, next.second)
    }
}
