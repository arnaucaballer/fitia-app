package com.arnaucaballer.fitia.feature.training.domain

class GetSessionUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(id: Long): TrainingSession? = repository.getSession(id)
}
