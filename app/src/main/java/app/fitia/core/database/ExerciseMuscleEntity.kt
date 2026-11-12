package app.fitia.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "exercise_muscles",
    primaryKeys = ["exerciseId", "muscleId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MuscleEntity::class,
            parentColumns = ["id"],
            childColumns = ["muscleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("muscleId")],
)
data class ExerciseMuscleEntity(
    val exerciseId: Long,
    val muscleId: Long,
    /** 0 is the primary muscle used for grouping; the rest are secondary. */
    val sortOrder: Int = 0,
)
