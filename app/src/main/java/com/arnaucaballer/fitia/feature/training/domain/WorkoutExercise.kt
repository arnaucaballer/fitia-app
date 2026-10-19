package com.arnaucaballer.fitia.feature.training.domain

data class WorkoutExercise(
    val id: Long = 0,
    val exerciseId: Long,
    val exerciseName: String,
    val sortOrder: Int,
    val setTargets: List<SetTarget> = emptyList(),
)
