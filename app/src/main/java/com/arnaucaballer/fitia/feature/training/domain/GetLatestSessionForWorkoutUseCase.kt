package com.arnaucaballer.fitia.feature.training.domain

class GetLatestSessionForWorkoutUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(workoutId: Long): TrainingSession? =
        repository.getLatestSessionForWorkout(workoutId)
}
