package com.arnaucaballer.fitia.feature.nutrition.domain

data class NutritionPlan(
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val notes: String = "",
    val createdAtEpochMs: Long = 0,
)
