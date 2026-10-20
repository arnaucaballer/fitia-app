package com.arnaucaballer.fitia.core.ai

data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val tools: List<AiToolDefinition> = emptyList(),
    /** Caps completion size so OpenRouter free/low-credit accounts don't reject the request. */
    val maxTokens: Int = 4_096,
)
