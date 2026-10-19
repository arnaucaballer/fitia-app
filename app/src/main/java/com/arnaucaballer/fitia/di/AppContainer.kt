package com.arnaucaballer.fitia.di

import android.content.Context
import androidx.room.Room
import com.arnaucaballer.fitia.core.database.FitiaDatabase
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
import com.arnaucaballer.fitia.feature.training.domain.ObserveExercisesUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObservePlansUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveSessionsUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveExerciseUseCase
import com.arnaucaballer.fitia.feature.training.domain.SavePlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.SaveWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.StartSessionFromWorkoutUseCase
import com.arnaucaballer.fitia.feature.training.domain.TrainingRepository

class AppContainer(
    context: Context,
) {
    private val database: FitiaDatabase = Room.databaseBuilder(
        context.applicationContext,
        FitiaDatabase::class.java,
        "fitia.db",
    ).build()

    val trainingRepository: TrainingRepository =
        RoomTrainingRepository(database.trainingDao())

    val observeExercisesUseCase = ObserveExercisesUseCase(trainingRepository)
    val saveExerciseUseCase = SaveExerciseUseCase(trainingRepository)
    val deleteExerciseUseCase = DeleteExerciseUseCase(trainingRepository)

    val observeWorkoutsUseCase = ObserveWorkoutsUseCase(trainingRepository)
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
}
