package com.arnaucaballer.fitia.feature.training.domain

data class SessionSet(
    val id: Long = 0,
    val setIndex: Int,
    val weightKg: Double? = null,
    val reps: Int? = null,
    val isCompleted: Boolean = false,
)
