package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.GetPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveGenericWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.SavePlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.TrainingRepository
import com.arnaucaballer.fitia.feature.training.domain.Workout
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlanDetailViewModel(
    private val planId: Long,
    private val getPlanUseCase: GetPlanUseCase,
    observeGenericWorkoutsUseCase: ObserveGenericWorkoutsUseCase,
    private val savePlanUseCase: SavePlanUseCase,
    private val trainingRepository: TrainingRepository,
) : ViewModel() {

    private val _plan = MutableStateFlow<TrainingPlan?>(null)
    val plan: StateFlow<TrainingPlan?> = _plan.asStateFlow()

    val genericWorkouts: StateFlow<List<Workout>> = observeGenericWorkoutsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _createdWorkout = MutableSharedFlow<Long>()
    val createdWorkout: SharedFlow<Long> = _createdWorkout.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _plan.value = getPlanUseCase(planId)
        }
    }

    /** Available generics not already linked to this plan. */
    fun availableGenerics(): List<Workout> {
        val linkedIds = _plan.value?.workouts?.map { it.id }.orEmpty().toSet()
        return genericWorkouts.value.filter { it.id !in linkedIds }
    }

    fun createPlanOwnedWorkout(name: String) {
        viewModelScope.launch {
            val id = trainingRepository.createPlanOwnedWorkout(planId, name)
            refresh()
            _createdWorkout.emit(id)
        }
    }

    fun linkGenericWorkout(workout: Workout) {
        viewModelScope.launch {
            trainingRepository.linkWorkoutToPlan(workout.id, planId)
            refresh()
        }
    }

    fun personalizeGeneric(workout: Workout) {
        viewModelScope.launch {
            val id = trainingRepository.copyGenericWorkoutIntoPlan(workout.id, planId)
            refresh()
            _createdWorkout.emit(id)
        }
    }

    fun removeWorkout(workoutId: Long) {
        val current = _plan.value ?: return
        viewModelScope.launch {
            savePlanUseCase(current.copy(workouts = current.workouts.filterNot { it.id == workoutId }))
            val removed = current.workouts.firstOrNull { it.id == workoutId }
            // Plan-exclusive workouts disappear with the plan link; generics stay in the library.
            if (removed != null && !removed.isGeneric) {
                trainingRepository.deleteWorkout(workoutId)
            }
            refresh()
        }
    }
}
