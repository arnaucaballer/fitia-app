package app.fitia.feature.settings.ui

import androidx.lifecycle.ViewModel
import app.fitia.core.ai.AiModelCatalog
import app.fitia.core.ai.AiProviderPreset
import app.fitia.core.ai.AiSettings
import app.fitia.feature.settings.data.AiSettingsStore
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
        val resolvedModel = model.ifBlank { AiModelCatalog.AUTOMATIC_ID }
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
