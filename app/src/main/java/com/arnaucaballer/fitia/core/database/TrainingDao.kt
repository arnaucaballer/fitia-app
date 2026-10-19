package com.arnaucaballer.fitia.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE ASC")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExercise(id: Long): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(entity: ExerciseEntity): Long

    @Delete
    suspend fun deleteExercise(entity: ExerciseEntity)

    @Query("SELECT * FROM workouts ORDER BY name COLLATE NOCASE ASC")
    fun observeWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkout(id: Long): WorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkout(entity: WorkoutEntity): Long

    @Delete
    suspend fun deleteWorkout(entity: WorkoutEntity)

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY sortOrder ASC")
    suspend fun getWorkoutExercises(workoutId: Long): List<WorkoutExerciseEntity>

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun clearWorkoutExercises(workoutId: Long)

    @Insert
    suspend fun insertWorkoutExercises(entities: List<WorkoutExerciseEntity>): List<Long>

    @Query("SELECT * FROM workout_set_targets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setIndex ASC")
    suspend fun getSetTargets(workoutExerciseId: Long): List<WorkoutSetTargetEntity>

    @Insert
    suspend fun insertSetTargets(entities: List<WorkoutSetTargetEntity>)

    @Query("SELECT * FROM plans ORDER BY name COLLATE NOCASE ASC")
    fun observePlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlan(id: Long): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(entity: PlanEntity): Long

    @Delete
    suspend fun deletePlan(entity: PlanEntity)

    @Query("SELECT * FROM plan_workouts WHERE planId = :planId ORDER BY sortOrder ASC")
    suspend fun getPlanWorkouts(planId: Long): List<PlanWorkoutEntity>

    @Query("DELETE FROM plan_workouts WHERE planId = :planId")
    suspend fun clearPlanWorkouts(planId: Long)

    @Insert
    suspend fun insertPlanWorkouts(entities: List<PlanWorkoutEntity>)

    @Query("SELECT * FROM sessions ORDER BY startedAtEpochMs DESC")
    fun observeSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE status = :status ORDER BY startedAtEpochMs DESC LIMIT 1")
    fun observeLatestSessionByStatus(status: String): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSession(id: Long): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(entity: SessionEntity): Long

    @Update
    suspend fun updateSession(entity: SessionEntity)

    @Delete
    suspend fun deleteSession(entity: SessionEntity)

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY sortOrder ASC")
    suspend fun getSessionExercises(sessionId: Long): List<SessionExerciseEntity>

    @Insert
    suspend fun insertSessionExercises(entities: List<SessionExerciseEntity>): List<Long>

    @Query("SELECT * FROM session_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setIndex ASC")
    suspend fun getSessionSets(sessionExerciseId: Long): List<SessionSetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessionSet(entity: SessionSetEntity): Long

    @Insert
    suspend fun insertSessionSets(entities: List<SessionSetEntity>)

    @Transaction
    suspend fun replaceWorkoutStructure(
        workoutId: Long,
        exercises: List<WorkoutExerciseEntity>,
        targetsByExerciseIndex: List<List<WorkoutSetTargetEntity>>,
    ) {
        clearWorkoutExercises(workoutId)
        val ids = insertWorkoutExercises(exercises)
        ids.forEachIndexed { index, workoutExerciseId ->
            val targets = targetsByExerciseIndex.getOrNull(index).orEmpty().map {
                it.copy(id = 0, workoutExerciseId = workoutExerciseId)
            }
            if (targets.isNotEmpty()) {
                insertSetTargets(targets)
            }
        }
    }

    @Transaction
    suspend fun replacePlanWorkouts(planId: Long, workouts: List<PlanWorkoutEntity>) {
        clearPlanWorkouts(planId)
        if (workouts.isNotEmpty()) {
            insertPlanWorkouts(workouts)
        }
    }
}
