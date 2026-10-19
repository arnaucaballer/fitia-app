package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String = "",
    val servingGrams: Double,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val source: String = "custom",
    val barcode: String = "",
)
