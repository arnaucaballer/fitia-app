package com.arnaucaballer.fitia.feature.training.data

import com.arnaucaballer.fitia.core.database.PlanWorkoutEntity
import com.arnaucaballer.fitia.core.database.SessionEntity
import com.arnaucaballer.fitia.core.database.SessionExerciseEntity
import com.arnaucaballer.fitia.core.database.SessionSetEntity
import com.arnaucaballer.fitia.core.database.SessionStatusValues
import com.arnaucaballer.fitia.core.database.TrainingDao
import com.arnaucaballer.fitia.core.database.WorkoutExerciseEntity
import com.arnaucaballer.fitia.feature.training.domain.Exercise
import com.arnaucaballer.fitia.feature.training.domain.SessionStatus
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.TrainingRepository
import com.arnaucaballer.fitia.feature.training.domain.TrainingSession
import com.arnaucaballer.fitia.feature.training.domain.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTrainingRepository(
    private val dao: TrainingDao,
) : TrainingRepository {

    override fun observeExercises(): Flow<List<Exercise>> =
        dao.observeExercises().map { list -> list.map { it.toDomain() } }

    override suspend fun getExercise(id: Long): Exercise? =
        dao.getExercise(id)?.toDomain()

    override suspend fun saveExercise(exercise: Exercise): Long {
        val now = System.currentTimeMillis()
        val entity = exercise.toEntity().copy(
            createdAtEpochMs = if (exercise.id == 0L) now else exercise.createdAtEpochMs,
        )
        return dao.upsertExercise(entity)
    }

    override suspend fun deleteExercise(id: Long) {
        val entity = dao.getExercise(id) ?: return
        dao.deleteExercise(entity)
    }

    override fun observeWorkouts(): Flow<List<Workout>> =
        dao.observeWorkouts().map { list -> list.map { it.toDomainSummary() } }

    override suspend fun getWorkout(id: Long): Workout? {
        val workout = dao.getWorkout(id) ?: return null
        val links = dao.getWorkoutExercises(id)
        val exercises = links.map { link ->
            val exerciseName = dao.getExercise(link.exerciseId)?.name ?: "Ejercicio"
            val targets = dao.getSetTargets(link.id).map { it.toDomain() }
            link.toDomain(exerciseName, targets)
        }
        return workout.toDomainSummary().copy(exercises = exercises)
    }

    override suspend fun saveWorkout(workout: Workout): Long {
        val now = System.currentTimeMillis()
        val workoutId = dao.upsertWorkout(
            workout.toEntity().copy(
                createdAtEpochMs = if (workout.id == 0L) now else workout.createdAtEpochMs,
            ),
        )
        val exerciseRows = workout.exercises.mapIndexed { index, item ->
            WorkoutExerciseEntity(
                workoutId = workoutId,
                exerciseId = item.exerciseId,
                sortOrder = index,
            )
        }
        val targets = workout.exercises.map { item ->
            item.setTargets.mapIndexed { setIndex, target ->
                target.copy(setIndex = setIndex).toEntity(workoutExerciseId = 0)
            }
        }
        dao.replaceWorkoutStructure(workoutId, exerciseRows, targets)
        return workoutId
    }

    override suspend fun deleteWorkout(id: Long) {
        val entity = dao.getWorkout(id) ?: return
        dao.deleteWorkout(entity)
    }

    override fun observePlans(): Flow<List<TrainingPlan>> =
        dao.observePlans().map { list -> list.map { it.toDomainSummary() } }

    override suspend fun getPlan(id: Long): TrainingPlan? {
        val plan = dao.getPlan(id) ?: return null
        val links = dao.getPlanWorkouts(id)
        val workouts = links.mapNotNull { link -> getWorkout(link.workoutId) }
        return plan.toDomainSummary().copy(workouts = workouts)
    }

    override suspend fun savePlan(plan: TrainingPlan): Long {
        val now = System.currentTimeMillis()
        val planId = dao.upsertPlan(
            plan.toEntity().copy(
                createdAtEpochMs = if (plan.id == 0L) now else plan.createdAtEpochMs,
            ),
        )
        val rows = plan.workouts.mapIndexed { index, workout ->
            PlanWorkoutEntity(
                planId = planId,
                workoutId = workout.id,
                sortOrder = index,
            )
        }
        dao.replacePlanWorkouts(planId, rows)
        return planId
    }

    override suspend fun deletePlan(id: Long) {
        val entity = dao.getPlan(id) ?: return
        dao.deletePlan(entity)
    }

    override fun observeSessions(): Flow<List<TrainingSession>> =
        dao.observeSessions().map { list -> list.map { it.toDomainSummary() } }

    override fun observeActiveSession(): Flow<TrainingSession?> =
        dao.observeLatestSessionByStatus(SessionStatusValues.IN_PROGRESS).map { entity ->
            entity?.toDomainSummary()
        }

    override suspend fun getSession(id: Long): TrainingSession? {
        val session = dao.getSession(id) ?: return null
        val exercises = dao.getSessionExercises(id).map { exercise ->
            val sets = dao.getSessionSets(exercise.id).map { it.toDomain() }
            exercise.toDomain(sets)
        }
        return session.toDomainSummary().copy(exercises = exercises)
    }

    override suspend fun startSessionFromWorkout(workoutId: Long): Long {
        val workout = getWorkout(workoutId) ?: error("Workout not found")
        val now = System.currentTimeMillis()
        val sessionId = dao.upsertSession(
            SessionEntity(
                workoutId = workoutId,
                name = workout.name,
                status = SessionStatus.InProgress.toStorage(),
                startedAtEpochMs = now,
            ),
        )
        val exerciseIds = dao.insertSessionExercises(
            workout.exercises.mapIndexed { index, item ->
                SessionExerciseEntity(
                    sessionId = sessionId,
                    exerciseId = item.exerciseId,
                    nameSnapshot = item.exerciseName,
                    sortOrder = index,
                )
            },
        )
        exerciseIds.forEachIndexed { index, sessionExerciseId ->
            val targets = workout.exercises[index].setTargets
            val sets = if (targets.isEmpty()) {
                listOf(
                    SessionSetEntity(
                        sessionExerciseId = sessionExerciseId,
                        setIndex = 0,
                    ),
                )
            } else {
                targets.map { target ->
                    SessionSetEntity(
                        sessionExerciseId = sessionExerciseId,
                        setIndex = target.setIndex,
                        weightKg = target.targetWeightKg,
                        reps = target.targetReps,
                    )
                }
            }
            dao.insertSessionSets(sets)
        }
        return sessionId
    }

    override suspend fun startBlankSession(name: String): Long {
        val now = System.currentTimeMillis()
        return dao.upsertSession(
            SessionEntity(
                name = name.trim().ifBlank { "Sesión libre" },
                status = SessionStatus.InProgress.toStorage(),
                startedAtEpochMs = now,
            ),
        )
    }

    override suspend fun logSet(
        sessionExerciseId: Long,
        setId: Long?,
        setIndex: Int,
        weightKg: Double?,
        reps: Int?,
        isCompleted: Boolean,
    ): Long {
        return dao.upsertSessionSet(
            SessionSetEntity(
                id = setId ?: 0L,
                sessionExerciseId = sessionExerciseId,
                setIndex = setIndex,
                weightKg = weightKg,
                reps = reps,
                isCompleted = isCompleted,
            ),
        )
    }

    override suspend fun finishSession(sessionId: Long) {
        val session = dao.getSession(sessionId) ?: return
        dao.updateSession(
            session.copy(
                status = SessionStatus.Completed.toStorage(),
                finishedAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun deleteSession(id: Long) {
        val entity = dao.getSession(id) ?: return
        dao.deleteSession(entity)
    }
}
