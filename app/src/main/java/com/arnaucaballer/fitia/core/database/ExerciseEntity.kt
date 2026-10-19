package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String = "",
    val notes: String = "",
    val createdAtEpochMs: Long,
)
