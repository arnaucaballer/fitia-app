package com.arnaucaballer.fitia.feature.training.domain

data class Workout(
    val id: Long = 0,
    val name: String,
    val notes: String = "",
    /** Null = generic library workout. Non-null = exclusive to that plan. */
    val planId: Long? = null,
    val createdAtEpochMs: Long = 0,
    val exercises: List<WorkoutExercise> = emptyList(),
) {
    val isGeneric: Boolean get() = planId == null
}
