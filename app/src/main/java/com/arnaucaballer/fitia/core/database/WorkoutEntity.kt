package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    indices = [Index("planId")],
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String = "",
    /** Null = generic library workout. Non-null = owned by that training plan. */
    val planId: Long? = null,
    val createdAtEpochMs: Long,
)
