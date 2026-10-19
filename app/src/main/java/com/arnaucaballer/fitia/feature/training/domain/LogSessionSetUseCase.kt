package com.arnaucaballer.fitia.feature.training.domain

class LogSessionSetUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(
        sessionExerciseId: Long,
        setId: Long?,
        setIndex: Int,
        weightKg: Double?,
        reps: Int?,
        isCompleted: Boolean,
    ): Long = repository.logSet(
        sessionExerciseId = sessionExerciseId,
        setId = setId,
        setIndex = setIndex,
        weightKg = weightKg,
        reps = reps,
        isCompleted = isCompleted,
    )
}
