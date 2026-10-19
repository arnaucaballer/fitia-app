package com.arnaucaballer.fitia.feature.chat.domain

import com.arnaucaballer.fitia.core.ai.AiToolDefinition
import com.arnaucaballer.fitia.core.ai.ChatToolCall
import com.arnaucaballer.fitia.feature.nutrition.domain.ActivityLevel
import com.arnaucaballer.fitia.feature.nutrition.domain.BodyProfile
import com.arnaucaballer.fitia.feature.nutrition.domain.FoodItem
import com.arnaucaballer.fitia.feature.nutrition.domain.MacroTargets
import com.arnaucaballer.fitia.feature.nutrition.domain.MealEntry
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionGoal
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionPlan
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import com.arnaucaballer.fitia.feature.nutrition.domain.RecalculateTargetsUseCase
import com.arnaucaballer.fitia.feature.nutrition.domain.SaveManualTargetsUseCase
import com.arnaucaballer.fitia.feature.nutrition.domain.Sex
import com.arnaucaballer.fitia.feature.training.domain.Exercise
import com.arnaucaballer.fitia.feature.training.domain.SetTarget
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.TrainingRepository
import com.arnaucaballer.fitia.feature.training.domain.Workout
import com.arnaucaballer.fitia.feature.training.domain.WorkoutExercise
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.time.LocalDate

