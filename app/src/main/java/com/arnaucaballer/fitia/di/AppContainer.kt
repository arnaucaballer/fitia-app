package com.arnaucaballer.fitia.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arnaucaballer.fitia.core.ai.OpenAiCompatibleClient
import com.arnaucaballer.fitia.core.database.FitiaDatabase
import com.arnaucaballer.fitia.core.database.FitiaMigrations
import com.arnaucaballer.fitia.feature.chat.domain.ChatOrchestrator
import com.arnaucaballer.fitia.feature.chat.domain.FitiaToolExecutor
import com.arnaucaballer.fitia.feature.chat.domain.FoodPhotoEstimator
import com.arnaucaballer.fitia.feature.nutrition.data.OpenFoodFactsClient
import com.arnaucaballer.fitia.feature.nutrition.data.RoomNutritionRepository
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import com.arnaucaballer.fitia.feature.nutrition.domain.RecalculateTargetsUseCase
import com.arnaucaballer.fitia.feature.nutrition.domain.SaveManualTargetsUseCase
import com.arnaucaballer.fitia.feature.settings.data.AiSettingsStore
import com.arnaucaballer.fitia.feature.training.data.RoomTrainingRepository
import com.arnaucaballer.fitia.feature.training.domain.DeleteExerciseUseCase
import com.arnaucaballer.fitia.feature.training.domain.DeletePlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.DeleteSessionSetUseCase
import com.arnaucaballer.fitia.feature.training.domain.DeleteSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.DeleteWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.FinishSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetLatestSessionForWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.LogSessionSetUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveActiveSessionUseCase
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionPlanGenerator
import com.arnaucaballer.fitia.feature.training.domain.ObserveMusclesUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveExercisesUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveMuscleUseCase
import com.arnaucaballer.fitia.feature.training.domain.RenameSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.RestoreSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.ClearWorkoutScheduleUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveGenericWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveWorkoutSchedulesUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveActiveTrainingPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObservePlanWeeklyScheduleUseCase
import com.arnaucaballer.fitia.feature.training.domain.SetActiveTrainingPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.SetPlanWeeklySlotUseCase
import com.arnaucaballer.fitia.feature.training.domain.SetWorkoutScheduleUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObservePlansUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveSessionsUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveExerciseUseCase
import com.arnaucaballer.fitia.feature.training.domain.SavePlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.StartBlankSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.StartSessionFromWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlanGenerator
import com.arnaucaballer.fitia.feature.training.domain.TrainingRepository

