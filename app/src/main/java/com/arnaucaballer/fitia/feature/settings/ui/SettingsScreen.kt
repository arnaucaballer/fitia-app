package com.arnaucaballer.fitia.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.core.ai.AiModelCatalog
import com.arnaucaballer.fitia.core.ai.AiProviderPreset
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: SettingsViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            SettingsViewModel(store = c.aiSettingsStore)
        },
    )
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var provider by rememberSaveable { mutableStateOf(AiProviderPreset.OpenRouter.name) }
    var apiKey by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf(AiModelCatalog.AUTOMATIC_ID) }
    var baseUrl by rememberSaveable { mutableStateOf("") }
    var customModel by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(settings) {
        provider = settings.provider.name
        apiKey = settings.apiKey
        model = settings.model.ifBlank { AiModelCatalog.AUTOMATIC_ID }
        baseUrl = settings.baseUrlOverride
        customModel = AiModelCatalog.optionsFor(settings.provider).none { it.id == model }
    }

    val selectedProvider = runCatching { AiProviderPreset.valueOf(provider) }
        .getOrDefault(AiProviderPreset.OpenRouter)
    val modelOptions = AiModelCatalog.optionsFor(selectedProvider)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(24.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.settings_provider))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AiProviderPreset.entries.forEach { preset ->
                    FilterChip(
                        selected = provider == preset.name,
                        onClick = {
                            provider = preset.name
                            if (!customModel) {
                                model = AiModelCatalog.AUTOMATIC_ID
                            }
                        },
                        label = { Text(preset.displayName) },
                    )
                }
            }

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text(stringResource(R.string.settings_api_key)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
            )

            Text(stringResource(R.string.settings_model), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.settings_model_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                modelOptions.forEach { option ->
                    FilterChip(
                        selected = !customModel && model == option.id,
                        onClick = {
                            customModel = false
                            model = option.id
                        },
                        label = { Text(option.label) },
                    )
                }
                FilterChip(
                    selected = customModel,
                    onClick = { customModel = true },
                    label = { Text(stringResource(R.string.settings_model_custom)) },
                )
            }
            if (customModel) {
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text(stringResource(R.string.settings_model_id)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text(stringResource(R.string.settings_base_url)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Text(stringResource(R.string.settings_api_key_hint))

            Button(
                onClick = {
                    viewModel.save(
                        provider = selectedProvider,
                        apiKey = apiKey,
                        model = if (customModel) model else model.ifBlank { AiModelCatalog.AUTOMATIC_ID },
                        baseUrlOverride = baseUrl,
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}
