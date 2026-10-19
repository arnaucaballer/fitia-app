package com.arnaucaballer.fitia.feature.training.domain

class StartSessionFromWorkoutUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(workoutId: Long): Long =
        repository.startSessionFromWorkout(workoutId)
}
