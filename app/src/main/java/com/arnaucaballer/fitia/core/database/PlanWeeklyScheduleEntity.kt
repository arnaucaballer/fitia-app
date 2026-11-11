package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "plan_weekly_schedule",
    primaryKeys = ["planId", "dayOfWeek"],
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
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("workoutId")],
)
data class PlanWeeklyScheduleEntity(
    val planId: Long,
    /** ISO-8601: 1 = lunes … 7 = domingo */
    val dayOfWeek: Int,
    val workoutId: Long?,
)
