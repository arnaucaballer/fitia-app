package com.arnaucaballer.fitia.feature.training.domain

import com.arnaucaballer.fitia.core.ai.ChatCompletionRequest
import com.arnaucaballer.fitia.core.ai.ChatMessage
import com.arnaucaballer.fitia.core.ai.OpenAiCompatibleClient
import com.arnaucaballer.fitia.feature.settings.data.AiSettingsStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class TrainingPlanGenerator(
    private val client: OpenAiCompatibleClient,
    private val settingsStore: AiSettingsStore,
    private val trainingRepository: TrainingRepository,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun generate(goal: String, daysPerWeek: Int, level: String): Long {
        val settings = settingsStore.settings.value
        if (!settings.isConfigured) error("Falta la clave de API")
        val prompt = """
            Crea un plan de entrenamiento. Responde SOLO JSON:
            {
              "plan_name":"string",
              "notes":"string",
              "workouts":[
                {
                  "name":"string",
                  "notes":"string",
                  "exercises":[
                    {"name":"string","muscle_group":"string","sets":[{"weight_kg":null,"reps":10}]}
                  ]
                }
              ]
            }
            Objetivo: $goal. Días/semana: $daysPerWeek. Nivel: $level.
            Usa ejercicios comunes de gimnasio. 3-6 series por ejercicio. Sin markdown.
        """.trimIndent()
        val result = client.complete(
            settings = settings,
            request = ChatCompletionRequest(
                model = settings.model,
                messages = listOf(
                    ChatMessage.system("Eres un entrenador personal. Solo JSON válido."),
                    ChatMessage.user(prompt),
                ),
            ),
        )
        val raw = stripJson(result.message.text)
        val root = json.parseToJsonElement(raw).jsonObject
        val planName = root["plan_name"]?.jsonPrimitive?.content ?: "Plan IA"
        val notes = root["notes"]?.jsonPrimitive?.content.orEmpty()
        val workoutObjs = root["workouts"]?.jsonArray.orEmpty()

        val planId = trainingRepository.savePlan(
            TrainingPlan(name = planName, notes = notes, workouts = emptyList()),
        )

        val ownedWorkouts = workoutObjs.map { workoutEl ->
            val w = workoutEl.jsonObject
            val exercises = w["exercises"]?.jsonArray.orEmpty().map { exEl ->
                val ex = exEl.jsonObject
                val name = ex["name"]?.jsonPrimitive?.content ?: "Ejercicio"
                val muscle = ex["muscle_group"]?.jsonPrimitive?.content.orEmpty()
                val exerciseId = trainingRepository.saveExercise(
                    Exercise(name = name, muscleGroup = muscle),
                )
                val sets = ex["sets"]?.jsonArray.orEmpty().mapIndexed { index, setEl ->
                    val s = setEl.jsonObject
                    SetTarget(
                        setIndex = index,
                        targetWeightKg = s["weight_kg"]?.jsonPrimitive?.content?.toDoubleOrNull(),
                        targetReps = s["reps"]?.jsonPrimitive?.intOrNull,
                    )
                }.ifEmpty {
                    listOf(SetTarget(setIndex = 0, targetReps = 10))
                }
                WorkoutExercise(
                    exerciseId = exerciseId,
                    exerciseName = name,
                    sortOrder = 0,
                    setTargets = sets,
                )
            }
            // Also keep a generic copy in the library for reuse
            trainingRepository.saveWorkout(
                Workout(
                    name = w["name"]?.jsonPrimitive?.content ?: "Entreno",
                    notes = w["notes"]?.jsonPrimitive?.content.orEmpty(),
                    planId = null,
                    exercises = exercises,
                ),
            )
            val ownedId = trainingRepository.saveWorkout(
                Workout(
                    name = w["name"]?.jsonPrimitive?.content ?: "Entreno",
                    notes = w["notes"]?.jsonPrimitive?.content.orEmpty(),
                    planId = planId,
                    exercises = exercises.map { e ->
                        e.copy(id = 0, setTargets = e.setTargets.map { it.copy(id = 0) })
                    },
                ),
            )
            trainingRepository.getWorkout(ownedId)!!
        }

        return trainingRepository.savePlan(
            TrainingPlan(id = planId, name = planName, notes = notes, workouts = ownedWorkouts),
        )
    }

    private fun stripJson(text: String): String =
        text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
}
