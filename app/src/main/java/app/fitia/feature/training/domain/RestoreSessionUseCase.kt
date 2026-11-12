package app.fitia.feature.training.domain

class RestoreSessionUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(session: TrainingSession) = repository.restoreSession(session)
}
