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
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun estimate(
        settings: AiSettings,
        mimeType: String,
        base64Data: String,
    ): FoodPhotoEstimate {
        val prompt = """
            Analiza la foto del alimento y responde SOLO con JSON válido:
            {"name":"string","grams":number,"calories":number,"protein_g":number,"carbs_g":number,"fat_g":number}
            Estima una ración razonable. Sin markdown.
        """.trimIndent()
        val result = client.complete(
            settings = settings,
            request = ChatCompletionRequest(
                model = settings.model,
                messages = listOf(
                    ChatMessage.system("Eres un estimador nutricional. Solo JSON."),
                    ChatMessage.user(
                        text = prompt,
                        image = ChatContentPart.ImageBase64(mimeType, base64Data),
                    ),
                ),
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
