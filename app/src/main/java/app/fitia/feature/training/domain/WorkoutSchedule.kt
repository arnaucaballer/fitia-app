package app.fitia.feature.training.domain

data class WorkoutSchedule(
    val dayEpochDay: Long,
    val workoutId: Long,
    val workoutName: String,
)
