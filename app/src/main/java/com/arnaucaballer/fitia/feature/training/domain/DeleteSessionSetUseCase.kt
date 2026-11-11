package com.arnaucaballer.fitia.feature.training.domain

class DeleteSessionSetUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(setId: Long) {
        repository.deleteSessionSet(setId)
    }
}
