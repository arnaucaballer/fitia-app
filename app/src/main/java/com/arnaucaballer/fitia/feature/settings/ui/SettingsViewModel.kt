package com.arnaucaballer.fitia.feature.settings.ui

import androidx.lifecycle.ViewModel
import com.arnaucaballer.fitia.core.ai.AiProviderPreset
import com.arnaucaballer.fitia.core.ai.AiSettings
import com.arnaucaballer.fitia.feature.settings.data.AiSettingsStore
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val store: AiSettingsStore,
) : ViewModel() {
    val settings: StateFlow<AiSettings> = store.settings

    fun save(
        provider: AiProviderPreset,
        apiKey: String,
        model: String,
        baseUrlOverride: String,
    ) {
        val resolvedModel = model.ifBlank { provider.defaultModel }
        store.update(
            AiSettings(
                provider = provider,
                apiKey = apiKey.trim(),
                model = resolvedModel.trim(),
                baseUrlOverride = baseUrlOverride.trim(),
            ),
        )
    }
}
