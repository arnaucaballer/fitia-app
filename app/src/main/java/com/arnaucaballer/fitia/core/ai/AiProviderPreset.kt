package com.arnaucaballer.fitia.core.ai

enum class AiProviderPreset(
    val displayName: String,
    val baseUrl: String,
    val defaultModel: String,
) {
    OpenRouter(
        displayName = "OpenRouter",
        baseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "google/gemini-2.5-flash",
    ),
    OpenCode(
        displayName = "OpenCode",
        baseUrl = "https://opencode.ai/inference/openai/v1",
        defaultModel = "kimi-k2.6",
    ),
}
