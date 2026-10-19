package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macro_targets")
data class MacroTargetsEntity(
    @PrimaryKey val id: Int = 1,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val maintenanceCalories: Int,
    val calorieAdjustment: Int,
    val proteinPerKg: Double,
    val fatPerKg: Double,
    val manualOverride: Boolean = false,
)
