package app.fitia.feature.chat.data

import app.fitia.core.ai.ChatMessage
import app.fitia.core.ai.ChatRole
import app.fitia.core.ai.ChatToolCall
import app.fitia.core.database.ChatDao
import app.fitia.core.database.ChatMessageEntity
import app.fitia.core.database.ChatThreadEntity
import app.fitia.feature.chat.domain.ChatRepository
import app.fitia.feature.chat.domain.ChatThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class RoomChatRepository(
    private val dao: ChatDao,
) : ChatRepository {
    override fun observeThreads(): Flow<List<ChatThread>> =
        dao.observeThreads().map { threads -> threads.map { it.toDomain() } }

    override suspend fun latestThread(): ChatThread? = dao.latestThread()?.toDomain()

    override suspend fun getThread(id: String): ChatThread? = dao.getThread(id)?.toDomain()

    override suspend fun insertThread(thread: ChatThread) {
        dao.insertThread(thread.toEntity())
    }

    override suspend fun updateThread(thread: ChatThread) {
        dao.updateThread(thread.toEntity())
    }

    override suspend fun messages(threadId: String): List<ChatMessage> =
        dao.getMessages(threadId).mapNotNull { it.toMessage() }

    override suspend fun saveTranscript(
        threadId: String,
        messages: List<ChatMessage>,
        updatedAtEpochMs: Long,
    ) {
        val thread = dao.getThread(threadId) ?: return
        dao.saveTranscript(
            thread = thread.copy(updatedAtEpochMs = updatedAtEpochMs),
            messages = messages.mapIndexedNotNull { index, message -> message.toEntity(threadId, index) },
        )
    }

    override suspend fun deleteThread(id: String) {
        dao.deleteThread(id)
    }

    override suspend fun restore(thread: ChatThread, messages: List<ChatMessage>) {
        dao.restore(
            thread = thread.toEntity(),
            messages = messages.mapIndexedNotNull { index, message -> message.toEntity(thread.id, index) },
        )
    }
}

private fun ChatThreadEntity.toDomain() = ChatThread(
    id = id,
    title = title,
    titleEdited = titleEdited,
    pinned = pinned,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs,
)

private fun ChatThread.toEntity() = ChatThreadEntity(
    id = id,
    title = title,
    titleEdited = titleEdited,
    pinned = pinned,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs,
)

private fun ChatMessage.toEntity(threadId: String, sortOrder: Int): ChatMessageEntity? {
    if (role == ChatRole.System) return null
    return ChatMessageEntity(
        threadId = threadId,
        sortOrder = sortOrder,
        role = role.name,
        text = text,
        toolCallsJson = encodeToolCalls(toolCalls),
        toolCallId = toolCallId,
        toolName = name,
    )
}

private fun ChatMessageEntity.toMessage(): ChatMessage? {
    val chatRole = runCatching { ChatRole.valueOf(role) }.getOrNull() ?: return null
    return when (chatRole) {
        ChatRole.System -> null
        ChatRole.User -> ChatMessage.user(text)
        ChatRole.Assistant -> ChatMessage.assistant(text, decodeToolCalls(toolCallsJson))
        ChatRole.Tool -> ChatMessage.tool(
            toolCallId = toolCallId.orEmpty(),
            name = toolName.orEmpty(),
            content = text,
        )
    }
}

private fun encodeToolCalls(calls: List<ChatToolCall>): String {
    if (calls.isEmpty()) return ""
    val array = JSONArray()
    calls.forEach { call ->
        array.put(
            JSONObject()
                .put("id", call.id)
                .put("name", call.name)
                .put("arguments", call.argumentsJson),
        )
    }
    return array.toString()
}

private fun decodeToolCalls(raw: String): List<ChatToolCall> {
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        List(array.length()) { index ->
            val obj = array.getJSONObject(index)
            ChatToolCall(
                id = obj.optString("id"),
                name = obj.optString("name"),
                argumentsJson = obj.optString("arguments"),
            )
        }
    }.getOrDefault(emptyList())
}
