package app.fitia.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MuscleEntity::class,
        ExerciseEntity::class,
        ExerciseMuscleEntity::class,
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
        WaterLogEntity::class,
        DayMealStateEntity::class,
        WorkoutScheduleEntity::class,
        TrainingPreferencesEntity::class,
        PlanWeeklyScheduleEntity::class,
        ChatThreadEntity::class,
        ChatMessageEntity::class,
    ],
    version = 9,
    exportSchema = true,
)
abstract class FitiaDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun chatDao(): ChatDao
}
