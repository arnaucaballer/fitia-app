package com.arnaucaballer.fitia.feature.training.domain

data class SessionExercise(
    val id: Long = 0,
    val exerciseId: Long? = null,
    val name: String,
    val sortOrder: Int,
    val sets: List<SessionSet> = emptyList(),
)
