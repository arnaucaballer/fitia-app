package app.fitia.feature.training.domain

data class PlanWeeklySlot(
    val planId: Long,
    val dayOfWeek: Int,
    val workoutId: Long?,
    val workoutName: String?,
)
