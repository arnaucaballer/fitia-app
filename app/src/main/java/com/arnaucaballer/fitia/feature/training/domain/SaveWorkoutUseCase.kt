package com.arnaucaballer.fitia.feature.training.domain

class SaveWorkoutUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(workout: Workout): Long = repository.saveWorkout(workout)
}
