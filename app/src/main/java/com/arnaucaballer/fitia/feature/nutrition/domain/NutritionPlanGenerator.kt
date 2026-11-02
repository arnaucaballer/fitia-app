package com.arnaucaballer.fitia.feature.nutrition.domain

import com.arnaucaballer.fitia.core.ai.ChatCompletionRequest
import com.arnaucaballer.fitia.core.ai.ChatContentPart
import com.arnaucaballer.fitia.core.ai.ChatMessage
import com.arnaucaballer.fitia.core.ai.OpenAiCompatibleClient
import com.arnaucaballer.fitia.feature.settings.data.AiSettingsStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class BodyPhotoInput(
    val mimeType: String,
    val base64: String,
    val label: String,
)

class NutritionPlanGenerator(
    private val client: OpenAiCompatibleClient,
    private val settingsStore: AiSettingsStore,
    private val nutritionRepository: NutritionRepository,
    private val recalculateTargetsUseCase: RecalculateTargetsUseCase,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun generateFromFormAndPhotos(
        profile: BodyProfile,
        photos: List<BodyPhotoInput>,
        extraNotes: String,
    ): Long {
        val settings = settingsStore.settings.value
        if (!settings.isConfigured) error("Falta la clave de API")

        nutritionRepository.saveProfile(profile)
        val baseline = MacroCalculator.suggestTargets(profile)

        val formSummary = buildString {
            appendLine("Peso: ${profile.weightKg} kg")
            appendLine("Altura: ${profile.heightCm} cm")
            appendLine("Edad: ${profile.ageYears}")
            appendLine("Sexo: ${profile.sex}")
            appendLine("Actividad: ${profile.activityLevel}")
            appendLine("Objetivo: ${profile.goal}")
            appendLine("Baseline calculado: ${baseline.calories} kcal, P ${baseline.proteinGrams}, C ${baseline.carbsGrams}, G ${baseline.fatGrams}, agua ${baseline.waterMlGoal} ml")
            if (extraNotes.isNotBlank()) appendLine("Notas: $extraNotes")
        }

        val prompt = """
            Eres un nutricionista. Con el formulario y las fotos del físico, ajusta el plan.
            Responde SOLO JSON:
            {
              "plan_name":"string",
              "calories":number,
              "protein_g":number,
              "carbs_g":number,
              "fat_g":number,
              "water_ml":number,
              "notes":"string",
              "body_assessment":"string"
            }
            Datos:
            $formSummary
            Sin markdown.
        """.trimIndent()

        val parts = mutableListOf<ChatContentPart>(ChatContentPart.Text(prompt))
        photos.forEach { photo ->
            parts += ChatContentPart.Text("Foto ${photo.label}:")
            parts += ChatContentPart.ImageBase64(photo.mimeType, photo.base64)
        }

        val result = client.complete(
            settings = settings,
            request = ChatCompletionRequest(
                model = settings.effectiveModel,
                messages = listOf(
                    ChatMessage.system("Nutricionista clínico. Solo JSON."),
                    ChatMessage(
                        role = com.arnaucaballer.fitia.core.ai.ChatRole.User,
                        parts = parts,
                    ),
                ),
            ),
        )
        val raw = result.message.text.trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val obj = json.parseToJsonElement(raw).jsonObject

        val calories = obj["calories"]?.jsonPrimitive?.intOrNull ?: baseline.calories
        val protein = obj["protein_g"]?.jsonPrimitive?.doubleOrNull ?: baseline.proteinGrams
        val carbs = obj["carbs_g"]?.jsonPrimitive?.doubleOrNull ?: baseline.carbsGrams
        val fat = obj["fat_g"]?.jsonPrimitive?.doubleOrNull ?: baseline.fatGrams
        val water = obj["water_ml"]?.jsonPrimitive?.intOrNull ?: baseline.waterMlGoal
        val notes = buildString {
            obj["body_assessment"]?.jsonPrimitive?.content?.let { appendLine(it) }
            obj["notes"]?.jsonPrimitive?.content?.let { append(it) }
        }.trim()

        val targets = baseline.copy(
            calories = calories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            waterMlGoal = water,
            manualOverride = true,
        )
        nutritionRepository.saveTargets(targets)

        return nutritionRepository.savePlan(
            NutritionPlan(
                name = obj["plan_name"]?.jsonPrimitive?.content ?: "Plan nutricional",
                calories = calories,
                proteinGrams = protein,
                carbsGrams = carbs,
                fatGrams = fat,
                notes = notes,
            ),
        )
    }

    suspend fun generateQuickFromProfile(): Long {
        val profile = nutritionRepository.getProfile()
            ?: error("Completa el perfil primero")
        recalculateTargetsUseCase(profile, force = true)
        val targets = nutritionRepository.getTargets() ?: MacroCalculator.suggestTargets(profile)
        return nutritionRepository.savePlan(
            NutritionPlan(
                name = "Plan ${profile.goal.name.lowercase()}",
                calories = targets.calories,
                proteinGrams = targets.proteinGrams,
                carbsGrams = targets.carbsGrams,
                fatGrams = targets.fatGrams,
                notes = "Generado desde perfil. Agua objetivo: ${targets.waterMlGoal} ml",
            ),
        )
    }
}
