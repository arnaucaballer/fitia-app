package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.DeleteWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveGenericWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.StartSessionFromWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.Workout
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutsViewModel(
    observeGenericWorkoutsUseCase: ObserveGenericWorkoutsUseCase,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val startSessionFromWorkoutUseCase: StartSessionFromWorkoutUseCase,
) : ViewModel() {

    val workouts: StateFlow<List<Workout>> = observeGenericWorkoutsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _sessionStarted = MutableSharedFlow<Long>()
    val sessionStarted: SharedFlow<Long> = _sessionStarted.asSharedFlow()

    fun createWorkout(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            saveWorkoutUseCase(Workout(name = name.trim()))
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch {
            deleteWorkoutUseCase(id)
        }
    }

    fun startSession(workoutId: Long) {
        viewModelScope.launch {
            val sessionId = startSessionFromWorkoutUseCase(workoutId)
            _sessionStarted.emit(sessionId)
        }
    }
}
