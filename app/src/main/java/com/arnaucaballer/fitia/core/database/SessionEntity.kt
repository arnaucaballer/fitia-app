package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("workoutId")],
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long? = null,
    val name: String,
    val status: String,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long? = null,
)
