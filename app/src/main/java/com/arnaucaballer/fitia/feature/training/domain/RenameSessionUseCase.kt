package com.arnaucaballer.fitia.feature.training.domain

class RenameSessionUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(id: Long, name: String) = repository.renameSession(id, name)
}
