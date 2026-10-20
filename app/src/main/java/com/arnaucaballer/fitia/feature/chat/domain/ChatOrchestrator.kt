package com.arnaucaballer.fitia.feature.chat.domain

import com.arnaucaballer.fitia.core.ai.AiSettings
import com.arnaucaballer.fitia.core.ai.ChatCompletionRequest
import com.arnaucaballer.fitia.core.ai.ChatContentPart
import com.arnaucaballer.fitia.core.ai.ChatMessage
import com.arnaucaballer.fitia.core.ai.ChatRole
import com.arnaucaballer.fitia.core.ai.OpenAiCompatibleClient
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import com.arnaucaballer.fitia.feature.training.domain.TrainingRepository
import kotlinx.coroutines.flow.first

class ChatOrchestrator(
    private val client: OpenAiCompatibleClient,
    private val toolExecutor: FitiaToolExecutor,
    private val trainingRepository: TrainingRepository,
    private val nutritionRepository: NutritionRepository,
) {
    suspend fun reply(
        settings: AiSettings,
        history: List<ChatMessage>,
        userText: String,
        image: ChatContentPart.ImageBase64? = null,
    ): List<ChatMessage> {
        val system = ChatMessage.system(buildSystemPrompt())
        val prior = history
            .filter { it.role != ChatRole.System }
            .let { trimForApi(it) }
        val conversation = mutableListOf(system)
        conversation += prior
        conversation += ChatMessage.user(userText, image)

        // Enough tool rounds for complex asks; no artificial "message limit".
        var turns = 0
        while (turns < 20) {
            turns += 1
            val result = client.complete(
                settings = settings,
                request = ChatCompletionRequest(
                    model = settings.effectiveModel,
                    messages = conversation,
                    tools = toolExecutor.definitions,
                ),
            )
            conversation += result.message
            if (result.message.toolCalls.isEmpty()) {
                return conversation.filter { it.role != ChatRole.System }
            }
            result.message.toolCalls.forEach { call ->
                val output = runCatching { toolExecutor.execute(call) }
                    .getOrElse { t -> """{"ok":false,"error":"${t.message?.take(200)}"}""" }
                conversation += ChatMessage.tool(
                    toolCallId = call.id,
                    name = call.name,
                    content = output,
                )
            }
        }
        return conversation.filter { it.role != ChatRole.System } +
            ChatMessage.assistant(
                "He hecho muchas acciones en este mensaje. Si necesitas más, pregunta otra vez.",
            )
    }

    /**
     * Keep the full conversation in the ViewModel UI, but only send a recent
     * window to the API so long chats don't blow the context window.
     */
    private fun trimForApi(messages: List<ChatMessage>): List<ChatMessage> {
        if (messages.size <= MaxApiMessages) return messages
        val kept = messages.takeLast(MaxApiMessages).toMutableList()
        // Don't start mid tool-call chain.
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

    private suspend fun buildSystemPrompt(): String {
        val plans = trainingRepository.observePlans().first().joinToString { it.name }
        val workouts = trainingRepository.observeGenericWorkouts().first().joinToString { it.name }
        val profile = nutritionRepository.getProfile()
        val targets = nutritionRepository.getTargets()
        return """
            Eres Fitia, asistente de entrenamiento y alimentación en español.
            Puedes consultar y modificar datos de la app usando tools.
            Sé claro y confirma las acciones realizadas.
            El usuario puede chatear sin límite de mensajes.

            Autonomía:
            - Si el usuario pide que seas autónomo, actúes solo, o dice cosas como "hazlo tú",
              "decide tú", "modo autónomo": actúa con las tools sin preguntar de más. Elige
              opciones razonables, ejecuta y resume lo hecho.
            - Si NO pide autonomía: haz todas las preguntas necesarias para ese caso concreto
              antes de cambiar datos importantes (objetivo, plan, macros, entrenos). Pregunta
              lo suficiente para saber qué es exactamente lo mejor aplicado a su situación.
              No asumas género, nivel, restricciones o preferencias sin preguntar.

            Macros y nutrición: sé lo más preciso posible (valores específicos, no redondeos vagos).

            Resumen:
            - Planes de entreno: ${plans.ifBlank { "ninguno" }}
            - Entrenamientos genéricos: ${workouts.ifBlank { "ninguno" }}
            - Perfil: ${profile?.let { "${it.weightKg} kg, ${it.heightCm} cm, ${it.ageYears} años, ${it.goal}" } ?: "sin perfil"}
            - Objetivos del día: ${targets?.let { "${it.calories} kcal / P ${it.proteinGrams} / C ${it.carbsGrams} / G ${it.fatGrams}" } ?: "sin objetivos"}
        """.trimIndent()
    }

    companion object {
        private const val MaxApiMessages = 40
    }
}
