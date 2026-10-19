package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plan_workouts",
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("planId"), Index("workoutId")],
)
data class PlanWorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val workoutId: Long,
    val sortOrder: Int,
)
