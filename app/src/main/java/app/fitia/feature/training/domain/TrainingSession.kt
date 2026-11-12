package app.fitia.feature.training.domain

data class TrainingSession(
    val id: Long = 0,
    val workoutId: Long? = null,
    val name: String,
    val status: SessionStatus,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long? = null,
    val exercises: List<SessionExercise> = emptyList(),
)
