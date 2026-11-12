package app.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitia.feature.training.domain.DeleteExerciseUseCase
import app.fitia.feature.training.domain.Exercise
import app.fitia.feature.training.domain.ExerciseMuscle
import app.fitia.feature.training.domain.Muscle
import app.fitia.feature.training.domain.ObserveExercisesUseCase
import app.fitia.feature.training.domain.ObserveMusclesUseCase
import app.fitia.feature.training.domain.SaveExerciseUseCase
import app.fitia.feature.training.domain.SaveMuscleUseCase
import app.fitia.feature.training.domain.SuggestExerciseMusclesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExercisesViewModel(
    observeExercisesUseCase: ObserveExercisesUseCase,
    observeMusclesUseCase: ObserveMusclesUseCase,
    private val saveExerciseUseCase: SaveExerciseUseCase,
    private val saveMuscleUseCase: SaveMuscleUseCase,
    private val deleteExerciseUseCase: DeleteExerciseUseCase,
    private val suggestExerciseMusclesUseCase: SuggestExerciseMusclesUseCase,
) : ViewModel() {
    private var suggestJob: Job? = null

    val exercises: StateFlow<List<Exercise>> = observeExercisesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val muscles: StateFlow<List<Muscle>> = observeMusclesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun saveExercise(
        name: String,
        muscleIds: List<Long>,
        exerciseId: Long = 0,
        notes: String = "",
    ) {
        if (name.isBlank() || muscleIds.isEmpty()) return
        viewModelScope.launch {
            val existing = exercises.value.firstOrNull { it.id == exerciseId }
            val resolved = muscleIds.distinct().map { id ->
                val known = muscles.value.firstOrNull { it.id == id }
                ExerciseMuscle(id, known?.name.orEmpty())
            }
            saveExerciseUseCase(
                Exercise(
                    id = exerciseId,
                    name = name.trim(),
                    muscleId = muscleIds.first(),
                    notes = existing?.notes ?: notes,
                    createdAtEpochMs = existing?.createdAtEpochMs ?: 0,
                    muscles = resolved,
                ),
            )
        }
    }

    fun suggestMuscles(
        exerciseName: String,
        onFinished: (Result<List<ExerciseMuscle>>) -> Unit,
    ) {
        suggestJob?.cancel()
        suggestJob = viewModelScope.launch {
            onFinished(runCatching { suggestExerciseMusclesUseCase(exerciseName) })
        }
    }

    fun createMuscle(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            saveMuscleUseCase(Muscle(name = name.trim()))
        }
    }

    fun deleteExercise(id: Long) {
        viewModelScope.launch {
            deleteExerciseUseCase(id)
        }
    }
}