class FitiaToolExecutor(
    private val trainingRepository: TrainingRepository,
    private val nutritionRepository: NutritionRepository,
    private val recalculateTargetsUseCase: RecalculateTargetsUseCase,
    private val saveManualTargetsUseCase: SaveManualTargetsUseCase,
) {
    private val json = Json { ignoreUnknownKeys = true }

    val definitions: List<AiToolDefinition> = listOf(
        tool("list_exercises", "Lista los ejercicios guardados.", """{"type":"object","properties":{}}"""),
        tool(
            "create_exercise",
            "Crea un ejercicio.",
            """{"type":"object","properties":{"name":{"type":"string"},"muscle_group":{"type":"string"},"notes":{"type":"string"}},"required":["name"]}""",
        ),
        tool("list_workouts", "Lista entrenamientos.", """{"type":"object","properties":{}}"""),
        tool(
            "create_workout",
            "Crea un entrenamiento con ejercicios y series objetivo.",
            """{"type":"object","properties":{"name":{"type":"string"},"notes":{"type":"string"},"exercises":{"type":"array","items":{"type":"object","properties":{"exercise_id":{"type":"integer"},"sets":{"type":"array","items":{"type":"object","properties":{"weight_kg":{"type":"number"},"reps":{"type":"integer"}}}}},"required":["exercise_id"]}}},"required":["name"]}""",
        ),
        tool("list_training_plans", "Lista planes de entrenamiento.", """{"type":"object","properties":{}}"""),
        tool(
            "create_training_plan",
            "Crea un plan con IDs de entrenamientos.",
            """{"type":"object","properties":{"name":{"type":"string"},"workout_ids":{"type":"array","items":{"type":"integer"}}},"required":["name"]}""",
        ),
        tool(
            "start_session",
            "Empieza una sesión desde un entrenamiento.",
            """{"type":"object","properties":{"workout_id":{"type":"integer"}},"required":["workout_id"]}""",
        ),
        tool(
            "log_set",
            "Registra o actualiza una serie.",
            """{"type":"object","properties":{"session_exercise_id":{"type":"integer"},"set_id":{"type":"integer"},"set_index":{"type":"integer"},"weight_kg":{"type":"number"},"reps":{"type":"integer"},"completed":{"type":"boolean"}},"required":["session_exercise_id","set_index"]}""",
        ),
        tool(
            "finish_session",
            "Termina una sesión.",
            """{"type":"object","properties":{"session_id":{"type":"integer"}},"required":["session_id"]}""",
        ),
        tool("get_nutrition_profile", "Obtiene perfil y objetivos.", """{"type":"object","properties":{}}"""),
        tool(
            "update_nutrition_profile",
            "Actualiza perfil y recalcula macros.",
            """{"type":"object","properties":{"weight_kg":{"type":"number"},"height_cm":{"type":"number"},"age_years":{"type":"integer"},"sex":{"type":"string"},"activity_level":{"type":"string"},"goal":{"type":"string"},"recalculate":{"type":"boolean"}},"required":["weight_kg","height_cm","age_years","sex","activity_level","goal"]}""",
        ),
        tool(
            "set_macro_targets",
            "Fija calorías y macros manualmente.",
            """{"type":"object","properties":{"calories":{"type":"integer"},"protein_g":{"type":"number"},"carbs_g":{"type":"number"},"fat_g":{"type":"number"}},"required":["calories","protein_g","carbs_g","fat_g"]}""",
        ),
        tool(
            "create_nutrition_plan",
            "Guarda un plan de alimentación.",
            """{"type":"object","properties":{"name":{"type":"string"},"calories":{"type":"integer"},"protein_g":{"type":"number"},"carbs_g":{"type":"number"},"fat_g":{"type":"number"}},"required":["name","calories","protein_g","carbs_g","fat_g"]}""",
        ),
        tool(
            "apply_nutrition_plan",
            "Aplica un plan de alimentación.",
            """{"type":"object","properties":{"plan_id":{"type":"integer"}},"required":["plan_id"]}""",
        ),
        tool(
            "add_food_to_meal",
            "Añade un alimento al diario.",
            """{"type":"object","properties":{"meal_name":{"type":"string"},"food_name":{"type":"string"},"grams":{"type":"number"},"calories":{"type":"number"},"protein_g":{"type":"number"},"carbs_g":{"type":"number"},"fat_g":{"type":"number"},"day_epoch_day":{"type":"integer"}},"required":["meal_name","food_name","grams","calories"]}""",
        ),
        tool(
            "create_food",
            "Crea un alimento en la biblioteca.",
            """{"type":"object","properties":{"name":{"type":"string"},"serving_grams":{"type":"number"},"calories":{"type":"number"},"protein_g":{"type":"number"},"carbs_g":{"type":"number"},"fat_g":{"type":"number"}},"required":["name","serving_grams","calories"]}""",
        ),
    )

    suspend fun execute(call: ChatToolCall): String {
        return runCatching {
            val args = json.parseToJsonElement(call.argumentsJson).jsonObject
            when (call.name) {
                "list_exercises" -> {
                    val items = trainingRepository.observeExercises().first()
                    items.joinToString(prefix = "[", postfix = "]") {
                        """{"id":${it.id},"name":${it.name.json()},"muscle_group":${it.muscleGroup.json()}}"""
                    }
                }
                "create_exercise" -> {
                    val id = trainingRepository.saveExercise(
                        Exercise(
                            name = args.string("name"),
                            muscleGroup = args.stringOrEmpty("muscle_group"),
                            notes = args.stringOrEmpty("notes"),
                        ),
                    )
                    """{"id":$id}"""
                }
                "list_workouts" -> {
                    val items = trainingRepository.observeWorkouts().first()
                    items.joinToString(prefix = "[", postfix = "]") {
                        """{"id":${it.id},"name":${it.name.json()}}"""
                    }
                }
                "create_workout" -> createWorkout(args)
                "list_training_plans" -> {
                    val items = trainingRepository.observePlans().first()
                    items.joinToString(prefix = "[", postfix = "]") {
                        """{"id":${it.id},"name":${it.name.json()}}"""
                    }
                }
                "create_training_plan" -> {
                    val workoutIds = args["workout_ids"]?.jsonArray
                        ?.mapNotNull { it.jsonPrimitive.longOrNull }
                        .orEmpty()
                    val workouts = workoutIds.mapNotNull { trainingRepository.getWorkout(it) }
                    val id = trainingRepository.savePlan(
                        TrainingPlan(name = args.string("name"), workouts = workouts),
                    )
                    """{"id":$id}"""
                }
                "start_session" -> {
                    val id = trainingRepository.startSessionFromWorkout(args.long("workout_id"))
                    """{"session_id":$id}"""
                }
                "log_set" -> {
                    val id = trainingRepository.logSet(
                        sessionExerciseId = args.long("session_exercise_id"),
                        setId = args.longOrNull("set_id"),
                        setIndex = args.int("set_index"),
                        weightKg = args.doubleOrNull("weight_kg"),
                        reps = args.intOrNull("reps"),
                        isCompleted = args.booleanOrNull("completed") ?: true,
                    )
                    """{"set_id":$id}"""
                }
                "finish_session" -> {
                    trainingRepository.finishSession(args.long("session_id"))
                    """{"ok":true}"""
                }
                "get_nutrition_profile" -> {
                    val profile = nutritionRepository.getProfile()
                    val targets = nutritionRepository.getTargets()
                    """{"profile":${profile.toJson()},"targets":${targets.toJson()}}"""
                }
                "update_nutrition_profile" -> {
                    val profile = BodyProfile(
                        weightKg = args.double("weight_kg"),
                        heightCm = args.double("height_cm"),
                        ageYears = args.int("age_years"),
                        sex = if (args.string("sex").equals("female", true)) Sex.Female else Sex.Male,
                        activityLevel = parseActivity(args.string("activity_level")),
                        goal = parseGoal(args.string("goal")),
                    )
                    val force = args.booleanOrNull("recalculate") ?: true
                    val targets = recalculateTargetsUseCase(profile, force = force)
                    """{"ok":true,"targets":${targets.toJson()}}"""
                }
                "set_macro_targets" -> {
                    val current = nutritionRepository.getTargets()
                    saveManualTargetsUseCase(
                        MacroTargets(
                            calories = args.int("calories"),
                            proteinGrams = args.double("protein_g"),
                            carbsGrams = args.double("carbs_g"),
                            fatGrams = args.double("fat_g"),
                            maintenanceCalories = current?.maintenanceCalories ?: args.int("calories"),
                            calorieAdjustment = current?.calorieAdjustment ?: 0,
                            proteinPerKg = current?.proteinPerKg ?: 2.0,
                            fatPerKg = current?.fatPerKg ?: 0.8,
                            manualOverride = true,
                        ),
                    )
                    """{"ok":true}"""
                }
                "create_nutrition_plan" -> {
                    val id = nutritionRepository.savePlan(
                        NutritionPlan(
                            name = args.string("name"),
                            calories = args.int("calories"),
                            proteinGrams = args.double("protein_g"),
                            carbsGrams = args.double("carbs_g"),
                            fatGrams = args.double("fat_g"),
                        ),
                    )
                    """{"id":$id}"""
                }
                "apply_nutrition_plan" -> {
                    nutritionRepository.applyPlan(args.long("plan_id"))
                    """{"ok":true}"""
                }
                "add_food_to_meal" -> addFoodToMeal(args)
                "create_food" -> {
                    val id = nutritionRepository.saveFood(
                        FoodItem(
                            name = args.string("name"),
                            servingGrams = args.double("serving_grams"),
                            calories = args.double("calories"),
                            proteinGrams = args.doubleOrNull("protein_g") ?: 0.0,
                            carbsGrams = args.doubleOrNull("carbs_g") ?: 0.0,
                            fatGrams = args.doubleOrNull("fat_g") ?: 0.0,
                        ),
                    )
                    """{"id":$id}"""
                }
                else -> """{"error":"Unknown tool"}"""
            }
        }.getOrElse { error ->
            """{"error":${(error.message ?: "failed").json()}}"""
        }
    }

    private suspend fun createWorkout(args: JsonObject): String {
        val exercises = args["exercises"]?.jsonArray.orEmpty().mapIndexed { index, element ->
            val obj = element.jsonObject
            val exerciseId = obj.long("exercise_id")
            val exercise = trainingRepository.getExercise(exerciseId)
            val sets = obj["sets"]?.jsonArray.orEmpty().mapIndexed { setIndex, setElement ->
                val set = setElement.jsonObject
                SetTarget(
                    setIndex = setIndex,
                    targetWeightKg = set.doubleOrNull("weight_kg"),
                    targetReps = set.intOrNull("reps"),
                )
            }.ifEmpty { listOf(SetTarget(setIndex = 0, targetReps = 10)) }
            WorkoutExercise(
                exerciseId = exerciseId,
                exerciseName = exercise?.name ?: "Ejercicio",
                sortOrder = index,
                setTargets = sets,
            )
        }
        val id = trainingRepository.saveWorkout(
            Workout(
                name = args.string("name"),
                notes = args.stringOrEmpty("notes"),
                exercises = exercises,
            ),
        )
        return """{"id":$id}"""
    }

    private suspend fun addFoodToMeal(args: JsonObject): String {
        val day = args.longOrNull("day_epoch_day") ?: LocalDate.now().toEpochDay()
        nutritionRepository.ensureDefaultMeals(day)
        val dayLog = nutritionRepository.observeDayLog(day).first()
        val mealName = args.string("meal_name")
        val mealId = dayLog.meals.firstOrNull { it.name.equals(mealName, ignoreCase = true) }?.id
            ?: nutritionRepository.addMeal(day, mealName)
        val entryId = nutritionRepository.addEntry(
            MealEntry(
                mealId = mealId,
                name = args.string("food_name"),
                grams = args.double("grams"),
                calories = args.double("calories"),
                proteinGrams = args.doubleOrNull("protein_g") ?: 0.0,
                carbsGrams = args.doubleOrNull("carbs_g") ?: 0.0,
                fatGrams = args.doubleOrNull("fat_g") ?: 0.0,
            ),
        )
        return """{"entry_id":$entryId,"meal_id":$mealId}"""
    }

    private fun tool(name: String, description: String, schema: String) =
        AiToolDefinition(name, description, schema)

    private fun parseActivity(raw: String): ActivityLevel = when (raw.lowercase()) {
        "light" -> ActivityLevel.Light
        "moderate" -> ActivityLevel.Moderate
        "active" -> ActivityLevel.Active
        "very_active", "veryactive" -> ActivityLevel.VeryActive
        else -> ActivityLevel.Sedentary
    }

    private fun parseGoal(raw: String): NutritionGoal = when (raw.lowercase()) {
        "lose", "lose_fat", "losefat" -> NutritionGoal.LoseFat
        "gain", "gain_muscle", "gainmuscle" -> NutritionGoal.GainMuscle
        else -> NutritionGoal.Maintain
    }
}

