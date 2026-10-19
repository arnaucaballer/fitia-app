package com.arnaucaballer.fitia.feature.training.domain

class GetWorkoutUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(id: Long): Workout? = repository.getWorkout(id)
}
