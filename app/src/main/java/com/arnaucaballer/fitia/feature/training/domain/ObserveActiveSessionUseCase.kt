package com.arnaucaballer.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

class ObserveActiveSessionUseCase(
    private val repository: TrainingRepository,
) {
    operator fun invoke(): Flow<TrainingSession?> = repository.observeActiveSession()
}
