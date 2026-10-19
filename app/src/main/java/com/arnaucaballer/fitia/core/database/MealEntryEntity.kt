package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_entries",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = FoodItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("mealId"), Index("foodItemId")],
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val foodItemId: Long? = null,
    val nameSnapshot: String,
    val grams: Double,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
)
