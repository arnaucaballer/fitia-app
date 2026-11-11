package com.arnaucaballer.fitia.feature.training.domain

class SetActiveTrainingPlanUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(planId: Long?) {
        repository.setActivePlan(planId)
    }
}
