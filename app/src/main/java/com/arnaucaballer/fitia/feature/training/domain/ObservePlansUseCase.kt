package com.arnaucaballer.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

class ObservePlansUseCase(
    private val repository: TrainingRepository,
) {
    operator fun invoke(): Flow<List<TrainingPlan>> = repository.observePlans()
}
