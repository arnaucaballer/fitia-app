package com.arnaucaballer.fitia.core.ai

data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val tools: List<AiToolDefinition> = emptyList(),
)
