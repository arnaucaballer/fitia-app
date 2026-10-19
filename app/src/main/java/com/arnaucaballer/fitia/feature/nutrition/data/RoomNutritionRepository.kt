package com.arnaucaballer.fitia.feature.nutrition.data

import com.arnaucaballer.fitia.core.database.MealEntity
import com.arnaucaballer.fitia.core.database.NutritionDao
import com.arnaucaballer.fitia.core.database.WaterLogEntity
import com.arnaucaballer.fitia.feature.nutrition.domain.BodyProfile
import com.arnaucaballer.fitia.feature.nutrition.domain.DayLog
import com.arnaucaballer.fitia.feature.nutrition.domain.FoodItem
import com.arnaucaballer.fitia.feature.nutrition.domain.MacroTargets
import com.arnaucaballer.fitia.feature.nutrition.domain.MealEntry
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionPlan
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RoomNutritionRepository(
    private val dao: NutritionDao,
) : NutritionRepository {

    override fun observeProfile(): Flow<BodyProfile?> =
        dao.observeProfile().map { it?.toDomain() }

    override suspend fun getProfile(): BodyProfile? = dao.getProfile()?.toDomain()

    override suspend fun saveProfile(profile: BodyProfile) {
        dao.upsertProfile(
            profile.toEntity().copy(updatedAtEpochMs = System.currentTimeMillis()),
        )
    }

    override fun observeTargets(): Flow<MacroTargets?> =
        dao.observeTargets().map { it?.toDomain() }

    override suspend fun getTargets(): MacroTargets? = dao.getTargets()?.toDomain()

    override suspend fun saveTargets(targets: MacroTargets) {
        dao.upsertTargets(targets.toEntity())
    }

    override fun observePlans(): Flow<List<NutritionPlan>> =
        dao.observePlans().map { list -> list.map { it.toDomain() } }

    override suspend fun savePlan(plan: NutritionPlan): Long {
        val now = System.currentTimeMillis()
        return dao.upsertPlan(
            plan.toEntity().copy(
                createdAtEpochMs = if (plan.id == 0L) now else plan.createdAtEpochMs,
            ),
        )
    }

    override suspend fun deletePlan(id: Long) {
        val entity = dao.getPlan(id) ?: return
        dao.deletePlan(entity)
    }

    override suspend fun applyPlan(planId: Long) {
        val plan = dao.getPlan(planId)?.toDomain() ?: return
        val current = getTargets()
        saveTargets(
            MacroTargets(
                calories = plan.calories,
                proteinGrams = plan.proteinGrams,
                carbsGrams = plan.carbsGrams,
                fatGrams = plan.fatGrams,
                maintenanceCalories = current?.maintenanceCalories ?: plan.calories,
                calorieAdjustment = current?.calorieAdjustment ?: 0,
                proteinPerKg = current?.proteinPerKg ?: 2.0,
                fatPerKg = current?.fatPerKg ?: 0.8,
                manualOverride = true,
                waterMlGoal = current?.waterMlGoal ?: 3000,
            ),
        )
    }

    override fun observeFoods(): Flow<List<FoodItem>> =
        dao.observeFoods().map { list -> list.map { it.toDomain() } }

    override suspend fun saveFood(food: FoodItem): Long = dao.upsertFood(food.toEntity())

    override suspend fun deleteFood(id: Long) {
        val entity = dao.getFood(id) ?: return
        dao.deleteFood(entity)
    }

    override fun observeDayLog(dayEpochDay: Long): Flow<DayLog> {
        return combine(
            dao.observeMeals(dayEpochDay),
            dao.observeEntriesForDay(dayEpochDay),
        ) { meals, entries ->
            val grouped = entries.groupBy { it.mealId }
            DayLog(
                dayEpochDay = dayEpochDay,
                meals = meals.map { meal ->
                    meal.toDomain(
                        grouped[meal.id].orEmpty().map { it.toDomain() },
                    )
                },
            )
        }
    }

    override suspend fun ensureDefaultMeals(dayEpochDay: Long) {
        if (dao.getMeals(dayEpochDay).isNotEmpty()) return
        listOf("Desayuno", "Comida", "Cena", "Snacks").forEachIndexed { index, name ->
            dao.upsertMeal(
                MealEntity(
                    dayEpochDay = dayEpochDay,
                    name = name,
                    sortOrder = index,
                ),
            )
        }
    }

    override suspend fun addMeal(dayEpochDay: Long, name: String): Long {
        val sortOrder = dao.getMeals(dayEpochDay).size
        return dao.upsertMeal(
            MealEntity(
                dayEpochDay = dayEpochDay,
                name = name.trim().ifBlank { "Comida" },
                sortOrder = sortOrder,
            ),
        )
    }

    override suspend fun deleteMeal(mealId: Long) {
        val entity = dao.getMeal(mealId) ?: return
        dao.deleteMeal(entity)
    }

    override suspend fun addEntry(entry: MealEntry): Long = dao.upsertEntry(entry.toEntity())

    override suspend fun updateEntry(entry: MealEntry) {
        dao.upsertEntry(entry.toEntity())
    }

    override suspend fun deleteEntry(entryId: Long) {
        val entity = dao.getEntry(entryId) ?: return
        dao.deleteEntry(entity)
    }

    override fun observeWaterTotal(dayEpochDay: Long): Flow<Int> =
        dao.observeWaterTotal(dayEpochDay)

    override suspend fun addWater(dayEpochDay: Long, amountMl: Int) {
        if (amountMl == 0) return
        dao.insertWaterLog(
            WaterLogEntity(
                dayEpochDay = dayEpochDay,
                amountMl = amountMl,
                createdAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }
}
