package app.fitia.feature.training.domain

class ClearCalendarUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke() {
        repository.clearCalendarAssignments()
    }
}
