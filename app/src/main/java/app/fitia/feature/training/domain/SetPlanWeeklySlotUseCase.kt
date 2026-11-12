package app.fitia.feature.training.domain

class SetPlanWeeklySlotUseCase(
    private val repository: TrainingRepository,
) {
    suspend operator fun invoke(planId: Long, dayOfWeek: Int, workoutId: Long?) {
        repository.setPlanWeeklySlot(planId, dayOfWeek, workoutId)
        repository.syncWeeklyPlanToCalendar(planId, weeksAhead = 12)
    }
}
