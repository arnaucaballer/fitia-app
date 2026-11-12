package app.fitia.feature.training.domain

/** A muscle trained by an exercise. The first one is the primary muscle. */
data class ExerciseMuscle(
    val id: Long,
    val name: String,
)
