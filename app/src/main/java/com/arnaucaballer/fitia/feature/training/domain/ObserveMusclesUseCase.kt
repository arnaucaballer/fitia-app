package com.arnaucaballer.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

class ObserveMusclesUseCase(
    private val repository: TrainingRepository,
) {
    operator fun invoke(): Flow<List<Muscle>> = repository.observeMuscles()
}
