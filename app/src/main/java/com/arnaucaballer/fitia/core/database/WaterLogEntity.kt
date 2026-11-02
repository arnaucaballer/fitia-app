package com.arnaucaballer.fitia.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "water_logs",
    indices = [Index("dayEpochDay")],
)
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayEpochDay: Long,
    val amountMl: Int,
    val createdAtEpochMs: Long,
)
