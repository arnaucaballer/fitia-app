package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Marks a day whose meals were created once, so deleting them does not recreate the defaults. */
@Entity(tableName = "day_meal_state")
data class DayMealStateEntity(
    @PrimaryKey val dayEpochDay: Long,
    val initialized: Boolean,
)
