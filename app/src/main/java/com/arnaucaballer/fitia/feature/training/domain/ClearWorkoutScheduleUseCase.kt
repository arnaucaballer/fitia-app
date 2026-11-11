package com.arnaucaballer.fitia.feature.training.domain

class ClearWorkoutScheduleUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(dayEpochDay: Long) {
        repository.clearWorkoutSchedule(dayEpochDay)
    }
}
