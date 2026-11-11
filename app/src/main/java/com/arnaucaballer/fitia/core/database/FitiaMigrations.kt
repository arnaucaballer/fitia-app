package com.arnaucaballer.fitia.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object FitiaMigrations {
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `muscles` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `createdAtEpochMs` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_muscles_name` ON `muscles` (`name`)",
            )
            val now = System.currentTimeMillis()
            val starterMuscles = listOf(
                "Deltoides anterior",
                "Deltoides lateral",
                "Deltoides posterior",
                "Pectoral mayor",
                "Pectoral menor",
                "Bíceps braquial",
                "Braquial",
                "Tríceps braquial (cabeza larga)",
                "Tríceps braquial (cabeza lateral)",
                "Dorsal ancho",
                "Trapecio superior",
                "Trapecio medio",
                "Romboide mayor",
                "Infraespinoso",
                "Recto abdominal",
                "Oblicuo externo",
                "Erectores espinales",
                "Cuádriceps (recto femoral)",
                "Cuádriceps (vasto lateral)",
                "Cuádriceps (vasto medial)",
                "Isquiosurales (bíceps femoral)",
                "Isquiosurales (semitendinoso)",
                "Glúteo mayor",
                "Glúteo medio",
                "Aductores",
                "Gemelo (gastrocnemio)",
                "Sóleo",
                "Antebrazo (flexores)",
                "Antebrazo (extensores)",
            )
            starterMuscles.forEach { name ->
                db.execSQL(
                    "INSERT OR IGNORE INTO muscles (name, createdAtEpochMs) VALUES (?, ?)",
                    arrayOf<Any>(name, now),
                )
            }
            db.execSQL("DROP TABLE IF EXISTS `exercises_new`")
            val hadMuscleId = db.hasColumn("exercises", "muscleId")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `exercises_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `muscleId` INTEGER,
                    `muscleGroup` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `createdAtEpochMs` INTEGER NOT NULL,
                    FOREIGN KEY(`muscleId`) REFERENCES `muscles`(`id`)
                        ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent(),
            )
            if (hadMuscleId) {
                db.execSQL(
                    """
                    INSERT INTO `exercises_new`
                        (`id`, `name`, `muscleId`, `muscleGroup`, `notes`, `createdAtEpochMs`)
                    SELECT `id`, `name`, `muscleId`, `muscleGroup`, `notes`, `createdAtEpochMs`
                    FROM `exercises`
                    """.trimIndent(),
                )
            } else {
                db.execSQL(
                    """
                    INSERT INTO `exercises_new`
                        (`id`, `name`, `muscleId`, `muscleGroup`, `notes`, `createdAtEpochMs`)
                    SELECT `id`, `name`, NULL, `muscleGroup`, `notes`, `createdAtEpochMs`
                    FROM `exercises`
                    """.trimIndent(),
                )
            }
            db.execSQL("DROP TABLE `exercises`")
            db.execSQL("ALTER TABLE `exercises_new` RENAME TO `exercises`")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_exercises_muscleId` ON `exercises` (`muscleId`)",
            )
            db.execSQL(
                """
                UPDATE exercises
                SET muscleId = (
                    SELECT id FROM muscles
                    WHERE lower(muscles.name) = lower(exercises.muscleGroup)
                    LIMIT 1
                )
                WHERE muscleGroup != ''
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `training_preferences` (
                    `id` INTEGER NOT NULL,
                    `activePlanId` INTEGER,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`activePlanId`) REFERENCES `plans`(`id`) ON DELETE SET NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO training_preferences (id, activePlanId) VALUES (1, NULL)
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `plan_weekly_schedule` (
                    `planId` INTEGER NOT NULL,
                    `dayOfWeek` INTEGER NOT NULL,
                    `workoutId` INTEGER,
                    PRIMARY KEY(`planId`, `dayOfWeek`),
                    FOREIGN KEY(`planId`) REFERENCES `plans`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`) ON DELETE SET NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_plan_weekly_schedule_workoutId ON plan_weekly_schedule(workoutId)",
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `workout_schedules` (
                    `dayEpochDay` INTEGER NOT NULL,
                    `workoutId` INTEGER NOT NULL,
                    PRIMARY KEY(`dayEpochDay`),
                    FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_workout_schedules_workoutId ON workout_schedules(workoutId)",
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `day_meal_state` (
                    `dayEpochDay` INTEGER NOT NULL,
                    `initialized` INTEGER NOT NULL,
                    PRIMARY KEY(`dayEpochDay`)
                )
                """.trimIndent(),
            )
        }
    }

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

    private fun SupportSQLiteDatabase.hasColumn(table: String, column: String): Boolean {
        val cursor = query("PRAGMA table_info(`$table`)")
        cursor.use { rows ->
            val nameIndex = rows.getColumnIndex("name")
            while (rows.moveToNext()) {
                if (rows.getString(nameIndex).equals(column, ignoreCase = true)) return true
            }
        }
        return false
    }
}
