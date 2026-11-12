package app.fitia.core.ai

data class ChatToolCall(
    val id: String,
    val name: String,
    val argumentsJson: String,
)
