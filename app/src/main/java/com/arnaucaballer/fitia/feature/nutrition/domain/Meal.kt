package com.arnaucaballer.fitia.feature.nutrition.domain

data class Meal(
    val id: Long = 0,
    val dayEpochDay: Long,
    val name: String,
    val sortOrder: Int,
    val entries: List<MealEntry> = emptyList(),
)
