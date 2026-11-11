package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.ClearWorkoutScheduleUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveActiveSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveActiveTrainingPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObservePlanWeeklyScheduleUseCase
import com.arnaucaballer.fitia.feature.training.domain.PlanWeeklySlot
import com.arnaucaballer.fitia.feature.training.domain.SetActiveTrainingPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.SetPlanWeeklySlotUseCase
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.ObserveSessionsUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveWorkoutSchedulesUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.SetWorkoutScheduleUseCase
import com.arnaucaballer.fitia.feature.training.domain.StartBlankSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.StartSessionFromWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.TrainingSession
import com.arnaucaballer.fitia.feature.training.domain.Workout
import com.arnaucaballer.fitia.feature.training.domain.WorkoutSchedule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingCalendarViewModel(
    observeSessionsUseCase: ObserveSessionsUseCase,
    observeWorkoutSchedulesUseCase: ObserveWorkoutSchedulesUseCase,
    observeWorkoutsUseCase: ObserveWorkoutsUseCase,
    observeActiveSessionUseCase: ObserveActiveSessionUseCase,
    observeActiveTrainingPlanUseCase: ObserveActiveTrainingPlanUseCase,
    observePlanWeeklyScheduleUseCase: ObservePlanWeeklyScheduleUseCase,
    private val getPlanUseCase: GetPlanUseCase,
    private val setWorkoutScheduleUseCase: SetWorkoutScheduleUseCase,
    private val clearWorkoutScheduleUseCase: ClearWorkoutScheduleUseCase,
    private val setPlanWeeklySlotUseCase: SetPlanWeeklySlotUseCase,
    private val setActiveTrainingPlanUseCase: SetActiveTrainingPlanUseCase,
    private val startSessionFromWorkoutUseCase: StartSessionFromWorkoutUseCase,
    private val startBlankSessionUseCase: StartBlankSessionUseCase,
) : ViewModel() {

    val activePlanId: StateFlow<Long?> = observeActiveTrainingPlanUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val _activePlan = MutableStateFlow<TrainingPlan?>(null)
    val activePlan: StateFlow<TrainingPlan?> = _activePlan.asStateFlow()

    val weeklySlots: StateFlow<List<PlanWeeklySlot>> = activePlanId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else observePlanWeeklyScheduleUseCase(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    init {
        viewModelScope.launch {
            activePlanId.collect { id ->
                _activePlan.value = id?.let { getPlanUseCase(it) }
            }
        }
    }

    val sessions: StateFlow<List<TrainingSession>> = observeSessionsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val schedules: StateFlow<List<WorkoutSchedule>> = observeWorkoutSchedulesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
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

    private val _needsWorkoutChoice = MutableSharedFlow<Long>()
    val needsWorkoutChoice: SharedFlow<Long> = _needsWorkoutChoice.asSharedFlow()

    fun assignWorkout(dayEpochDay: Long, workoutId: Long) {
        viewModelScope.launch {
            setWorkoutScheduleUseCase(dayEpochDay, workoutId)
        }
    }

    fun clearSchedule(dayEpochDay: Long) {
        viewModelScope.launch {
            clearWorkoutScheduleUseCase(dayEpochDay)
        }
    }

    fun train(dayEpochDay: Long, scheduledWorkoutId: Long?) {
        viewModelScope.launch {
            val active = activeSession.value
            if (active != null) {
                _sessionStarted.emit(active.id)
                return@launch
            }
            if (scheduledWorkoutId != null) {
                _sessionStarted.emit(startSessionFromWorkoutUseCase(scheduledWorkoutId))
            } else {
                _needsWorkoutChoice.emit(dayEpochDay)
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

    fun setWeeklySlot(dayOfWeek: Int, workoutId: Long?) {
        val planId = activePlanId.value ?: return
        viewModelScope.launch {
            setPlanWeeklySlotUseCase(planId, dayOfWeek, workoutId)
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
