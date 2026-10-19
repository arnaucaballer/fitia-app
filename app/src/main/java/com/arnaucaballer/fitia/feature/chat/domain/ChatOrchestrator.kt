package com.arnaucaballer.fitia.feature.chat.domain

import com.arnaucaballer.fitia.core.ai.AiSettings
import com.arnaucaballer.fitia.core.ai.ChatCompletionRequest
import com.arnaucaballer.fitia.core.ai.ChatContentPart
import com.arnaucaballer.fitia.core.ai.ChatMessage
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
        val conversation = mutableListOf(system)
        conversation += history.filter { it.role != com.arnaucaballer.fitia.core.ai.ChatRole.System }
        conversation += ChatMessage.user(userText, image)

        var turns = 0
        while (turns < 5) {
            turns += 1
            val result = client.complete(
                settings = settings,
                request = ChatCompletionRequest(
                    model = settings.model,
                    messages = conversation,
                    tools = toolExecutor.definitions,
                ),
            )
            conversation += result.message
            if (result.message.toolCalls.isEmpty()) {
                return conversation.filter { it.role != com.arnaucaballer.fitia.core.ai.ChatRole.System }
            }
            result.message.toolCalls.forEach { call ->
                val output = toolExecutor.execute(call)
                conversation += ChatMessage.tool(
                    toolCallId = call.id,
                    name = call.name,
                    content = output,
                )
            }
        }
        return conversation.filter { it.role != com.arnaucaballer.fitia.core.ai.ChatRole.System } +
            ChatMessage.assistant("He alcanzado el límite de herramientas para este mensaje.")
    }

    private suspend fun buildSystemPrompt(): String {
        val plans = trainingRepository.observePlans().first().joinToString { it.name }
        val workouts = trainingRepository.observeWorkouts().first().joinToString { it.name }
        val profile = nutritionRepository.getProfile()
        val targets = nutritionRepository.getTargets()
        return """
            Eres Fitia, asistente de entrenamiento y alimentación en español.
            Puedes consultar y modificar datos de la app usando tools.
            Sé conciso, claro y confirma las acciones realizadas.
            Resumen:
            - Planes de entreno: ${plans.ifBlank { "ninguno" }}
            - Entrenamientos: ${workouts.ifBlank { "ninguno" }}
            - Perfil: ${profile?.let { "${it.weightKg} kg, ${it.heightCm} cm, ${it.ageYears} años, ${it.goal}" } ?: "sin perfil"}
            - Objetivos del día: ${targets?.let { "${it.calories} kcal / P ${it.proteinGrams} / C ${it.carbsGrams} / G ${it.fatGrams}" } ?: "sin objetivos"}
        """.trimIndent()
    }
}
