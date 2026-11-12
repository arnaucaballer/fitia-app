package app.fitia.feature.training.domain

class AddExerciseToSessionUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(sessionId: Long, exerciseId: Long): Long =
        repository.addExerciseToSession(sessionId, exerciseId)
}
