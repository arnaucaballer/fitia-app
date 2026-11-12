package app.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

class ObserveActiveTrainingPlanUseCase(
    private val repository: TrainingRepository,
) {
    operator fun invoke(): Flow<Long?> = repository.observeActivePlanId()
}
