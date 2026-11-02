package com.arnaucaballer.fitia.core.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
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
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class OpenAiCompatibleClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
    }

    private val client = HttpClient(Android) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 120_000
        }
    }

    suspend fun complete(settings: AiSettings, request: ChatCompletionRequest): ChatCompletionResult {
        if (!settings.isConfigured) {
            error("Falta la clave de API. Ábrela en Ajustes.")
        }
        val body = buildJsonObject {
            put("model", request.model)
            put("messages", messagesToJson(request.messages))
            put("max_tokens", request.maxTokens)
            if (request.tools.isNotEmpty()) {
                put("tools", toolsToJson(request.tools))
            }
        }
        val httpResponse = client.post("${settings.effectiveBaseUrl.trimEnd('/')}/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${settings.apiKey.trim()}")
            header("HTTP-Referer", "https://github.com/arnaucaballer/fitia-app")
            header("X-Title", "Fitia")
            setBody(body)
        }
        val raw = httpResponse.bodyAsText()
        if (!httpResponse.status.isSuccess()) {
            val detail = runCatching {
                json.parseToJsonElement(raw).jsonObject["error"]
                    ?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
            }.getOrNull()
            error(detail ?: "Error API ${httpResponse.status.value}: ${raw.take(240)}")
        }

        val response = json.decodeFromString(ApiResponse.serializer(), raw)
        val message = response.choices.firstOrNull()?.message
            ?: error("La API no devolvió ninguna respuesta.")
        val toolCalls = message.toolCalls.orEmpty().mapNotNull { call ->
            val name = call.function?.name ?: return@mapNotNull null
            val args = when (val arguments = call.function.arguments) {
                null -> "{}"
                is JsonPrimitive -> arguments.content
                else -> arguments.toString()
            }
            ChatToolCall(
                id = call.id ?: "tool_${name}",
                name = name,
                argumentsJson = args.ifBlank { "{}" },
            )
        }
        val text = message.contentText()
        return ChatCompletionResult(
            message = ChatMessage.assistant(
                text = text,
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
                            if (message.text.isNotBlank()) {
                                put("content", message.text)
                            }
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
    val content: JsonElement? = null,
    @SerialName("tool_calls") val toolCalls: List<ApiToolCall>? = null,
)

@Serializable
private data class ApiToolCall(
    val id: String? = null,
    val function: ApiFunctionCall? = null,
)

@Serializable
private data class ApiFunctionCall(
    val name: String? = null,
    val arguments: JsonElement? = null,
)

private fun ApiMessage.contentText(): String {
    val element = content ?: return ""
    return when (element) {
        is JsonPrimitive -> element.contentOrNull.orEmpty()
        is JsonArray -> element.mapNotNull { part ->
            part.jsonObject["text"]?.jsonPrimitive?.contentOrNull
        }.joinToString("\n")
        is JsonObject -> element["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
        else -> element.toString()
    }
}
