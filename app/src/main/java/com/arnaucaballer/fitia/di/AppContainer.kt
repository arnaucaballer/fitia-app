package com.arnaucaballer.fitia.di

import android.content.Context
import androidx.room.Room
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
import com.arnaucaballer.fitia.feature.training.domain.DeleteWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.FinishSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.LogSessionSetUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveActiveSessionUseCase
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionPlanGenerator
import com.arnaucaballer.fitia.feature.training.domain.ObserveExercisesUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveGenericWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObservePlansUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveSessionsUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveExerciseUseCase
import com.arnaucaballer.fitia.feature.training.domain.SavePlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveWorkoutUseCase
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
    ).build()

    val trainingRepository: TrainingRepository =
        RoomTrainingRepository(database.trainingDao())

    val nutritionRepository: NutritionRepository =
        RoomNutritionRepository(database.nutritionDao())

    val openFoodFactsClient = OpenFoodFactsClient()
    val aiSettingsStore = AiSettingsStore(context)
    val openAiCompatibleClient = OpenAiCompatibleClient()

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
    val startSessionFromWorkoutUseCase = StartSessionFromWorkoutUseCase(trainingRepository)
    val logSessionSetUseCase = LogSessionSetUseCase(trainingRepository)
    val finishSessionUseCase = FinishSessionUseCase(trainingRepository)

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
