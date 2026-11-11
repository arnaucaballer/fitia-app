package com.arnaucaballer.fitia.feature.training.domain

class StartBlankSessionUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(name: String): Long = repository.startBlankSession(name)
}
