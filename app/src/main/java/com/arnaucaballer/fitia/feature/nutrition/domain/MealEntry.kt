package com.arnaucaballer.fitia.feature.nutrition.domain

data class MealEntry(
    val id: Long = 0,
    val mealId: Long,
    val foodItemId: Long? = null,
    val name: String,
    val grams: Double,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
)
