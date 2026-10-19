package com.arnaucaballer.fitia.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object FitiaMigrations {
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE workouts ADD COLUMN planId INTEGER")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_workouts_planId ON workouts(planId)")
            db.execSQL("ALTER TABLE macro_targets ADD COLUMN waterMlGoal INTEGER NOT NULL DEFAULT 3000")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS water_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    dayEpochDay INTEGER NOT NULL,
                    amountMl INTEGER NOT NULL,
                    createdAtEpochMs INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_water_logs_dayEpochDay ON water_logs(dayEpochDay)",
            )
        }
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS body_profile (
                    id INTEGER NOT NULL PRIMARY KEY,
                    weightKg REAL NOT NULL,
                    heightCm REAL NOT NULL,
                    ageYears INTEGER NOT NULL,
                    sex TEXT NOT NULL,
                    activityLevel TEXT NOT NULL,
                    goal TEXT NOT NULL,
                    updatedAtEpochMs INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS macro_targets (
                    id INTEGER NOT NULL PRIMARY KEY,
                    calories INTEGER NOT NULL,
                    proteinGrams REAL NOT NULL,
                    carbsGrams REAL NOT NULL,
                    fatGrams REAL NOT NULL,
                    maintenanceCalories INTEGER NOT NULL,
                    calorieAdjustment INTEGER NOT NULL,
                    proteinPerKg REAL NOT NULL,
                    fatPerKg REAL NOT NULL,
                    manualOverride INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS nutrition_plans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    calories INTEGER NOT NULL,
                    proteinGrams REAL NOT NULL,
                    carbsGrams REAL NOT NULL,
                    fatGrams REAL NOT NULL,
                    notes TEXT NOT NULL,
                    createdAtEpochMs INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS food_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    brand TEXT NOT NULL,
                    servingGrams REAL NOT NULL,
                    calories REAL NOT NULL,
                    proteinGrams REAL NOT NULL,
                    carbsGrams REAL NOT NULL,
                    fatGrams REAL NOT NULL,
                    source TEXT NOT NULL,
                    barcode TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS meals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    dayEpochDay INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    sortOrder INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_meals_dayEpochDay ON meals(dayEpochDay)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS meal_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    mealId INTEGER NOT NULL,
                    foodItemId INTEGER,
                    nameSnapshot TEXT NOT NULL,
                    grams REAL NOT NULL,
                    calories REAL NOT NULL,
                    proteinGrams REAL NOT NULL,
                    carbsGrams REAL NOT NULL,
                    fatGrams REAL NOT NULL,
                    FOREIGN KEY(mealId) REFERENCES meals(id) ON DELETE CASCADE,
                    FOREIGN KEY(foodItemId) REFERENCES food_items(id) ON DELETE SET NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_entries_mealId ON meal_entries(mealId)")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_meal_entries_foodItemId ON meal_entries(foodItemId)",
            )
        }
    }
}