class AppContainer(
    context: Context,
) {
    private val database: FitiaDatabase = Room.databaseBuilder(
        context.applicationContext,
        FitiaDatabase::class.java,
        "fitia.db",
    ).addMigrations(
        FitiaMigrations.MIGRATION_1_2,
        FitiaMigrations.MIGRATION_2_3,
        FitiaMigrations.MIGRATION_3_4,
        FitiaMigrations.MIGRATION_4_5,
        FitiaMigrations.MIGRATION_5_6,
        FitiaMigrations.MIGRATION_6_7,
        FitiaMigrations.MIGRATION_7_8,
    ).setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()

    val trainingRepository: TrainingRepository =
        RoomTrainingRepository(database.trainingDao())

    val nutritionRepository: NutritionRepository =
        RoomNutritionRepository(database.nutritionDao())

    val openFoodFactsClient = OpenFoodFactsClient()
    val aiSettingsStore = AiSettingsStore(context)
    val openAiCompatibleClient = OpenAiCompatibleClient()

    val observeMusclesUseCase = ObserveMusclesUseCase(trainingRepository)
    val saveMuscleUseCase = SaveMuscleUseCase(trainingRepository)
    val observeExercisesUseCase = ObserveExercisesUseCase(trainingRepository)
    val saveExerciseUseCase = SaveExerciseUseCase(trainingRepository)
    val deleteExerciseUseCase = DeleteExerciseUseCase(trainingRepository)

    val observeWorkoutsUseCase = ObserveWorkoutsUseCase(trainingRepository)
    val observeGenericWorkoutsUseCase = ObserveGenericWorkoutsUseCase(trainingRepository)
    val getWorkoutUseCase = GetWorkoutUseCase(trainingRepository)
    val saveWorkoutUseCase = SaveWorkoutUseCase(trainingRepository)
    val deleteWorkoutUseCase = DeleteWorkoutUseCase(trainingRepository)

    val observePlansUseCase = ObservePlansUseCase(trainingRepository)
    val getPlanUseCase = GetPlanUseCase(trainingRepository)
    val savePlanUseCase = SavePlanUseCase(trainingRepository)
    val deletePlanUseCase = DeletePlanUseCase(trainingRepository)

    val observeSessionsUseCase = ObserveSessionsUseCase(trainingRepository)
    val observeActiveSessionUseCase = ObserveActiveSessionUseCase(trainingRepository)
    val getSessionUseCase = GetSessionUseCase(trainingRepository)
    val getLatestSessionForWorkoutUseCase =
        GetLatestSessionForWorkoutUseCase(trainingRepository)
    val startSessionFromWorkoutUseCase = StartSessionFromWorkoutUseCase(trainingRepository)
    val startBlankSessionUseCase = StartBlankSessionUseCase(trainingRepository)
    val logSessionSetUseCase = LogSessionSetUseCase(trainingRepository)
    val deleteSessionSetUseCase = DeleteSessionSetUseCase(trainingRepository)
    val deleteSessionUseCase = DeleteSessionUseCase(trainingRepository)
    val restoreSessionUseCase = RestoreSessionUseCase(trainingRepository)
    val renameSessionUseCase = RenameSessionUseCase(trainingRepository)
    val finishSessionUseCase = FinishSessionUseCase(trainingRepository)

    val observeWorkoutSchedulesUseCase = ObserveWorkoutSchedulesUseCase(trainingRepository)
    val setWorkoutScheduleUseCase = SetWorkoutScheduleUseCase(trainingRepository)
    val clearWorkoutScheduleUseCase = ClearWorkoutScheduleUseCase(trainingRepository)

    val observeActiveTrainingPlanUseCase = ObserveActiveTrainingPlanUseCase(trainingRepository)
    val setActiveTrainingPlanUseCase = SetActiveTrainingPlanUseCase(trainingRepository)
    val observePlanWeeklyScheduleUseCase = ObservePlanWeeklyScheduleUseCase(trainingRepository)
    val setPlanWeeklySlotUseCase = SetPlanWeeklySlotUseCase(trainingRepository)

    val recalculateTargetsUseCase = RecalculateTargetsUseCase(nutritionRepository)
    val saveManualTargetsUseCase = SaveManualTargetsUseCase(nutritionRepository)

    val trainingPlanGenerator = TrainingPlanGenerator(
        client = openAiCompatibleClient,
        settingsStore = aiSettingsStore,
        trainingRepository = trainingRepository,
    )
    val nutritionPlanGenerator = NutritionPlanGenerator(
        client = openAiCompatibleClient,
        settingsStore = aiSettingsStore,
        nutritionRepository = nutritionRepository,
        recalculateTargetsUseCase = recalculateTargetsUseCase,
    )

    private val toolExecutor = FitiaToolExecutor(
        trainingRepository = trainingRepository,
        nutritionRepository = nutritionRepository,
        recalculateTargetsUseCase = recalculateTargetsUseCase,
        saveManualTargetsUseCase = saveManualTargetsUseCase,
    )

    val chatOrchestrator = ChatOrchestrator(
        client = openAiCompatibleClient,
        toolExecutor = toolExecutor,
        trainingRepository = trainingRepository,
        nutritionRepository = nutritionRepository,
    )

    val foodPhotoEstimator = FoodPhotoEstimator(openAiCompatibleClient)
}
