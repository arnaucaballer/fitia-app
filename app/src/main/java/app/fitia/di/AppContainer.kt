package app.fitia.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import app.fitia.core.ai.OpenAiCompatibleClient
import app.fitia.core.database.FitiaDatabase
import app.fitia.core.database.FitiaMigrations
import app.fitia.feature.chat.data.RoomChatRepository
import app.fitia.feature.chat.domain.ChatOrchestrator
import app.fitia.feature.chat.domain.ChatRepository
import app.fitia.feature.chat.domain.FitiaToolExecutor
import app.fitia.feature.chat.domain.FoodPhotoEstimator
import app.fitia.feature.nutrition.data.OpenFoodFactsClient
import app.fitia.feature.nutrition.data.RoomNutritionRepository
import app.fitia.feature.nutrition.domain.NutritionRepository
import app.fitia.feature.nutrition.domain.RecalculateTargetsUseCase
import app.fitia.feature.nutrition.domain.SaveManualTargetsUseCase
import app.fitia.feature.settings.data.AiSettingsStore
import app.fitia.feature.training.data.RoomTrainingRepository
import app.fitia.feature.training.domain.AddExerciseToSessionUseCase
import app.fitia.feature.training.domain.DeleteExerciseUseCase
import app.fitia.feature.training.domain.DeletePlanUseCase
import app.fitia.feature.training.domain.DeleteSessionSetUseCase
import app.fitia.feature.training.domain.DeleteSessionUseCase
import app.fitia.feature.training.domain.DeleteWorkoutUseCase
import app.fitia.feature.training.domain.FinishSessionUseCase
import app.fitia.feature.training.domain.GetPlanUseCase
import app.fitia.feature.training.domain.GetSessionUseCase
import app.fitia.feature.training.domain.GetLatestSessionForWorkoutUseCase
import app.fitia.feature.training.domain.GetWorkoutUseCase
import app.fitia.feature.training.domain.LogSessionSetUseCase
import app.fitia.feature.training.domain.ObserveActiveSessionUseCase
import app.fitia.feature.nutrition.domain.NutritionPlanGenerator
import app.fitia.feature.training.domain.ObserveMusclesUseCase
import app.fitia.feature.training.domain.ObserveExercisesUseCase
import app.fitia.feature.training.domain.SaveMuscleUseCase
import app.fitia.feature.training.domain.RenameSessionUseCase
import app.fitia.feature.training.domain.RestoreSessionUseCase
import app.fitia.feature.training.domain.ClearCalendarUseCase
import app.fitia.feature.training.domain.ClearWorkoutScheduleUseCase
import app.fitia.feature.training.domain.ObserveGenericWorkoutsUseCase
import app.fitia.feature.training.domain.ObserveWorkoutSchedulesUseCase
import app.fitia.feature.training.domain.ObserveActiveTrainingPlanUseCase
import app.fitia.feature.training.domain.ObservePlanWeeklyScheduleUseCase
import app.fitia.feature.training.domain.SetActiveTrainingPlanUseCase
import app.fitia.feature.training.domain.SetPlanWeeklySlotUseCase
import app.fitia.feature.training.domain.SetWorkoutScheduleUseCase
import app.fitia.feature.training.domain.ObservePlansUseCase
import app.fitia.feature.training.domain.ObserveSessionsUseCase
import app.fitia.feature.training.domain.ObserveWorkoutsUseCase
import app.fitia.feature.training.domain.SaveExerciseUseCase
import app.fitia.feature.training.domain.SavePlanUseCase
import app.fitia.feature.training.domain.SaveWorkoutUseCase
import app.fitia.feature.training.domain.UpdateWorkoutSetTargetUseCase
import app.fitia.feature.training.domain.StartBlankSessionUseCase
import app.fitia.feature.training.domain.SuggestExerciseMusclesUseCase
import app.fitia.feature.training.domain.StartSessionFromWorkoutUseCase
import app.fitia.feature.training.domain.TrainingPlanGenerator
import app.fitia.feature.training.domain.TrainingRepository

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
        FitiaMigrations.MIGRATION_8_9,
    ).setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()

    val trainingRepository: TrainingRepository =
        RoomTrainingRepository(database.trainingDao())

    val nutritionRepository: NutritionRepository =
        RoomNutritionRepository(database.nutritionDao())

    val chatRepository: ChatRepository =
        RoomChatRepository(database.chatDao())

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
    val updateWorkoutSetTargetUseCase = UpdateWorkoutSetTargetUseCase(trainingRepository)
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
    val addExerciseToSessionUseCase = AddExerciseToSessionUseCase(trainingRepository)
    val deleteSessionSetUseCase = DeleteSessionSetUseCase(trainingRepository)
    val deleteSessionUseCase = DeleteSessionUseCase(trainingRepository)
    val restoreSessionUseCase = RestoreSessionUseCase(trainingRepository)
    val renameSessionUseCase = RenameSessionUseCase(trainingRepository)
    val finishSessionUseCase = FinishSessionUseCase(trainingRepository)

    val observeWorkoutSchedulesUseCase = ObserveWorkoutSchedulesUseCase(trainingRepository)
    val setWorkoutScheduleUseCase = SetWorkoutScheduleUseCase(trainingRepository)
    val clearWorkoutScheduleUseCase = ClearWorkoutScheduleUseCase(trainingRepository)
    val clearCalendarUseCase = ClearCalendarUseCase(trainingRepository)

    val observeActiveTrainingPlanUseCase = ObserveActiveTrainingPlanUseCase(trainingRepository)
    val setActiveTrainingPlanUseCase = SetActiveTrainingPlanUseCase(trainingRepository)
    val observePlanWeeklyScheduleUseCase = ObservePlanWeeklyScheduleUseCase(trainingRepository)
    val setPlanWeeklySlotUseCase = SetPlanWeeklySlotUseCase(trainingRepository)

    val recalculateTargetsUseCase = RecalculateTargetsUseCase(nutritionRepository)
    val saveManualTargetsUseCase = SaveManualTargetsUseCase(nutritionRepository)

    val suggestExerciseMusclesUseCase = SuggestExerciseMusclesUseCase(
        client = openAiCompatibleClient,
        settingsStore = aiSettingsStore,
        trainingRepository = trainingRepository,
    )
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
