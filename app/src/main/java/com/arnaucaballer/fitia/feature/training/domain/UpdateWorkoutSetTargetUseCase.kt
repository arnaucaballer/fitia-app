package com.arnaucaballer.fitia.feature.training.domain

class UpdateWorkoutSetTargetUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(
        workoutExerciseId: Long,
        setIndex: Int,
        weightKg: Double?,
        reps: Int?,
    ): Boolean = repository.updateWorkoutSetTarget(
        workoutExerciseId = workoutExerciseId,
        setIndex = setIndex,
        weightKg = weightKg,
        reps = reps,
    )
}
