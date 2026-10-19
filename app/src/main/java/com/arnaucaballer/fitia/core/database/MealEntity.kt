package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meals",
    indices = [Index("dayEpochDay")],
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayEpochDay: Long,
    val name: String,
    val sortOrder: Int,
)
