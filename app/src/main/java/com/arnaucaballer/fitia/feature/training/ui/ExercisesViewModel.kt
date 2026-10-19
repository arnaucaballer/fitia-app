package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.DeleteExerciseUseCase
import com.arnaucaballer.fitia.feature.training.domain.Exercise
import com.arnaucaballer.fitia.feature.training.domain.ObserveExercisesUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveExerciseUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExercisesViewModel(
    observeExercisesUseCase: ObserveExercisesUseCase,
    private val saveExerciseUseCase: SaveExerciseUseCase,
    private val deleteExerciseUseCase: DeleteExerciseUseCase,
) : ViewModel() {

    val exercises: StateFlow<List<Exercise>> = observeExercisesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun createExercise(name: String, muscleGroup: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            saveExerciseUseCase(
                Exercise(name = name.trim(), muscleGroup = muscleGroup.trim()),
            )
        }
    }

    fun deleteExercise(id: Long) {
        viewModelScope.launch {
            deleteExerciseUseCase(id)
        }
    }
}
