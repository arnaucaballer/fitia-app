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
import com.arnaucaballer.fitia.feature.training.domain.TrainingSession
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
        // —— Lectura entrenamiento ——
        tool(
            "get_training_overview",
            "Vista general de entrenamiento: planes, entrenos genéricos, ejercicios y sesión activa.",
            """{"type":"object","properties":{}}""",
        ),
        tool("list_exercises", "Lista los ejercicios de la biblioteca.", """{"type":"object","properties":{}}"""),
        tool(
            "list_workouts",
            "Lista entrenamientos (genéricos y/o de plan).",
            """{"type":"object","properties":{"only_generic":{"type":"boolean"}}}""",
        ),
        tool(
            "get_workout",
            "Detalle de un entrenamiento: ejercicios, series, peso y reps objetivo.",
            """{"type":"object","properties":{"workout_id":{"type":"integer"}},"required":["workout_id"]}""",
        ),
        tool("list_training_plans", "Lista planes de entrenamiento con nº de entrenos.", """{"type":"object","properties":{}}"""),
        tool(
            "get_training_plan",
            "Detalle de un plan con sus entrenamientos y ejercicios.",
            """{"type":"object","properties":{"plan_id":{"type":"integer"}},"required":["plan_id"]}""",
        ),
        tool(
            "list_sessions",
            "Lista sesiones recientes (historial).",
            """{"type":"object","properties":{"limit":{"type":"integer"}}}""",
        ),
        tool(
            "get_session",
            "Detalle de una sesión: ejercicios y series registradas.",
            """{"type":"object","properties":{"session_id":{"type":"integer"}},"required":["session_id"]}""",
        ),
        tool("get_active_session", "Sesión de entrenamiento en curso, si hay.", """{"type":"object","properties":{}}"""),
        // —— Escritura entrenamiento ——
        tool(
            "create_exercise",
            "Crea un ejercicio.",
            """{"type":"object","properties":{"name":{"type":"string"},"muscle_group":{"type":"string"},"notes":{"type":"string"}},"required":["name"]}""",
        ),
        tool(
            "create_workout",
            "Crea un entrenamiento. Sin plan_id = genérico (biblioteca). Con plan_id = solo de ese plan y se enlaza al plan.",
            """{"type":"object","properties":{"name":{"type":"string"},"notes":{"type":"string"},"plan_id":{"type":"integer"},"exercises":{"type":"array","items":{"type":"object","properties":{"exercise_id":{"type":"integer"},"sets":{"type":"array","items":{"type":"object","properties":{"weight_kg":{"type":"number"},"reps":{"type":"integer"}}}}},"required":["exercise_id"]}}},"required":["name"]}""",
        ),
        tool(
            "create_training_plan",
            "Crea un plan (conjunto de entrenamientos) con IDs existentes: genéricos o del plan.",
            """{"type":"object","properties":{"name":{"type":"string"},"workout_ids":{"type":"array","items":{"type":"integer"}}},"required":["name"]}""",
        ),
        tool(
            "link_workout_to_plan",
            "Enlaza un entrenamiento existente (normalmente genérico) a un plan sin copiarlo.",
            """{"type":"object","properties":{"plan_id":{"type":"integer"},"workout_id":{"type":"integer"}},"required":["plan_id","workout_id"]}""",
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
        // —— Lectura alimentación ——
        tool(
            "get_nutrition_overview",
            "Vista general de alimentación: perfil, objetivos, planes, agua y totales de hoy.",
            """{"type":"object","properties":{}}""",
        ),
        tool("get_nutrition_profile", "Obtiene perfil corporal y objetivos de macros/agua.", """{"type":"object","properties":{}}"""),
        tool(
            "list_nutrition_plans",
            "Lista planes de alimentación guardados con macros.",
            """{"type":"object","properties":{}}""",
        ),
        tool(
            "get_day_log",
            "Diario de un día: comidas, alimentos, macros y agua. Por defecto hoy.",
            """{"type":"object","properties":{"day_epoch_day":{"type":"integer"}}}""",
        ),
        tool("list_foods", "Lista alimentos de la biblioteca del usuario.", """{"type":"object","properties":{}}"""),
        tool(
            "list_days_with_food",
            "Días (epoch day) en los que hay comida registrada.",
            """{"type":"object","properties":{"limit":{"type":"integer"}}}""",
        ),
        // —— Escritura alimentación ——
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
            "add_water",
            "Añade agua (ml) al día indicado (por defecto hoy).",
            """{"type":"object","properties":{"amount_ml":{"type":"integer"},"day_epoch_day":{"type":"integer"}},"required":["amount_ml"]}""",
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
                "get_training_overview" -> trainingOverview()
                "list_exercises" -> {
                    val items = trainingRepository.observeExercises().first()
                    items.joinToString(prefix = "[", postfix = "]") { it.toJson() }
                }
                "list_workouts" -> {
                    val onlyGeneric = args.booleanOrNull("only_generic") ?: false
                    val items = if (onlyGeneric) {
                        trainingRepository.observeGenericWorkouts().first()
                    } else {
                        trainingRepository.observeWorkouts().first()
                    }
                    items.joinToString(prefix = "[", postfix = "]") { it.toSummaryJson() }
                }
                "get_workout" -> {
                    val workout = trainingRepository.getWorkout(args.long("workout_id"))
                        ?: return@runCatching """{"error":"workout_not_found"}"""
                    workout.toDetailJson()
                }
                "list_training_plans" -> {
                    val items = trainingRepository.observePlans().first()
                    items.joinToString(prefix = "[", postfix = "]") {
                        """{"id":${it.id},"name":${it.name.json()},"workout_count":${it.workouts.size}}"""
                    }
                }
                "get_training_plan" -> {
                    val plan = trainingRepository.getPlan(args.long("plan_id"))
                        ?: return@runCatching """{"error":"plan_not_found"}"""
                    plan.toDetailJson()
                }
                "list_sessions" -> {
                    val limit = (args.intOrNull("limit") ?: 20).coerceIn(1, 100)
                    val items = trainingRepository.observeSessions().first().take(limit)
                    items.joinToString(prefix = "[", postfix = "]") { it.toSummaryJson() }
                }
                "get_session" -> {
                    val session = trainingRepository.getSession(args.long("session_id"))
                        ?: return@runCatching """{"error":"session_not_found"}"""
                    session.toDetailJson()
                }
                "get_active_session" -> {
                    val session = trainingRepository.observeActiveSession().first()
                    session?.toDetailJson() ?: """{"active":false}"""
                }
                "create_exercise" -> {
                    val muscleGroup = args.stringOrEmpty("muscle_group")
                    val muscleId = trainingRepository.findOrCreateMuscleByName(muscleGroup)
                    val id = trainingRepository.saveExercise(
                        Exercise(
                            name = args.string("name"),
                            muscleId = muscleId,
                            muscleGroup = muscleGroup,
                            notes = args.stringOrEmpty("notes"),
                        ),
                    )
                    """{"id":$id}"""
                }
                "create_workout" -> createWorkout(args)
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
                "link_workout_to_plan" -> {
                    trainingRepository.linkWorkoutToPlan(
                        workoutId = args.long("workout_id"),
                        planId = args.long("plan_id"),
                    )
                    """{"ok":true}"""
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
                "get_nutrition_overview" -> nutritionOverview()
                "get_nutrition_profile" -> {
                    val profile = nutritionRepository.getProfile()
                    val targets = nutritionRepository.getTargets()
                    """{"profile":${profile.toJson()},"targets":${targets.toJson()}}"""
                }
                "list_nutrition_plans" -> {
                    val items = nutritionRepository.observePlans().first()
                    items.joinToString(prefix = "[", postfix = "]") { it.toJson() }
                }
                "get_day_log" -> {
                    val day = args.longOrNull("day_epoch_day") ?: LocalDate.now().toEpochDay()
                    dayLogJson(day)
                }
                "list_foods" -> {
                    val items = nutritionRepository.observeFoods().first()
                    items.joinToString(prefix = "[", postfix = "]") { it.toJson() }
                }
                "list_days_with_food" -> {
                    val limit = (args.intOrNull("limit") ?: 30).coerceIn(1, 365)
                    val days = nutritionRepository.observeDaysWithFood().first().take(limit)
                    days.joinToString(prefix = "[", postfix = "]")
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
                            waterMlGoal = current?.waterMlGoal ?: 3000,
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
                "add_water" -> {
                    val day = args.longOrNull("day_epoch_day") ?: LocalDate.now().toEpochDay()
                    val amount = args.int("amount_ml")
                    nutritionRepository.addWater(day, amount)
                    val total = nutritionRepository.observeWaterTotal(day).first()
                    """{"ok":true,"day_epoch_day":$day,"added_ml":$amount,"total_ml":$total}"""
                }
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

    private suspend fun trainingOverview(): String {
        val plans = trainingRepository.observePlans().first()
        val generic = trainingRepository.observeGenericWorkouts().first()
        val exercises = trainingRepository.observeExercises().first()
        val active = trainingRepository.observeActiveSession().first()
        val recent = trainingRepository.observeSessions().first().take(5)
        return buildString {
            append("{")
            append("\"plans\":")
            append(plans.joinToString(prefix = "[", postfix = "]") {
                """{"id":${it.id},"name":${it.name.json()},"workout_count":${it.workouts.size}}"""
            })
            append(",\"generic_workouts\":")
            append(generic.joinToString(prefix = "[", postfix = "]") { it.toSummaryJson() })
            append(",\"exercise_count\":")
            append(exercises.size)
            append(",\"active_session\":")
            append(active?.toSummaryJson() ?: "null")
            append(",\"recent_sessions\":")
            append(recent.joinToString(prefix = "[", postfix = "]") { it.toSummaryJson() })
            append("}")
        }
    }

    private suspend fun nutritionOverview(): String {
        val today = LocalDate.now().toEpochDay()
        val profile = nutritionRepository.getProfile()
        val targets = nutritionRepository.getTargets()
        val plans = nutritionRepository.observePlans().first()
        val dayLog = nutritionRepository.observeDayLog(today).first()
        val water = nutritionRepository.observeWaterTotal(today).first()
        val t = dayLog.totals
        return buildString {
            append("{")
            append("\"today_epoch_day\":$today,")
            append("\"profile\":${profile.toJson()},")
            append("\"targets\":${targets.toJson()},")
            append("\"plans\":")
            append(plans.joinToString(prefix = "[", postfix = "]") { it.toJson() })
            append(",\"today_totals\":")
            append("""{"calories":${t.calories},"protein_g":${t.proteinGrams},"carbs_g":${t.carbsGrams},"fat_g":${t.fatGrams}}""")
            append(",\"today_water_ml\":$water,")
            append("\"meal_count\":${dayLog.meals.size}")
            append("}")
        }
    }

    private suspend fun dayLogJson(day: Long): String {
        val dayLog = nutritionRepository.observeDayLog(day).first()
        val water = nutritionRepository.observeWaterTotal(day).first()
        val targets = nutritionRepository.getTargets()
        val t = dayLog.totals
        val meals = dayLog.meals.joinToString(prefix = "[", postfix = "]") { meal ->
            val entries = meal.entries.joinToString(prefix = "[", postfix = "]") { e ->
                """{"id":${e.id},"name":${e.name.json()},"grams":${e.grams},"calories":${e.calories},"protein_g":${e.proteinGrams},"carbs_g":${e.carbsGrams},"fat_g":${e.fatGrams}}"""
            }
            """{"id":${meal.id},"name":${meal.name.json()},"entries":$entries}"""
        }
        return """{"day_epoch_day":$day,"water_ml":$water,"targets":${targets.toJson()},"totals":{"calories":${t.calories},"protein_g":${t.proteinGrams},"carbs_g":${t.carbsGrams},"fat_g":${t.fatGrams}},"meals":$meals}"""
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
            }
            WorkoutExercise(
                exerciseId = exerciseId,
                exerciseName = exercise?.name ?: "Ejercicio",
                sortOrder = index,
                setTargets = sets,
            )
        }
        val planId = args.longOrNull("plan_id")
        val id = trainingRepository.saveWorkout(
            Workout(
                name = args.string("name"),
                notes = args.stringOrEmpty("notes"),
                planId = planId,
                exercises = exercises,
            ),
        )
        if (planId != null) {
            trainingRepository.linkWorkoutToPlan(workoutId = id, planId = planId)
        }
        return """{"id":$id,"plan_id":${planId ?: "null"},"is_generic":${planId == null}}"""
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
    return """{"calories":$calories,"protein_g":$proteinGrams,"carbs_g":$carbsGrams,"fat_g":$fatGrams,"maintenance":$maintenanceCalories,"water_ml_goal":$waterMlGoal,"manual_override":$manualOverride}"""
}

private fun Exercise.toJson(): String =
    """{"id":$id,"name":${name.json()},"muscle_group":${muscleGroup.json()},"notes":${notes.json()}}"""

private fun Workout.toSummaryJson(): String =
    """{"id":$id,"name":${name.json()},"plan_id":${planId ?: "null"},"is_generic":$isGeneric,"exercise_count":${exercises.size}}"""

private fun Workout.toDetailJson(): String {
    val ex = exercises.joinToString(prefix = "[", postfix = "]") { we ->
        val sets = we.setTargets.joinToString(prefix = "[", postfix = "]") { s ->
            """{"set_index":${s.setIndex},"weight_kg":${s.targetWeightKg ?: "null"},"reps":${s.targetReps ?: "null"}}"""
        }
        """{"id":${we.id},"exercise_id":${we.exerciseId},"name":${we.exerciseName.json()},"sets":$sets}"""
    }
    return """{"id":$id,"name":${name.json()},"notes":${notes.json()},"plan_id":${planId ?: "null"},"is_generic":$isGeneric,"exercises":$ex}"""
}

private fun TrainingPlan.toDetailJson(): String {
    val ws = workouts.joinToString(prefix = "[", postfix = "]") { it.toDetailJson() }
    return """{"id":$id,"name":${name.json()},"notes":${notes.json()},"workouts":$ws}"""
}

private fun TrainingSession.toSummaryJson(): String =
    """{"id":$id,"name":${name.json()},"status":"${status.name}","workout_id":${workoutId ?: "null"},"started_at_ms":$startedAtEpochMs,"finished_at_ms":${finishedAtEpochMs ?: "null"},"exercise_count":${exercises.size}}"""

private fun TrainingSession.toDetailJson(): String {
    val ex = exercises.joinToString(prefix = "[", postfix = "]") { se ->
        val sets = se.sets.joinToString(prefix = "[", postfix = "]") { s ->
            """{"id":${s.id},"set_index":${s.setIndex},"weight_kg":${s.weightKg ?: "null"},"reps":${s.reps ?: "null"},"completed":${s.isCompleted}}"""
        }
        """{"id":${se.id},"exercise_id":${se.exerciseId ?: "null"},"name":${se.name.json()},"sets":$sets}"""
    }
    return """{"id":$id,"name":${name.json()},"status":"${status.name}","workout_id":${workoutId ?: "null"},"started_at_ms":$startedAtEpochMs,"finished_at_ms":${finishedAtEpochMs ?: "null"},"exercises":$ex}"""
}

private fun NutritionPlan.toJson(): String =
    """{"id":$id,"name":${name.json()},"calories":$calories,"protein_g":$proteinGrams,"carbs_g":$carbsGrams,"fat_g":$fatGrams}"""

private fun FoodItem.toJson(): String =
    """{"id":$id,"name":${name.json()},"brand":${brand.json()},"serving_grams":$servingGrams,"calories":$calories,"protein_g":$proteinGrams,"carbs_g":$carbsGrams,"fat_g":$fatGrams}"""
