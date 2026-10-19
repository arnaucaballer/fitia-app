package com.arnaucaballer.fitia.core.ai

data class AiSettings(
    val provider: AiProviderPreset = AiProviderPreset.OpenRouter,
    val apiKey: String = "",
    val model: String = AiProviderPreset.OpenRouter.defaultModel,
    val baseUrlOverride: String = "",
) {
    val effectiveBaseUrl: String
        get() = baseUrlOverride.ifBlank { provider.baseUrl }

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()
}
