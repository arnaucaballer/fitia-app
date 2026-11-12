package app.fitia.feature.training.domain

class SaveMuscleUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(muscle: Muscle): Long = repository.saveMuscle(muscle)
}
