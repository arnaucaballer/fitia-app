package com.arnaucaballer.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

class ObserveWorkoutsUseCase(
    private val repository: TrainingRepository,
) {
    operator fun invoke(): Flow<List<Workout>> = repository.observeWorkouts()
}
