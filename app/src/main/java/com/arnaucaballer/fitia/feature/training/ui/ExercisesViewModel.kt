package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.DeleteExerciseUseCase
import com.arnaucaballer.fitia.feature.training.domain.Exercise
import com.arnaucaballer.fitia.feature.training.domain.Muscle
import com.arnaucaballer.fitia.feature.training.domain.ObserveExercisesUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveMusclesUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveExerciseUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveMuscleUseCase
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
) : ViewModel() {

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
        muscleId: Long?,
        exerciseId: Long = 0,
        notes: String = "",
    ) {
        if (name.isBlank() || muscleId == null) return
        viewModelScope.launch {
            val existing = exercises.value.firstOrNull { it.id == exerciseId }
            saveExerciseUseCase(
                Exercise(
                    id = exerciseId,
                    name = name.trim(),
                    muscleId = muscleId,
                    notes = existing?.notes ?: notes,
                    createdAtEpochMs = existing?.createdAtEpochMs ?: 0,
                ),
            )
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
