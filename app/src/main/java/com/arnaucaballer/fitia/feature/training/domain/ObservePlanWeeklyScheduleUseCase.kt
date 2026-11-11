package com.arnaucaballer.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

class ObservePlanWeeklyScheduleUseCase(
    private val repository: TrainingRepository,
) {
    operator fun invoke(planId: Long): Flow<List<PlanWeeklySlot>> =
        repository.observePlanWeeklySchedule(planId)
}
