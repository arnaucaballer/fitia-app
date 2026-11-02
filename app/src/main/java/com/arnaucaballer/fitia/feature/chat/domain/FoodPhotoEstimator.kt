package com.arnaucaballer.fitia.feature.chat.domain

import com.arnaucaballer.fitia.core.ai.AiSettings
import com.arnaucaballer.fitia.core.ai.ChatCompletionRequest
import com.arnaucaballer.fitia.core.ai.ChatContentPart
import com.arnaucaballer.fitia.core.ai.ChatMessage
import com.arnaucaballer.fitia.core.ai.OpenAiCompatibleClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FoodPhotoEstimator(
    private val client: OpenAiCompatibleClient,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun estimate(
        settings: AiSettings,
        mimeType: String,
        base64Data: String,
    ): FoodPhotoEstimate {
        val prompt = """
            Analiza la foto del alimento con la máxima precisión posible.
            Identifica cada componente visible, estima gramos reales de la ración en la foto
            y calcula macros usando densidades nutricionales típicas (no redondees a números
            genéricos; sé específico, p. ej. 327 kcal no 300).
            Responde SOLO con JSON válido:
            {"name":"string","grams":number,"calories":number,"protein_g":number,"carbs_g":number,"fat_g":number}
            Sin markdown.
        """.trimIndent()
        return completeEstimate(
            settings = settings,
            system = "Eres un estimador nutricional clínico. Máxima precisión en macros. Solo JSON.",
            userText = prompt,
            image = ChatContentPart.ImageBase64(mimeType, base64Data),
        )
    }

    /**
     * Recalculates macros after the user edits name/grams (and optionally keeps the photo).
     */
    suspend fun recalculate(
        settings: AiSettings,
        name: String,
        grams: Double,
        notes: String = "",
        mimeType: String? = null,
        base64Data: String? = null,
    ): FoodPhotoEstimate {
        val prompt = buildString {
            appendLine("Recalcula macros con máxima precisión para:")
            appendLine("Nombre/descripción: $name")
            appendLine("Gramos: $grams")
            if (notes.isNotBlank()) appendLine("Notas del usuario: $notes")
            appendLine("Si hay foto, úsala para afinar la estimación.")
            appendLine("Sé específico (no redondees a valores genéricos).")
            appendLine("""Responde SOLO JSON: {"name":"string","grams":number,"calories":number,"protein_g":number,"carbs_g":number,"fat_g":number}""")
        }
        val image = if (mimeType != null && !base64Data.isNullOrBlank()) {
            ChatContentPart.ImageBase64(mimeType, base64Data)
        } else {
            null
        }
        val estimated = completeEstimate(
            settings = settings,
            system = "Eres un estimador nutricional clínico. Máxima precisión. Solo JSON.",
            userText = prompt,
            image = image,
        )
        return if (grams > 0) estimated.copy(grams = grams) else estimated
    }

    private suspend fun completeEstimate(
        settings: AiSettings,
        system: String,
        userText: String,
        image: ChatContentPart.ImageBase64?,
    ): FoodPhotoEstimate {
        val result = client.complete(
            settings = settings,
            request = ChatCompletionRequest(
                model = settings.effectiveModel,
                messages = listOf(
                    ChatMessage.system(system),
                    ChatMessage.user(text = userText, image = image),
                ),
                maxTokens = 1_024,
            ),
        )
        val raw = result.message.text
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val obj = json.parseToJsonElement(raw).jsonObject
        return FoodPhotoEstimate(
            name = obj["name"]?.jsonPrimitive?.content ?: "Alimento",
            grams = obj["grams"]?.jsonPrimitive?.doubleOrNull ?: 100.0,
            calories = obj["calories"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            proteinGrams = obj["protein_g"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            carbsGrams = obj["carbs_g"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            fatGrams = obj["fat_g"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
        )
    }
}
