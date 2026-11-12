package app.fitia.feature.training.domain

class SaveExerciseUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(exercise: Exercise): Long = repository.saveExercise(exercise)
}
