package app.fitia.feature.training.domain

class DeleteSessionUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(id: Long) = repository.deleteSession(id)
}
