package com.arnaucaballer.fitia.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Query("SELECT * FROM body_profile WHERE id = 1")
    fun observeProfile(): Flow<BodyProfileEntity?>

    @Query("SELECT * FROM body_profile WHERE id = 1")
    suspend fun getProfile(): BodyProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(entity: BodyProfileEntity)

    @Query("SELECT * FROM macro_targets WHERE id = 1")
    fun observeTargets(): Flow<MacroTargetsEntity?>

    @Query("SELECT * FROM macro_targets WHERE id = 1")
    suspend fun getTargets(): MacroTargetsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTargets(entity: MacroTargetsEntity)

    @Query("SELECT * FROM nutrition_plans ORDER BY name COLLATE NOCASE ASC")
    fun observePlans(): Flow<List<NutritionPlanEntity>>

    @Query("SELECT * FROM nutrition_plans WHERE id = :id")
    suspend fun getPlan(id: Long): NutritionPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(entity: NutritionPlanEntity): Long

    @Delete
    suspend fun deletePlan(entity: NutritionPlanEntity)

    @Query("SELECT * FROM food_items ORDER BY name COLLATE NOCASE ASC")
    fun observeFoods(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getFood(id: Long): FoodItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFood(entity: FoodItemEntity): Long

    @Delete
    suspend fun deleteFood(entity: FoodItemEntity)

    @Query("SELECT * FROM meals WHERE dayEpochDay = :day ORDER BY sortOrder ASC")
    fun observeMeals(day: Long): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE dayEpochDay = :day ORDER BY sortOrder ASC")
    suspend fun getMeals(day: Long): List<MealEntity>

    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMeal(id: Long): MealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeal(entity: MealEntity): Long

    @Delete
    suspend fun deleteMeal(entity: MealEntity)

    @Query("SELECT * FROM meal_entries WHERE mealId = :mealId ORDER BY id ASC")
    suspend fun getEntries(mealId: Long): List<MealEntryEntity>

    @Query(
        """
        SELECT meal_entries.* FROM meal_entries
        INNER JOIN meals ON meals.id = meal_entries.mealId
        WHERE meals.dayEpochDay = :day
        ORDER BY meals.sortOrder ASC, meal_entries.id ASC
        """,
    )
    fun observeEntriesForDay(day: Long): Flow<List<MealEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entity: MealEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entity: MealEntryEntity)

    @Query("SELECT * FROM meal_entries WHERE id = :id")
    suspend fun getEntry(id: Long): MealEntryEntity?

    @Query("SELECT * FROM water_logs WHERE dayEpochDay = :day ORDER BY createdAtEpochMs ASC")
    fun observeWaterLogs(day: Long): Flow<List<WaterLogEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE dayEpochDay = :day")
    fun observeWaterTotal(day: Long): Flow<Int>

    @Insert
    suspend fun insertWaterLog(entity: WaterLogEntity): Long

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLog(id: Long)

    @Query(
        """
        SELECT DISTINCT m.dayEpochDay FROM meals m
        INNER JOIN meal_entries e ON e.mealId = m.id
        ORDER BY m.dayEpochDay DESC
        """,
    )
    fun observeDaysWithFood(): Flow<List<Long>>
}
