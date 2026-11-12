package app.fitia.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitia.core.ai.ChatMessage
import app.fitia.core.ai.ChatRole
import app.fitia.feature.chat.domain.ChatOrchestrator
import app.fitia.feature.chat.domain.ChatRepository
import app.fitia.feature.chat.domain.ChatThread
import app.fitia.feature.chat.domain.FoodPhotoEstimate
import app.fitia.feature.chat.domain.FoodPhotoEstimator
import app.fitia.feature.nutrition.domain.MealEntry
import app.fitia.feature.nutrition.domain.NutritionRepository
import app.fitia.feature.settings.data.AiSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.util.UUID

class ChatViewModel(
    private val settingsStore: AiSettingsStore,
    private val orchestrator: ChatOrchestrator,
    private val foodPhotoEstimator: FoodPhotoEstimator,
    private val nutritionRepository: NutritionRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val gate = Mutex()
    private var resumed = false
    private var currentThreadId: String? = null

    /** Full API history (user/assistant/tool) of the open chat. */
    private val history = mutableListOf<ChatMessage>()

    private val _messages = MutableStateFlow<List<UiChatMessage>>(emptyList())
    val messages: StateFlow<List<UiChatMessage>> = _messages.asStateFlow()

    private val _title = MutableStateFlow<String?>(null)
    val title: StateFlow<String?> = _title.asStateFlow()

    val threads: StateFlow<List<ChatThread>> = chatRepository.observeThreads()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _pendingFood = MutableStateFlow<FoodPhotoEstimate?>(null)
    val pendingFood: StateFlow<FoodPhotoEstimate?> = _pendingFood.asStateFlow()

    private val _undoPrompts = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val undoPrompts: SharedFlow<Int> = _undoPrompts.asSharedFlow()

    private var undoToken = 0
    private val pendingUndos = ArrayDeque<DeletedChat>()

    val settingsConfigured: StateFlow<Boolean> = settingsStore.settings
        .map { it.isConfigured }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = settingsStore.settings.value.isConfigured,
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            gate.withLock {
                if (resumed) return@withLock
                val latest = chatRepository.latestThread()
                if (latest != null) {
                    apply(latest, chatRepository.messages(latest.id))
                }
                resumed = true
            }
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isLoading.value) return
        if (!settingsStore.settings.value.isConfigured) {
            _error.value = "missing_api_key"
            return
        }
        _error.value = null
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (threadId, prior) = gate.withLock {
                    resumed = true
                    val id = ensureThread(trimmed)
                    id to history.toList()
                }
                persist(threadId, prior + ChatMessage.user(trimmed))
                runCatching {
                    orchestrator.reply(
                        settings = settingsStore.settings.value,
                        history = prior,
                        userText = trimmed,
                    )
                }.onSuccess { updated ->
                    persist(threadId, updated)
                }.onFailure { throwable ->
                    _error.value = throwable.message ?: "error"
                    persist(
                        threadId,
                        prior + ChatMessage.user(trimmed) + ChatMessage.assistant(
                            "No he podido responder ahora. Inténtalo de nuevo.",
                        ),
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startNewChat() {
        if (_isLoading.value) return
        viewModelScope.launch(Dispatchers.IO) {
            gate.withLock {
                resumed = true
                currentThreadId = null
                history.clear()
                _messages.value = emptyList()
                _title.value = null
            }
        }
    }

    fun open(threadId: String) {
        if (_isLoading.value) return
        viewModelScope.launch(Dispatchers.IO) {
            gate.withLock {
                resumed = true
                val thread = chatRepository.getThread(threadId) ?: return@withLock
                apply(thread, chatRepository.messages(threadId))
            }
        }
    }

    fun rename(thread: ChatThread, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val current = chatRepository.getThread(thread.id) ?: return@launch
            chatRepository.updateThread(
                current.copy(title = trimmed, titleEdited = true),
            )
            gate.withLock {
                if (currentThreadId == thread.id) _title.value = trimmed
            }
        }
    }

    fun togglePin(thread: ChatThread) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = chatRepository.getThread(thread.id) ?: return@launch
            chatRepository.updateThread(current.copy(pinned = !current.pinned))
        }
    }

    fun delete(thread: ChatThread) {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = chatRepository.messages(thread.id)
            chatRepository.deleteThread(thread.id)
            val token = gate.withLock {
                undoToken += 1
                val next = undoToken
                pendingUndos.addLast(DeletedChat(next, thread, messages))
                while (pendingUndos.size > 8) pendingUndos.removeFirst()
                if (currentThreadId == thread.id) {
                    currentThreadId = null
                    history.clear()
                    _messages.value = emptyList()
                    _title.value = null
                }
                next
            }
            _undoPrompts.emit(token)
        }
    }

    fun undo(token: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val snapshot = gate.withLock {
                val found = pendingUndos.firstOrNull { it.token == token } ?: return@withLock null
                pendingUndos.remove(found)
                found
            } ?: return@launch
            chatRepository.restore(snapshot.thread, snapshot.messages)
            gate.withLock {
                if (currentThreadId == null) {
                    apply(snapshot.thread, snapshot.messages)
                }
            }
        }
    }

    fun analyzeFoodPhoto(mimeType: String, base64: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
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
                appendAssistant("He añadido ${estimate.name} a $mealName.")
            }.onFailure { throwable ->
                _error.value = throwable.message ?: "error"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private suspend fun appendAssistant(text: String) {
        val (threadId, prior) = gate.withLock {
            resumed = true
            val id = ensureThread(text)
            id to history.toList()
        }
        persist(threadId, prior + ChatMessage.assistant(text))
    }

    private suspend fun ensureThread(firstMessage: String): String {
        currentThreadId?.let { return it }
        val now = System.currentTimeMillis()
        val thread = ChatThread(
            id = UUID.randomUUID().toString(),
            title = titleFrom(firstMessage),
            titleEdited = false,
            pinned = false,
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
        )
        chatRepository.insertThread(thread)
        currentThreadId = thread.id
        _title.value = thread.title
        return thread.id
    }

    private fun apply(thread: ChatThread, messages: List<ChatMessage>) {
        currentThreadId = thread.id
        history.clear()
        history.addAll(messages)
        _messages.value = toUi(messages)
        _title.value = thread.title
    }

    private suspend fun persist(threadId: String, messages: List<ChatMessage>) {
        val kept = trimStored(messages)
        chatRepository.saveTranscript(threadId, kept, System.currentTimeMillis())
        gate.withLock {
            if (currentThreadId == threadId) {
                history.clear()
                history.addAll(kept)
                _messages.value = toUi(kept)
            }
        }
    }

    private fun trimStored(messages: List<ChatMessage>): List<ChatMessage> {
        val filtered = messages.filter { it.role != ChatRole.System }
        if (filtered.size <= MaxStoredMessages) return filtered
        val kept = filtered.takeLast(MaxStoredMessages).toMutableList()
        while (kept.isNotEmpty() && kept.first().role == ChatRole.Tool) {
            kept.removeAt(0)
        }
        if (kept.isNotEmpty() && kept.first().role == ChatRole.Assistant && kept.first().toolCalls.isNotEmpty()) {
            kept.removeAt(0)
            while (kept.isNotEmpty() && kept.first().role == ChatRole.Tool) {
                kept.removeAt(0)
            }
        }
        return kept
    }

    private fun toUi(messages: List<ChatMessage>): List<UiChatMessage> {
        return messages.mapIndexedNotNull { index, message ->
            val visible = (message.role == ChatRole.User || message.role == ChatRole.Assistant) &&
                (message.toolCalls.isEmpty() || message.text.isNotBlank())
            if (!visible) return@mapIndexedNotNull null
            UiChatMessage(
                id = "$index-${message.role}-${message.toolCallId.orEmpty()}-${message.text.hashCode()}",
                isUser = message.role == ChatRole.User,
                text = message.text.ifBlank { "(acción realizada)" },
            )
        }
    }

    private fun titleFrom(text: String): String {
        val line = text.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        if (line.isEmpty()) return "Nuevo chat"
        return if (line.length <= 42) line else line.take(41).trimEnd() + "…"
    }

    private data class DeletedChat(
        val token: Int,
        val thread: ChatThread,
        val messages: List<ChatMessage>,
    )

    private companion object {
        const val MaxStoredMessages = 120
    }
}
