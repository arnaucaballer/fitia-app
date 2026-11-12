package app.fitia.feature.chat.domain

import app.fitia.core.ai.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeThreads(): Flow<List<ChatThread>>

    suspend fun latestThread(): ChatThread?

    suspend fun getThread(id: String): ChatThread?

    suspend fun insertThread(thread: ChatThread)

    suspend fun updateThread(thread: ChatThread)

    suspend fun messages(threadId: String): List<ChatMessage>

    suspend fun saveTranscript(threadId: String, messages: List<ChatMessage>, updatedAtEpochMs: Long)

    suspend fun deleteThread(id: String)

    suspend fun restore(thread: ChatThread, messages: List<ChatMessage>)
}
