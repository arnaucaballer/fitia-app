package com.arnaucaballer.fitia.feature.training.domain

class DeleteWorkoutUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(id: Long) = repository.deleteWorkout(id)
}
