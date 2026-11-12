package app.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitia.feature.training.domain.ObserveActiveSessionUseCase
import app.fitia.feature.training.domain.ObservePlansUseCase
import app.fitia.feature.training.domain.ObserveWorkoutsUseCase
import app.fitia.feature.training.domain.StartBlankSessionUseCase
import app.fitia.feature.training.domain.StartSessionFromWorkoutUseCase
import app.fitia.feature.training.domain.TrainingPlan
import app.fitia.feature.training.domain.TrainingSession
import app.fitia.feature.training.domain.Workout
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogSessionViewModel(
    observeWorkoutsUseCase: ObserveWorkoutsUseCase,
    observePlansUseCase: ObservePlansUseCase,
    observeActiveSessionUseCase: ObserveActiveSessionUseCase,
    private val startSessionFromWorkoutUseCase: StartSessionFromWorkoutUseCase,
    private val startBlankSessionUseCase: StartBlankSessionUseCase,
) : ViewModel() {

    val workouts: StateFlow<List<Workout>> = observeWorkoutsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val plans: StateFlow<List<TrainingPlan>> = observePlansUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val activeSession: StateFlow<TrainingSession?> = observeActiveSessionUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _sessionStarted = MutableSharedFlow<Long>()
    val sessionStarted: SharedFlow<Long> = _sessionStarted.asSharedFlow()

    fun startFromWorkout(workoutId: Long) {
        viewModelScope.launch {
            _sessionStarted.emit(startSessionFromWorkoutUseCase(workoutId))
        }
    }

    fun startBlank(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _sessionStarted.emit(startBlankSessionUseCase(trimmed))
        }
    }
}
