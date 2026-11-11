package com.arnaucaballer.fitia.feature.settings.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.arnaucaballer.fitia.core.ai.AiModelCatalog
import com.arnaucaballer.fitia.core.ai.AiProviderPreset
import com.arnaucaballer.fitia.core.ai.AiSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiSettingsStore(
    context: Context,
) {
    private val prefs = openPrefs(context.applicationContext)

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
        return try {
            val providerName = prefs.getString(KEY_PROVIDER, AiProviderPreset.OpenRouter.name)
            val provider = runCatching { AiProviderPreset.valueOf(providerName!!) }
                .getOrDefault(AiProviderPreset.OpenRouter)
            AiSettings(
                provider = provider,
                apiKey = prefs.getString(KEY_API_KEY, "").orEmpty(),
                model = prefs.getString(KEY_MODEL, AiModelCatalog.AUTOMATIC_ID).orEmpty()
                    .ifBlank { AiModelCatalog.AUTOMATIC_ID },
                baseUrlOverride = prefs.getString(KEY_BASE_URL, "").orEmpty(),
            )
        } catch (_: Throwable) {
            AiSettings()
        }
    }

    private companion object {
        const val PREFS_NAME = "fitia_ai_settings"
        const val KEY_PROVIDER = "provider"

        fun openPrefs(context: Context): SharedPreferences {
            return try {
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            } catch (_: Throwable) {
                context.getSharedPreferences("${PREFS_NAME}_plain", Context.MODE_PRIVATE)
            }
        }
        const val KEY_API_KEY = "api_key"
        const val KEY_MODEL = "model"
        const val KEY_BASE_URL = "base_url"
    }
}