private fun String.json(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

private fun JsonObject.string(key: String): String =
    this[key]?.jsonPrimitive?.contentOrNull ?: error("Missing $key")

private fun JsonObject.stringOrEmpty(key: String): String =
    this[key]?.jsonPrimitive?.contentOrNull.orEmpty()

private fun JsonObject.long(key: String): Long =
    this[key]?.jsonPrimitive?.longOrNull ?: error("Missing $key")

private fun JsonObject.longOrNull(key: String): Long? =
    this[key]?.jsonPrimitive?.longOrNull

private fun JsonObject.int(key: String): Int =
    this[key]?.jsonPrimitive?.intOrNull ?: error("Missing $key")

private fun JsonObject.intOrNull(key: String): Int? =
    this[key]?.jsonPrimitive?.intOrNull

private fun JsonObject.double(key: String): Double =
    this[key]?.jsonPrimitive?.doubleOrNull ?: error("Missing $key")

private fun JsonObject.doubleOrNull(key: String): Double? =
    this[key]?.jsonPrimitive?.doubleOrNull

private fun JsonObject.booleanOrNull(key: String): Boolean? =
    this[key]?.jsonPrimitive?.booleanOrNull

private fun BodyProfile?.toJson(): String {
    if (this == null) return "null"
    return """{"weight_kg":$weightKg,"height_cm":$heightCm,"age_years":$ageYears,"sex":"${sex.name.lowercase()}","activity":"${activityLevel.name.lowercase()}","goal":"${goal.name.lowercase()}"}"""
}

private fun MacroTargets?.toJson(): String {
    if (this == null) return "null"
    return """{"calories":$calories,"protein_g":$proteinGrams,"carbs_g":$carbsGrams,"fat_g":$fatGrams,"maintenance":$maintenanceCalories}"""
}
