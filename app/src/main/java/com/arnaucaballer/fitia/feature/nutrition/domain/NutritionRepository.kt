package com.arnaucaballer.fitia.feature.nutrition.domain

import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun observeProfile(): Flow<BodyProfile?>
    suspend fun getProfile(): BodyProfile?
    suspend fun saveProfile(profile: BodyProfile)

    fun observeTargets(): Flow<MacroTargets?>
    suspend fun getTargets(): MacroTargets?
    suspend fun saveTargets(targets: MacroTargets)

    fun observePlans(): Flow<List<NutritionPlan>>
    suspend fun savePlan(plan: NutritionPlan): Long
    suspend fun deletePlan(id: Long)
    suspend fun applyPlan(planId: Long)

    fun observeFoods(): Flow<List<FoodItem>>
    suspend fun saveFood(food: FoodItem): Long
    suspend fun deleteFood(id: Long)

    fun observeDayLog(dayEpochDay: Long): Flow<DayLog>
    suspend fun ensureDefaultMeals(dayEpochDay: Long)
    suspend fun addMeal(dayEpochDay: Long, name: String): Long
    suspend fun deleteMeal(mealId: Long)
    suspend fun addEntry(entry: MealEntry): Long
    suspend fun updateEntry(entry: MealEntry)
    suspend fun deleteEntry(entryId: Long)

    fun observeWaterTotal(dayEpochDay: Long): Flow<Int>
    suspend fun addWater(dayEpochDay: Long, amountMl: Int)
}
