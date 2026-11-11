package com.arnaucaballer.fitia.feature.training.domain

import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun observeMuscles(): Flow<List<Muscle>>
    suspend fun saveMuscle(muscle: Muscle): Long
    suspend fun findOrCreateMuscleByName(name: String): Long?

    fun observeExercises(): Flow<List<Exercise>>
    suspend fun listPlanNames(): List<String>
    suspend fun listGenericWorkoutNames(): List<String>
    suspend fun getExercise(id: Long): Exercise?
    suspend fun saveExercise(exercise: Exercise): Long
    suspend fun deleteExercise(id: Long)

    fun observeWorkouts(): Flow<List<Workout>>
    fun observeGenericWorkouts(): Flow<List<Workout>>
    suspend fun getWorkout(id: Long): Workout?
    suspend fun saveWorkout(workout: Workout): Long
    /** Updates one logged target. Returns false when that row is not in the database yet. */
    suspend fun updateWorkoutSetTarget(
        workoutExerciseId: Long,
        setIndex: Int,
        weightKg: Double?,
        reps: Int?,
    ): Boolean
    suspend fun deleteWorkout(id: Long)
    /** Links an existing workout (usually generic) into a plan without copying. */
    suspend fun linkWorkoutToPlan(workoutId: Long, planId: Long)
    /** Creates an empty plan-exclusive workout and links it to the plan. */
    suspend fun createPlanOwnedWorkout(planId: Long, name: String): Long
    /** Copies a generic workout into a plan-owned unique workout and links it. */
    suspend fun copyGenericWorkoutIntoPlan(workoutId: Long, planId: Long): Long

    fun observePlans(): Flow<List<TrainingPlan>>
    suspend fun getPlan(id: Long): TrainingPlan?
    suspend fun savePlan(plan: TrainingPlan): Long
    suspend fun deletePlan(id: Long)

    fun observeSessions(): Flow<List<TrainingSession>>
    fun observeActiveSession(): Flow<TrainingSession?>
    suspend fun getSession(id: Long): TrainingSession?
    /** Last finished session of this workout, with its logged sets. */
    suspend fun getLatestSessionForWorkout(workoutId: Long): TrainingSession?
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
    suspend fun deleteSessionSet(setId: Long)
    suspend fun finishSession(sessionId: Long)
    suspend fun deleteSession(id: Long)
    suspend fun restoreSession(session: TrainingSession)
    suspend fun renameSession(id: Long, name: String)

    fun observeWorkoutSchedules(): Flow<List<WorkoutSchedule>>
    suspend fun setWorkoutSchedule(dayEpochDay: Long, workoutId: Long)
    suspend fun clearWorkoutSchedule(dayEpochDay: Long)

    fun observeActivePlanId(): Flow<Long?>
    suspend fun setActivePlan(planId: Long?)
    fun observePlanWeeklySchedule(planId: Long): Flow<List<PlanWeeklySlot>>
    suspend fun setPlanWeeklySlot(planId: Long, dayOfWeek: Int, workoutId: Long?)
    suspend fun syncWeeklyPlanToCalendar(planId: Long, weeksAhead: Int)
}
