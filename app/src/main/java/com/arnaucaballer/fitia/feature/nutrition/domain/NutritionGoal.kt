package com.arnaucaballer.fitia.feature.nutrition.domain

enum class NutritionGoal(val defaultCalorieAdjustment: Int) {
    LoseFat(-500),
    Maintain(0),
    GainMuscle(300),
}
