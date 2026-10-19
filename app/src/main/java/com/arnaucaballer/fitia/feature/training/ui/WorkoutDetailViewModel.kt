package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.Exercise
import com.arnaucaballer.fitia.feature.training.domain.GetWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveExercisesUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.SetTarget
import com.arnaucaballer.fitia.feature.training.domain.StartSessionFromWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.Workout
import com.arnaucaballer.fitia.feature.training.domain.WorkoutExercise
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(
    private val workoutId: Long,
    private val getWorkoutUseCase: GetWorkoutUseCase,
    observeExercisesUseCase: ObserveExercisesUseCase,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val startSessionFromWorkoutUseCase: StartSessionFromWorkoutUseCase,
) : ViewModel() {

    private val _workout = MutableStateFlow<Workout?>(null)
    val workout: StateFlow<Workout?> = _workout.asStateFlow()

    val exercises: StateFlow<List<Exercise>> = observeExercisesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _sessionStarted = MutableSharedFlow<Long>()
    val sessionStarted: SharedFlow<Long> = _sessionStarted.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _workout.value = getWorkoutUseCase(workoutId)
        }
    }

    fun addExercise(exercise: Exercise) {
        val current = _workout.value ?: return
        val next = current.copy(
            exercises = current.exercises + WorkoutExercise(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                sortOrder = current.exercises.size,
                setTargets = listOf(SetTarget(setIndex = 0, targetReps = 10)),
            ),
        )
        persist(next)
    }

    fun addSet(sortOrder: Int) {
        val current = _workout.value ?: return
        val exercises = current.exercises.map { item ->
            if (item.sortOrder != sortOrder) {
                item
            } else {
                item.copy(
                    setTargets = item.setTargets + SetTarget(
                        setIndex = item.setTargets.size,
                        targetReps = 10,
                    ),
                )
            }
        }
        persist(current.copy(exercises = exercises))
    }

    fun updateSet(
        sortOrder: Int,
        setIndex: Int,
        weightKg: Double?,
        reps: Int?,
    ) {
        val current = _workout.value ?: return
        val exercises = current.exercises.map { item ->
            if (item.sortOrder != sortOrder) {
                item
            } else {
                item.copy(
                    setTargets = item.setTargets.map { target ->
                        if (target.setIndex == setIndex) {
                            target.copy(targetWeightKg = weightKg, targetReps = reps)
                        } else {
                            target
                        }
                    },
                )
            }
        }
        persist(current.copy(exercises = exercises))
    }

    fun removeExercise(sortOrder: Int) {
        val current = _workout.value ?: return
        val exercises = current.exercises
            .filterNot { it.sortOrder == sortOrder }
            .mapIndexed { index, item -> item.copy(sortOrder = index) }
        persist(current.copy(exercises = exercises))
    }

    fun startSession() {
        viewModelScope.launch {
            val sessionId = startSessionFromWorkoutUseCase(workoutId)
            _sessionStarted.emit(sessionId)
        }
    }

    private fun persist(workout: Workout) {
        viewModelScope.launch {
            saveWorkoutUseCase(workout)
            refresh()
        }
    }
}
