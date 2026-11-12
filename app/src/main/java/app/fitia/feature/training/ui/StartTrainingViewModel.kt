package app.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitia.feature.training.domain.GetPlanUseCase
import app.fitia.feature.training.domain.ObserveActiveTrainingPlanUseCase
import app.fitia.feature.training.domain.ObserveActiveSessionUseCase
import app.fitia.feature.training.domain.ObserveWorkoutSchedulesUseCase
import app.fitia.feature.training.domain.SetActiveTrainingPlanUseCase
import app.fitia.feature.training.domain.ObserveWorkoutsUseCase
import app.fitia.feature.training.domain.StartBlankSessionUseCase
import app.fitia.feature.training.domain.StartSessionFromWorkoutUseCase
import app.fitia.feature.training.domain.TrainingPlan
import app.fitia.feature.training.domain.TrainingSession
import app.fitia.feature.training.domain.Workout
import app.fitia.feature.training.domain.WorkoutSchedule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class StartTrainingViewModel(
    observeActiveTrainingPlanUseCase: ObserveActiveTrainingPlanUseCase,
    observeWorkoutSchedulesUseCase: ObserveWorkoutSchedulesUseCase,
    observeWorkoutsUseCase: ObserveWorkoutsUseCase,
    observeActiveSessionUseCase: ObserveActiveSessionUseCase,
    private val getPlanUseCase: GetPlanUseCase,
    private val startSessionFromWorkoutUseCase: StartSessionFromWorkoutUseCase,
    private val startBlankSessionUseCase: StartBlankSessionUseCase,
    private val setActiveTrainingPlanUseCase: SetActiveTrainingPlanUseCase,
) : ViewModel() {

    private val activePlanId = observeActiveTrainingPlanUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _activePlan = MutableStateFlow<TrainingPlan?>(null)
    val activePlan: StateFlow<TrainingPlan?> = _activePlan.asStateFlow()

    init {
        viewModelScope.launch {
            activePlanId.collect { id ->
                _activePlan.value = id?.let { getPlanUseCase(it) }
            }
        }
    }

    val schedules: StateFlow<List<WorkoutSchedule>> = observeWorkoutSchedulesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val todaySchedule: StateFlow<WorkoutSchedule?> = schedules
        .map { list ->
            val today = LocalDate.now().toEpochDay()
            list.firstOrNull { it.dayEpochDay == today }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val workouts: StateFlow<List<Workout>> = observeWorkoutsUseCase().stateIn(
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

    private val _needsWorkoutChoice = MutableSharedFlow<Unit>()
    val needsWorkoutChoice: SharedFlow<Unit> = _needsWorkoutChoice.asSharedFlow()

    fun trainToday() {
        viewModelScope.launch {
            val active = activeSession.value
            if (active != null) {
                _sessionStarted.emit(active.id)
                return@launch
            }
            val scheduled = todaySchedule.value
            if (scheduled != null) {
                _sessionStarted.emit(startSessionFromWorkoutUseCase(scheduled.workoutId))
            } else {
                _needsWorkoutChoice.emit(Unit)
            }
        }
    }

    fun startFromWorkout(workoutId: Long) {
        viewModelScope.launch {
            _sessionStarted.emit(startSessionFromWorkoutUseCase(workoutId))
        }
    }

    fun stopFollowingPlan() {
        viewModelScope.launch {
            setActiveTrainingPlanUseCase(null)
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
