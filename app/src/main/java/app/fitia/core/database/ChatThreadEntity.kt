package app.fitia.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val titleEdited: Boolean,
    val pinned: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
