package com.arnaucaballer.fitia.core.ai

data class ChatMessage(
    val role: ChatRole,
    val parts: List<ChatContentPart> = emptyList(),
    val toolCalls: List<ChatToolCall> = emptyList(),
    val toolCallId: String? = null,
    val name: String? = null,
) {
    val text: String
        get() = parts.filterIsInstance<ChatContentPart.Text>().joinToString("\n") { it.text }

    companion object {
        fun system(text: String) = ChatMessage(
            role = ChatRole.System,
            parts = listOf(ChatContentPart.Text(text)),
        )

        fun user(text: String, image: ChatContentPart.ImageBase64? = null) = ChatMessage(
            role = ChatRole.User,
            parts = buildList {
                add(ChatContentPart.Text(text))
                if (image != null) add(image)
            },
        )

        fun assistant(text: String, toolCalls: List<ChatToolCall> = emptyList()) = ChatMessage(
            role = ChatRole.Assistant,
            parts = listOf(ChatContentPart.Text(text)),
            toolCalls = toolCalls,
        )

        fun tool(toolCallId: String, name: String, content: String) = ChatMessage(
            role = ChatRole.Tool,
            parts = listOf(ChatContentPart.Text(content)),
            toolCallId = toolCallId,
            name = name,
        )
    }
}
