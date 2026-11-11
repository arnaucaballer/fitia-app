package com.arnaucaballer.fitia.feature.training.domain

data class Exercise(
    val id: Long = 0,
    val name: String,
    val muscleId: Long? = null,
    /** Resolved muscle label for UI; falls back to legacy [muscleGroup]. */
    val muscleName: String = "",
    val muscleGroup: String = "",
    val notes: String = "",
    val createdAtEpochMs: Long = 0,
)
