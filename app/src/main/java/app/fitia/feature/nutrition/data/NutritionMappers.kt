package app.fitia.feature.nutrition.data

import app.fitia.core.database.BodyProfileEntity
import app.fitia.core.database.FoodItemEntity
import app.fitia.core.database.MacroTargetsEntity
import app.fitia.core.database.MealEntity
import app.fitia.core.database.MealEntryEntity
import app.fitia.core.database.NutritionPlanEntity
import app.fitia.core.database.WaterLogEntity
import app.fitia.feature.nutrition.domain.ActivityLevel
import app.fitia.feature.nutrition.domain.BodyProfile
import app.fitia.feature.nutrition.domain.FoodItem
import app.fitia.feature.nutrition.domain.MacroTargets
import app.fitia.feature.nutrition.domain.Meal
import app.fitia.feature.nutrition.domain.MealEntry
import app.fitia.feature.nutrition.domain.NutritionGoal
import app.fitia.feature.nutrition.domain.NutritionPlan
import app.fitia.feature.nutrition.domain.Sex
import app.fitia.feature.nutrition.domain.WaterLog

internal fun BodyProfileEntity.toDomain(): BodyProfile = BodyProfile(
    weightKg = weightKg,
    heightCm = heightCm,
    ageYears = ageYears,
    sex = sex.toSex(),
    activityLevel = activityLevel.toActivityLevel(),
    goal = goal.toGoal(),
    updatedAtEpochMs = updatedAtEpochMs,
)

internal fun BodyProfile.toEntity(): BodyProfileEntity = BodyProfileEntity(
    weightKg = weightKg,
    heightCm = heightCm,
    ageYears = ageYears,
    sex = sex.name.lowercase(),
    activityLevel = activityLevel.name.lowercase(),
    goal = goal.name.lowercase(),
    updatedAtEpochMs = updatedAtEpochMs,
)

internal fun MacroTargetsEntity.toDomain(): MacroTargets = MacroTargets(
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    maintenanceCalories = maintenanceCalories,
    calorieAdjustment = calorieAdjustment,
    proteinPerKg = proteinPerKg,
    fatPerKg = fatPerKg,
    manualOverride = manualOverride,
    waterMlGoal = waterMlGoal,
)

internal fun MacroTargets.toEntity(): MacroTargetsEntity = MacroTargetsEntity(
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    maintenanceCalories = maintenanceCalories,
    calorieAdjustment = calorieAdjustment,
    proteinPerKg = proteinPerKg,
    fatPerKg = fatPerKg,
    manualOverride = manualOverride,
    waterMlGoal = waterMlGoal,
)

internal fun NutritionPlanEntity.toDomain(): NutritionPlan = NutritionPlan(
    id = id,
    name = name,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    notes = notes,
    createdAtEpochMs = createdAtEpochMs,
)

internal fun NutritionPlan.toEntity(): NutritionPlanEntity = NutritionPlanEntity(
    id = id,
    name = name.trim(),
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    notes = notes.trim(),
    createdAtEpochMs = createdAtEpochMs,
)

internal fun FoodItemEntity.toDomain(): FoodItem = FoodItem(
    id = id,
    name = name,
    brand = brand,
    servingGrams = servingGrams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    source = source,
    barcode = barcode,
)

internal fun FoodItem.toEntity(): FoodItemEntity = FoodItemEntity(
    id = id,
    name = name.trim(),
    brand = brand.trim(),
    servingGrams = servingGrams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    source = source,
    barcode = barcode,
)

internal fun MealEntity.toDomain(entries: List<MealEntry>): Meal = Meal(
    id = id,
    dayEpochDay = dayEpochDay,
    name = name,
    sortOrder = sortOrder,
    entries = entries,
)

internal fun MealEntryEntity.toDomain(): MealEntry = MealEntry(
    id = id,
    mealId = mealId,
    foodItemId = foodItemId,
    name = nameSnapshot,
    grams = grams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
)

internal fun WaterLogEntity.toDomain(): WaterLog = WaterLog(
    id = id,
    dayEpochDay = dayEpochDay,
    amountMl = amountMl,
    createdAtEpochMs = createdAtEpochMs,
)

internal fun WaterLog.toEntity(): WaterLogEntity = WaterLogEntity(
    id = id,
    dayEpochDay = dayEpochDay,
    amountMl = amountMl,
    createdAtEpochMs = createdAtEpochMs,
)

internal fun MealEntry.toEntity(): MealEntryEntity = MealEntryEntity(
    id = id,
    mealId = mealId,
    foodItemId = foodItemId,
    nameSnapshot = name.trim(),
    grams = grams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
)

private fun String.toSex(): Sex = when (lowercase()) {
    "female" -> Sex.Female
    else -> Sex.Male
}

private fun String.toActivityLevel(): ActivityLevel = when (lowercase()) {
    "light" -> ActivityLevel.Light
    "moderate" -> ActivityLevel.Moderate
    "active" -> ActivityLevel.Active
    "veryactive", "very_active" -> ActivityLevel.VeryActive
    else -> ActivityLevel.Sedentary
}

private fun String.toGoal(): NutritionGoal = when (lowercase()) {
    "losefat", "lose_fat", "lose" -> NutritionGoal.LoseFat
    "gainmuscle", "gain_muscle", "gain" -> NutritionGoal.GainMuscle
    else -> NutritionGoal.Maintain
}
