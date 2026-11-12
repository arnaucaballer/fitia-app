package app.fitia.feature.training.domain

import app.fitia.core.ai.ChatCompletionRequest
import app.fitia.core.ai.ChatMessage
import app.fitia.core.ai.OpenAiCompatibleClient
import app.fitia.feature.settings.data.AiSettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SuggestExerciseMusclesUseCase(
    private val client: OpenAiCompatibleClient,
    private val settingsStore: AiSettingsStore,
    private val trainingRepository: TrainingRepository,
) {
    suspend operator fun invoke(exerciseName: String): List<ExerciseMuscle> {
        val trimmed = exerciseName.trim()
        if (trimmed.isEmpty()) return emptyList()
        val existing = trainingRepository.observeMuscles().first()
        if (existing.isEmpty()) return emptyList()
        val names = requestMuscleNames(trimmed, existing.map { it.name })
        return matchExistingMuscles(names, existing, MAX_MUSCLES)
    }

    private suspend fun requestMuscleNames(exerciseName: String, catalog: List<String>): List<String> {
        val settings = settingsStore.settings.value
        val example = catalog.firstOrNull { it.equals("Pectoral mayor", ignoreCase = true) } ?: catalog.first()
        val result = client.complete(
            settings = settings,
            request = ChatCompletionRequest(
                model = settings.effectiveModel,
                maxTokens = 256,
                messages = listOf(
                    ChatMessage.system(
                        "Eres un entrenador. Respondes solo JSON válido, sin markdown.",
                    ),
                    ChatMessage.user(
                        """
                        Ejercicio: "$exerciseName".
                        Di qué músculos trabaja, del principal al menos importante.
                        Usa solo músculos que ya existen, copiando el nombre tal cual.
                        No inventes ninguno que no esté en esta lista:
                        ${catalog.joinToString(", ")}.
                        Responde {"muscles":["$example"]}.
                        Entre 1 y $MAX_MUSCLES músculos. El primero es el principal.
                        """.trimIndent(),
                    ),
                ),
            ),
        )
        return parseSuggestedMuscleNames(result.message.text)
    }

    private companion object {
        const val MAX_MUSCLES = 4
    }
}

internal fun parseSuggestedMuscleNames(raw: String): List<String> {
    val cleaned = raw.trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
    val start = cleaned.indexOf('{')
    val end = cleaned.lastIndexOf('}')
    if (start < 0 || end <= start) return emptyList()
    val root = runCatching {
        muscleJson.parseToJsonElement(cleaned.substring(start, end + 1)).jsonObject
    }.getOrNull() ?: return emptyList()
    return root["muscles"]?.jsonArray.orEmpty().mapNotNull { element ->
        runCatching { element.jsonPrimitive.contentOrNull?.trim()?.takeIf { it.isNotEmpty() } }.getOrNull()
    }
}

private val muscleJson = Json { ignoreUnknownKeys = true; isLenient = true }
