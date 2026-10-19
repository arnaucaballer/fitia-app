package com.arnaucaballer.fitia.feature.training.domain

class GetPlanUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(id: Long): TrainingPlan? = repository.getPlan(id)
}
