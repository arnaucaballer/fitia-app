package com.arnaucaballer.fitia.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetTargetEntity::class,
        PlanEntity::class,
        PlanWorkoutEntity::class,
        SessionEntity::class,
        SessionExerciseEntity::class,
        SessionSetEntity::class,
        BodyProfileEntity::class,
        MacroTargetsEntity::class,
        NutritionPlanEntity::class,
        FoodItemEntity::class,
        MealEntity::class,
        MealEntryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class FitiaDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao
    abstract fun nutritionDao(): NutritionDao
}
