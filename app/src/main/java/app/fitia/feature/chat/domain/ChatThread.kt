package app.fitia.feature.chat.domain

data class ChatThread(
    val id: String,
    val title: String,
    val titleEdited: Boolean,
    val pinned: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
