package app.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

class ObserveWorkoutSchedulesUseCase(
    private val repository: TrainingRepository,
) {
    operator fun invoke(): Flow<List<WorkoutSchedule>> = repository.observeWorkoutSchedules()
}
