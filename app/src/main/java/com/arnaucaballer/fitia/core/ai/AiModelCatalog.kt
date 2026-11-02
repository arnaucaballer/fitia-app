package com.arnaucaballer.fitia.core.ai

data class AiModelOption(
    val id: String,
    val label: String,
)

object AiModelCatalog {
    const val AUTOMATIC_ID = "auto"

    fun optionsFor(provider: AiProviderPreset): List<AiModelOption> = when (provider) {
        AiProviderPreset.OpenRouter -> listOf(
            AiModelOption(AUTOMATIC_ID, "Automático"),
            AiModelOption("google/gemini-2.5-flash", "Gemini 2.5 Flash"),
            AiModelOption("google/gemini-2.5-pro", "Gemini 2.5 Pro"),
            AiModelOption("openai/gpt-4o-mini", "GPT-4o mini"),
            AiModelOption("openai/gpt-4o", "GPT-4o"),
            AiModelOption("anthropic/claude-sonnet-4", "Claude Sonnet 4"),
            AiModelOption("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet"),
            AiModelOption("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B"),
            AiModelOption("deepseek/deepseek-chat", "DeepSeek Chat"),
            AiModelOption("qwen/qwen-2.5-72b-instruct", "Qwen 2.5 72B"),
        )
        AiProviderPreset.OpenCode -> listOf(
            AiModelOption(AUTOMATIC_ID, "Automático"),
            AiModelOption("kimi-k2.6", "Kimi K2.6"),
            AiModelOption("gpt-4o-mini", "GPT-4o mini"),
            AiModelOption("gpt-4o", "GPT-4o"),
            AiModelOption("claude-sonnet-4", "Claude Sonnet 4"),
        )
    }

    fun labelFor(provider: AiProviderPreset, modelId: String): String =
        optionsFor(provider).firstOrNull { it.id == modelId }?.label
            ?: modelId.ifBlank { "Automático" }
}
