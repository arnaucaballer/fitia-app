package com.arnaucaballer.fitia.feature.training.domain

class DeleteExerciseUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(id: Long) = repository.deleteExercise(id)
}
