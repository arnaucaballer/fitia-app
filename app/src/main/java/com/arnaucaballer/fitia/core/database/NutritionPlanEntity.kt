package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_plans")
data class NutritionPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val notes: String = "",
    val createdAtEpochMs: Long,
)
