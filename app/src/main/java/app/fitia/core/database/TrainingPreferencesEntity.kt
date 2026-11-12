package app.fitia.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_preferences",
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["activePlanId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class TrainingPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val activePlanId: Long?,
)
