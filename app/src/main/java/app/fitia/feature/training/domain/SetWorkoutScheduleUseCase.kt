package app.fitia.feature.training.domain

class SetWorkoutScheduleUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(dayEpochDay: Long, workoutId: Long) {
        repository.setWorkoutSchedule(dayEpochDay, workoutId)
    }
}
