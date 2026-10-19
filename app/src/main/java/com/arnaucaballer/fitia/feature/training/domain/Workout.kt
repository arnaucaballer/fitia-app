package com.arnaucaballer.fitia.feature.training.domain

data class Workout(
    val id: Long = 0,
    val name: String,
    val notes: String = "",
    val createdAtEpochMs: Long = 0,
    val exercises: List<WorkoutExercise> = emptyList(),
)
