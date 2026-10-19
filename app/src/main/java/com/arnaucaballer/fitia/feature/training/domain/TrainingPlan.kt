package com.arnaucaballer.fitia.feature.training.domain

data class TrainingPlan(
    val id: Long = 0,
    val name: String,
    val notes: String = "",
    val createdAtEpochMs: Long = 0,
    val workouts: List<Workout> = emptyList(),
)
