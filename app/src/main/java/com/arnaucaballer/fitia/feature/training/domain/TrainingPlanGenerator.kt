package com.arnaucaballer.fitia.feature.training.domain

import com.arnaucaballer.fitia.core.ai.ChatCompletionRequest
import com.arnaucaballer.fitia.core.ai.ChatMessage
import com.arnaucaballer.fitia.core.ai.OpenAiCompatibleClient
import com.arnaucaballer.fitia.feature.settings.data.AiSettingsStore
import com.arnaucaballer.fitia.feature.training.domain.Exercise
import com.arnaucaballer.fitia.feature.training.domain.ExerciseMuscle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
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

    suspend fun generateDraft(
        goal: String,
        daysPerWeek: Int,
        level: String,
        userDescription: String = "",
    ): TrainingPlan {
        val raw = requestPlanJson(goal, daysPerWeek, level, userDescription)
        return parseGeneratedPlan(raw)
    }

    suspend fun persistGeneratedPlan(draft: TrainingPlan): Long {
        val planId = trainingRepository.savePlan(
            TrainingPlan(name = draft.name.trim(), notes = draft.notes.trim(), workouts = emptyList()),
        )

        val ownedWorkouts = draft.workouts.map { workout ->
            val exercises = workout.exercises.map { item ->
                val resolved = item.muscleGroups.mapNotNull { group ->
                    trainingRepository.findOrCreateMuscleByName(group)?.let { muscleId ->
                        ExerciseMuscle(id = muscleId, name = group.trim())
                    }
                }
                val exerciseId = trainingRepository.saveExercise(
                    Exercise(
                        name = item.exerciseName,
                        muscleId = resolved.firstOrNull()?.id,
                        muscleGroup = resolved.firstOrNull()?.name.orEmpty(),
                        muscles = resolved,
                    ),
                )
                item.copy(exerciseId = exerciseId)
            }
            val ownedId = trainingRepository.saveWorkout(
                Workout(
                    name = workout.name,
                    notes = workout.notes,
                    planId = planId,
                    exercises = exercises,
                ),
            )
            trainingRepository.getWorkout(ownedId)!!
        }

        return trainingRepository.savePlan(
            TrainingPlan(
                id = planId,
                name = draft.name.trim(),
                notes = draft.notes.trim(),
                workouts = ownedWorkouts,
            ),
        )
    }

    private suspend fun requestPlanJson(
        goal: String,
        daysPerWeek: Int,
        level: String,
        userDescription: String,
    ): String {
        val settings = settingsStore.settings.value
        if (!settings.isConfigured) error("Falta la clave de API")
        val extra = userDescription.trim().takeIf { it.isNotEmpty() }?.let {
            "Descripción del usuario: $it."
        }.orEmpty()
        val prompt = """
            Crea un plan de entrenamiento. Responde SOLO JSON:
            {
              "plan_name":"string",
              "notes":"string descripción general del plan en español",
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
            $extra
            Usa ejercicios comunes de gimnasio. 3-6 series por ejercicio. Sin markdown.
        """.trimIndent()
        val result = client.complete(
            settings = settings,
            request = ChatCompletionRequest(
                model = settings.effectiveModel,
                messages = listOf(
                    ChatMessage.system("Eres un entrenador personal. Solo JSON válido."),
                    ChatMessage.user(prompt),
                ),
            ),
        )
        return stripJson(result.message.text)
    }

    private fun parseGeneratedPlan(raw: String): TrainingPlan {
        val root = json.parseToJsonElement(raw).jsonObject
        val planName = root["plan_name"]?.jsonPrimitive?.content ?: "Plan IA"
        val notes = root["notes"]?.jsonPrimitive?.content.orEmpty()
        val workoutObjs = root["workouts"]?.jsonArray.orEmpty()

        val workouts = workoutObjs.map { workoutEl ->
            val w = workoutEl.jsonObject
            val exercises = w["exercises"]?.jsonArray.orEmpty().mapIndexed { index, exEl ->
                val ex = exEl.jsonObject
                val name = ex["name"]?.jsonPrimitive?.content ?: "Ejercicio"
                val muscle = ex["muscle_group"]?.jsonPrimitive?.content.orEmpty()
                val secondary = ex["muscle_groups"]?.jsonArray.orEmpty().mapNotNull {
                    it.jsonPrimitive.contentOrNull?.trim()
                }.filterNot { it.equals(muscle.trim(), ignoreCase = true) }
                val sets = ex["sets"]?.jsonArray.orEmpty().mapIndexed { setIndex, setEl ->
                    val s = setEl.jsonObject
                    SetTarget(
                        setIndex = setIndex,
                        targetWeightKg = s["weight_kg"]?.jsonPrimitive?.content?.toDoubleOrNull(),
                        targetReps = s["reps"]?.jsonPrimitive?.intOrNull,
                    )
                }.ifEmpty {
                    listOf(SetTarget(setIndex = 0, targetReps = 10))
                }
                WorkoutExercise(
                    exerciseId = 0,
                    exerciseName = name,
                    sortOrder = index,
                    muscleGroup = muscle,
                    muscleGroups = listOf(muscle).filter { it.isNotBlank() } + secondary,
                    setTargets = sets,
                )
            }
            Workout(
                name = w["name"]?.jsonPrimitive?.content ?: "Entreno",
                notes = w["notes"]?.jsonPrimitive?.content.orEmpty(),
                exercises = exercises,
            )
        }

        return TrainingPlan(name = planName, notes = notes, workouts = workouts)
    }

    private fun stripJson(text: String): String =
        text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
}
