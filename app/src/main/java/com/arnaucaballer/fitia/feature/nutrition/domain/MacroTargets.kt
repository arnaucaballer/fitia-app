package com.arnaucaballer.fitia.feature.nutrition.domain

data class MacroTargets(
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val maintenanceCalories: Int,
    val calorieAdjustment: Int,
    val proteinPerKg: Double,
    val fatPerKg: Double,
    val manualOverride: Boolean = false,
    val waterMlGoal: Int = 3000,
)
