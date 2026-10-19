package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.GetPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.SavePlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlanDetailViewModel(
    private val planId: Long,
    private val getPlanUseCase: GetPlanUseCase,
    observeWorkoutsUseCase: ObserveWorkoutsUseCase,
    private val savePlanUseCase: SavePlanUseCase,
) : ViewModel() {

    private val _plan = MutableStateFlow<TrainingPlan?>(null)
    val plan: StateFlow<TrainingPlan?> = _plan.asStateFlow()

    val workouts: StateFlow<List<Workout>> = observeWorkoutsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _plan.value = getPlanUseCase(planId)
        }
    }

    fun addWorkout(workout: Workout) {
        val current = _plan.value ?: return
        if (current.workouts.any { it.id == workout.id }) return
        persist(current.copy(workouts = current.workouts + workout))
    }

    fun removeWorkout(workoutId: Long) {
        val current = _plan.value ?: return
        persist(current.copy(workouts = current.workouts.filterNot { it.id == workoutId }))
    }

    private fun persist(plan: TrainingPlan) {
        viewModelScope.launch {
            savePlanUseCase(plan)
            refresh()
        }
    }
}
