package com.arnaucaballer.fitia.feature.training.domain

class DeletePlanUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(id: Long) = repository.deletePlan(id)
}
