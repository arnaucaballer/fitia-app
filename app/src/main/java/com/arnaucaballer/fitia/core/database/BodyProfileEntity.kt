package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_profile")
data class BodyProfileEntity(
    @PrimaryKey val id: Int = 1,
    val weightKg: Double,
    val heightCm: Double,
    val ageYears: Int,
    val sex: String,
    val activityLevel: String,
    val goal: String,
    val updatedAtEpochMs: Long,
)
