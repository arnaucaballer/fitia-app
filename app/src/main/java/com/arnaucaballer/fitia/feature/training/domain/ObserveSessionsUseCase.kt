package com.arnaucaballer.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

class ObserveSessionsUseCase(
    private val repository: TrainingRepository,
) {
    operator fun invoke(): Flow<List<TrainingSession>> = repository.observeSessions()
}
