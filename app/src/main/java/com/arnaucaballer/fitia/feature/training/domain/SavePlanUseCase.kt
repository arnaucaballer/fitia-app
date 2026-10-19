package com.arnaucaballer.fitia.feature.training.domain

class SavePlanUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(plan: TrainingPlan): Long = repository.savePlan(plan)
}
