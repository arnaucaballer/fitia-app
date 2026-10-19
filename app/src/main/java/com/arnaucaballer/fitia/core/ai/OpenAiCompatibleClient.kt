package com.arnaucaballer.fitia.core.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class OpenAiCompatibleClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun complete(settings: AiSettings, request: ChatCompletionRequest): ChatCompletionResult {
        if (!settings.isConfigured) {
            error("API key missing")
        }
        val body = buildJsonObject {
            put("model", request.model)
            put("messages", messagesToJson(request.messages))
            if (request.tools.isNotEmpty()) {
                put("tools", toolsToJson(request.tools))
            }
        }
        val response: ApiResponse = client.post("${settings.effectiveBaseUrl.trimEnd('/')}/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${settings.apiKey}")
            header("HTTP-Referer", "https://github.com/arnaucaballer/fitia-app")
            header("X-Title", "Fitia")
            setBody(body)
        }.body()

        val choice = response.choices.firstOrNull()
            ?: error("Empty completion response")
        val message = choice.message
        val toolCalls = message.toolCalls.orEmpty().map { call ->
            ChatToolCall(
                id = call.id,
                name = call.function.name,
                argumentsJson = call.function.arguments,
            )
        }
        return ChatCompletionResult(
            message = ChatMessage.assistant(
                text = message.content.orEmpty(),
                toolCalls = toolCalls,
            ),
        )
    }

    private fun messagesToJson(messages: List<ChatMessage>): JsonArray = buildJsonArray {
        messages.forEach { message ->
            add(
                buildJsonObject {
                    put("role", message.role.toApiRole())
                    when {
                        message.role == ChatRole.Tool -> {
                            put("tool_call_id", message.toolCallId.orEmpty())
                            put("content", message.text)
                            message.name?.let { put("name", it) }
                        }
                        message.toolCalls.isNotEmpty() -> {
                            put("content", message.text)
                            put(
                                "tool_calls",
                                buildJsonArray {
                                    message.toolCalls.forEach { call ->
                                        add(
                                            buildJsonObject {
                                                put("id", call.id)
                                                put("type", "function")
                                                put(
                                                    "function",
                                                    buildJsonObject {
                                                        put("name", call.name)
                                                        put("arguments", call.argumentsJson)
                                                    },
                                                )
                                            },
                                        )
                                    }
                                },
                            )
                        }
                        message.parts.size == 1 && message.parts.first() is ChatContentPart.Text -> {
                            put("content", message.text)
                        }
                        else -> {
                            put(
                                "content",
                                buildJsonArray {
                                    message.parts.forEach { part ->
                                        when (part) {
                                            is ChatContentPart.Text -> add(
                                                buildJsonObject {
                                                    put("type", "text")
                                                    put("text", part.text)
                                                },
                                            )
                                            is ChatContentPart.ImageBase64 -> add(
                                                buildJsonObject {
                                                    put("type", "image_url")
                                                    put(
                                                        "image_url",
                                                        buildJsonObject {
                                                            put(
                                                                "url",
                                                                "data:${part.mimeType};base64,${part.base64Data}",
                                                            )
                                                        },
                                                    )
                                                },
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                },
            )
        }
    }

    private fun toolsToJson(tools: List<AiToolDefinition>): JsonArray = buildJsonArray {
        tools.forEach { tool ->
            val parameters = json.parseToJsonElement(tool.parametersJsonSchema)
            add(
                buildJsonObject {
                    put("type", "function")
                    put(
                        "function",
                        buildJsonObject {
                            put("name", tool.name)
                            put("description", tool.description)
                            put("parameters", parameters)
                        },
                    )
                },
            )
        }
    }

    private fun ChatRole.toApiRole(): String = when (this) {
        ChatRole.System -> "system"
        ChatRole.User -> "user"
        ChatRole.Assistant -> "assistant"
        ChatRole.Tool -> "tool"
    }

    fun close() {
        client.close()
    }
}

@Serializable
private data class ApiResponse(
    val choices: List<ApiChoice> = emptyList(),
)

@Serializable
private data class ApiChoice(
    val message: ApiMessage,
)

@Serializable
private data class ApiMessage(
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ApiToolCall>? = null,
)

@Serializable
private data class ApiToolCall(
    val id: String,
    val function: ApiFunctionCall,
)

@Serializable
private data class ApiFunctionCall(
    val name: String,
    val arguments: String,
)
