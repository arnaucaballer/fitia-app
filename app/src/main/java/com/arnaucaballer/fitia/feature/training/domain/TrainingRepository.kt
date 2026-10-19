package com.arnaucaballer.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun observeExercises(): Flow<List<Exercise>>
    suspend fun getExercise(id: Long): Exercise?
    suspend fun saveExercise(exercise: Exercise): Long
    suspend fun deleteExercise(id: Long)

    fun observeWorkouts(): Flow<List<Workout>>
    suspend fun getWorkout(id: Long): Workout?
    suspend fun saveWorkout(workout: Workout): Long
    suspend fun deleteWorkout(id: Long)

    fun observePlans(): Flow<List<TrainingPlan>>
    suspend fun getPlan(id: Long): TrainingPlan?
    suspend fun savePlan(plan: TrainingPlan): Long
    suspend fun deletePlan(id: Long)

    fun observeSessions(): Flow<List<TrainingSession>>
    fun observeActiveSession(): Flow<TrainingSession?>
    suspend fun getSession(id: Long): TrainingSession?
    suspend fun startSessionFromWorkout(workoutId: Long): Long
    suspend fun startBlankSession(name: String): Long
    suspend fun logSet(
        sessionExerciseId: Long,
        setId: Long?,
        setIndex: Int,
        weightKg: Double?,
        reps: Int?,
        isCompleted: Boolean,
    ): Long
    suspend fun finishSession(sessionId: Long)
    suspend fun deleteSession(id: Long)
}
