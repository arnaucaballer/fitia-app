package com.arnaucaballer.fitia.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.core.ai.ChatMessage
import com.arnaucaballer.fitia.core.ai.ChatRole
import com.arnaucaballer.fitia.feature.chat.domain.ChatOrchestrator
import com.arnaucaballer.fitia.feature.chat.domain.FoodPhotoEstimate
import com.arnaucaballer.fitia.feature.chat.domain.FoodPhotoEstimator
import com.arnaucaballer.fitia.feature.nutrition.domain.MealEntry
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import com.arnaucaballer.fitia.feature.settings.data.AiSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class ChatViewModel(
    private val settingsStore: AiSettingsStore,
    private val orchestrator: ChatOrchestrator,
    private val foodPhotoEstimator: FoodPhotoEstimator,
    private val nutritionRepository: NutritionRepository,
) : ViewModel() {

    /** Full API history (user/assistant/tool). Grows without a hard chat cap. */
    private val history = mutableListOf<ChatMessage>()

    private val _messages = MutableStateFlow<List<UiChatMessage>>(emptyList())
    val messages: StateFlow<List<UiChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _pendingFood = MutableStateFlow<FoodPhotoEstimate?>(null)
    val pendingFood: StateFlow<FoodPhotoEstimate?> = _pendingFood.asStateFlow()

    val settingsConfigured: StateFlow<Boolean> = settingsStore.settings
        .map { it.isConfigured }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = settingsStore.settings.value.isConfigured,
        )

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isLoading.value) return
        viewModelScope.launch {
            val settings = settingsStore.settings.value
            if (!settings.isConfigured) {
                _error.value = "missing_api_key"
                return@launch
            }
            _error.value = null
            _isLoading.value = true
            val userUi = UiChatMessage(
                id = UUID.randomUUID().toString(),
                isUser = true,
                text = trimmed,
            )
            _messages.value = _messages.value + userUi

            runCatching {
                orchestrator.reply(
                    settings = settings,
                    history = history.toList(),
                    userText = trimmed,
                )
            }.onSuccess { updated ->
                history.clear()
                history.addAll(updated)
                val visible = updated
                    .filter { it.role == ChatRole.User || it.role == ChatRole.Assistant }
                    .filter { it.toolCalls.isEmpty() || it.text.isNotBlank() }
                    .map { msg ->
                        UiChatMessage(
                            id = UUID.randomUUID().toString(),
                            isUser = msg.role == ChatRole.User,
                            text = msg.text.ifBlank { "(acción realizada)" },
                        )
                    }
                _messages.value = visible
            }.onFailure { throwable ->
                _error.value = throwable.message ?: "error"
                _messages.value = _messages.value + UiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "No he podido responder ahora. Inténtalo de nuevo.",
                )
            }
            _isLoading.value = false
        }
    }

    fun analyzeFoodPhoto(mimeType: String, base64: String) {
        viewModelScope.launch {
            val settings = settingsStore.settings.value
            if (!settings.isConfigured) {
                _error.value = "missing_api_key"
                return@launch
            }
            _isLoading.value = true
            _error.value = null
            runCatching {
                foodPhotoEstimator.estimate(settings, mimeType, base64)
            }.onSuccess { estimate ->
                _pendingFood.value = estimate
            }.onFailure { throwable ->
                _error.value = throwable.message ?: "error"
            }
            _isLoading.value = false
        }
    }

    fun dismissFoodEstimate() {
        _pendingFood.value = null
    }

    fun confirmFoodEstimate(estimate: FoodPhotoEstimate, mealName: String) {
        viewModelScope.launch {
            runCatching {
                val day = LocalDate.now().toEpochDay()
                nutritionRepository.ensureDefaultMeals(day)
                val dayLog = nutritionRepository.observeDayLog(day).first()
                val mealId = dayLog.meals.firstOrNull { it.name.equals(mealName, true) }?.id
                    ?: nutritionRepository.addMeal(day, mealName)
                nutritionRepository.addEntry(
                    MealEntry(
                        mealId = mealId,
                        name = estimate.name,
                        grams = estimate.grams,
                        calories = estimate.calories,
                        proteinGrams = estimate.proteinGrams,
                        carbsGrams = estimate.carbsGrams,
                        fatGrams = estimate.fatGrams,
                    ),
                )
            }.onSuccess {
                _pendingFood.value = null
                _messages.value = _messages.value + UiChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "He añadido ${estimate.name} a $mealName.",
                )
            }.onFailure { throwable ->
                _error.value = throwable.message ?: "error"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
