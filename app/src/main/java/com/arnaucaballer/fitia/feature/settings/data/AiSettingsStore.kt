package com.arnaucaballer.fitia.feature.settings.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.arnaucaballer.fitia.core.ai.AiProviderPreset
import com.arnaucaballer.fitia.core.ai.AiSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiSettingsStore(
    context: Context,
) {
    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        "fitia_ai_settings",
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<AiSettings> = _settings.asStateFlow()

    fun update(settings: AiSettings) {
        prefs.edit()
            .putString(KEY_PROVIDER, settings.provider.name)
            .putString(KEY_API_KEY, settings.apiKey)
            .putString(KEY_MODEL, settings.model)
            .putString(KEY_BASE_URL, settings.baseUrlOverride)
            .apply()
        _settings.value = settings
    }

    private fun read(): AiSettings {
        val providerName = prefs.getString(KEY_PROVIDER, AiProviderPreset.OpenRouter.name)
        val provider = runCatching { AiProviderPreset.valueOf(providerName!!) }
            .getOrDefault(AiProviderPreset.OpenRouter)
        return AiSettings(
            provider = provider,
            apiKey = prefs.getString(KEY_API_KEY, "").orEmpty(),
            model = prefs.getString(KEY_MODEL, provider.defaultModel).orEmpty()
                .ifBlank { provider.defaultModel },
            baseUrlOverride = prefs.getString(KEY_BASE_URL, "").orEmpty(),
        )
    }

    private companion object {
        const val KEY_PROVIDER = "provider"
        const val KEY_API_KEY = "api_key"
        const val KEY_MODEL = "model"
        const val KEY_BASE_URL = "base_url"
    }
}
