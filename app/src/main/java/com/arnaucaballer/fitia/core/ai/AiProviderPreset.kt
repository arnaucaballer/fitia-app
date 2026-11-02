package com.arnaucaballer.fitia.core.ai

enum class AiProviderPreset(
    val displayName: String,
    val baseUrl: String,
    val defaultModel: String,
    /** Resolved when the user picks "Automático". */
    val autoModel: String,
) {
    OpenRouter(
        displayName = "OpenRouter",
        baseUrl = "https://openrouter.ai/api/v1",
        defaultModel = AiModelCatalog.AUTOMATIC_ID,
        autoModel = "openrouter/auto",
    ),
    OpenCode(
        displayName = "OpenCode",
        baseUrl = "https://opencode.ai/inference/openai/v1",
        defaultModel = AiModelCatalog.AUTOMATIC_ID,
        autoModel = "kimi-k2.6",
    ),
}
