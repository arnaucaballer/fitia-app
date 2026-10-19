package com.arnaucaballer.fitia.feature.training.domain

class FinishSessionUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(sessionId: Long) = repository.finishSession(sessionId)
}
