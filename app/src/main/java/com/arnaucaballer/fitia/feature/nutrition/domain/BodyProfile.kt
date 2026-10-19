package com.arnaucaballer.fitia.feature.nutrition.domain

data class BodyProfile(
    val weightKg: Double,
    val heightCm: Double,
    val ageYears: Int,
    val sex: Sex,
    val activityLevel: ActivityLevel,
    val goal: NutritionGoal,
    val updatedAtEpochMs: Long = 0,
)
