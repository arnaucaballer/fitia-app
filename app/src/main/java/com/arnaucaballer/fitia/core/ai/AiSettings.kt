package com.arnaucaballer.fitia.core.ai

data class AiSettings(
    val provider: AiProviderPreset = AiProviderPreset.OpenRouter,
    val apiKey: String = "",
    val model: String = AiProviderPreset.OpenRouter.defaultModel,
    val baseUrlOverride: String = "",
) {
    val effectiveBaseUrl: String
        get() = baseUrlOverride.ifBlank { provider.baseUrl }

    val effectiveModel: String
        get() {
            val selected = model.trim().ifBlank { provider.defaultModel }
            return if (selected.equals(AiModelCatalog.AUTOMATIC_ID, ignoreCase = true) ||
                selected.equals("automatic", ignoreCase = true) ||
                selected.equals("automático", ignoreCase = true)
            ) {
                provider.autoModel
            } else {
                selected
            }
        }

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()
}
