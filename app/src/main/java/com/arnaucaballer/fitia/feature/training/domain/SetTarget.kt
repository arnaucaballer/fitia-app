package com.arnaucaballer.fitia.feature.training.domain

data class SetTarget(
    val id: Long = 0,
    val setIndex: Int,
    val targetWeightKg: Double? = null,
    val targetReps: Int? = null,
)
