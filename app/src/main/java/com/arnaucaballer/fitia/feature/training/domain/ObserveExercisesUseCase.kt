package com.arnaucaballer.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

class ObserveExercisesUseCase(
    private val repository: TrainingRepository,
) {
    operator fun invoke(): Flow<List<Exercise>> = repository.observeExercises()
}
