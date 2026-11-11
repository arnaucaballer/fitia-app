package com.arnaucaballer.fitia.feature.training.data

import com.arnaucaballer.fitia.core.database.ExerciseMuscleEntity
import com.arnaucaballer.fitia.core.database.PlanWorkoutEntity
import com.arnaucaballer.fitia.core.database.SessionEntity
import com.arnaucaballer.fitia.core.database.SessionExerciseEntity
import com.arnaucaballer.fitia.core.database.SessionSetEntity
import com.arnaucaballer.fitia.core.database.SessionStatusValues
import com.arnaucaballer.fitia.core.database.TrainingDao
import com.arnaucaballer.fitia.core.database.WorkoutExerciseEntity
import com.arnaucaballer.fitia.core.database.MuscleEntity
import com.arnaucaballer.fitia.feature.training.domain.Exercise
import com.arnaucaballer.fitia.feature.training.domain.ExerciseMuscle
import com.arnaucaballer.fitia.feature.training.domain.Muscle
import com.arnaucaballer.fitia.feature.training.domain.SessionStatus
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.TrainingRepository
import com.arnaucaballer.fitia.feature.training.domain.TrainingSession
import com.arnaucaballer.fitia.feature.training.domain.Workout
import com.arnaucaballer.fitia.feature.training.domain.mergedWithHistory
import com.arnaucaballer.fitia.feature.training.domain.PlanWeeklySlot
import com.arnaucaballer.fitia.feature.training.domain.WorkoutSchedule
import com.arnaucaballer.fitia.core.database.PlanWeeklyScheduleEntity
import com.arnaucaballer.fitia.core.database.TrainingPreferencesEntity
import com.arnaucaballer.fitia.core.database.WorkoutScheduleEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RoomTrainingRepository(
    private val dao: TrainingDao,
) : TrainingRepository {

    override fun observeMuscles(): Flow<List<Muscle>> =
        dao.observeMuscles().map { list -> list.map { it.toDomain() } }

    override suspend fun saveMuscle(muscle: Muscle): Long {
        val now = System.currentTimeMillis()
        val existing = dao.findMuscleByName(muscle.name.trim())
        if (existing != null && muscle.id == 0L) return existing.id
        val entity = muscle.toEntity().copy(
            createdAtEpochMs = if (muscle.id == 0L) now else muscle.createdAtEpochMs,
        )
        return dao.upsertMuscle(entity)
    }

    override suspend fun findOrCreateMuscleByName(name: String): Long? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        dao.findMuscleByName(trimmed)?.let { return it.id }
        return dao.upsertMuscle(
            MuscleEntity(
                name = trimmed,
                createdAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }

    override fun observeExercises(): Flow<List<Exercise>> =
        combine(
            dao.observeExercises(),
            dao.observeMuscles(),
            dao.observeExerciseMuscles(),
        ) { exercises, muscles, links ->
            val muscleNames = muscles.associate { it.id to it.name }
            val byExercise = links.groupBy { it.exerciseId }
            exercises.map { entity ->
                val rows = byExercise[entity.id].orEmpty()
                    .sortedBy { it.sortOrder }
                    .mapNotNull { link ->
                        muscleNames[link.muscleId]?.let { ExerciseMuscle(link.muscleId, it) }
                    }
                val resolved = entity.muscleId?.let { muscleNames[it] }.orEmpty()
                entity.toDomain(resolvedMuscleName = resolved, resolvedMuscles = rows)
            }
        }.distinctUntilChanged()

    override suspend fun listPlanNames(): List<String> = dao.listPlanNames()

    override suspend fun listGenericWorkoutNames(): List<String> = dao.listGenericWorkoutNames()

    override suspend fun getExercise(id: Long): Exercise? {
        val entity = dao.getExercise(id) ?: return null
        val resolved = entity.muscleId?.let { dao.getMuscle(it)?.name }.orEmpty()
        val rows = dao.getExerciseMuscles(id).mapNotNull { link ->
            dao.getMuscle(link.muscleId)?.let { ExerciseMuscle(it.id, it.name) }
        }
        return entity.toDomain(resolvedMuscleName = resolved, resolvedMuscles = rows)
    }

    override suspend fun saveExercise(exercise: Exercise): Long {
        val now = System.currentTimeMillis()
        val muscleIds = exercise.muscleIds
        val primaryId = muscleIds.firstOrNull()
        val muscleLabel = primaryId?.let { dao.getMuscle(it)?.name }.orEmpty()
            .ifBlank { exercise.muscleGroup }
        val entity = exercise.toEntity().copy(
            muscleId = primaryId,
            muscleGroup = muscleLabel.trim(),
            createdAtEpochMs = if (exercise.id == 0L) now else exercise.createdAtEpochMs,
        )
        val exerciseId = dao.upsertExercise(entity)
        dao.clearExerciseMuscles(exerciseId)
        if (muscleIds.isNotEmpty()) {
            dao.insertExerciseMuscles(
                muscleIds.mapIndexed { index, muscleId ->
                    ExerciseMuscleEntity(
                        exerciseId = exerciseId,
                        muscleId = muscleId,
                        sortOrder = index,
                    )
                },
            )
        }
        return exerciseId
    }

    override suspend fun deleteExercise(id: Long) {
        val entity = dao.getExercise(id) ?: return
        dao.deleteExercise(entity)
    }

    override fun observeWorkouts(): Flow<List<Workout>> =
        dao.observeWorkouts().map { list -> list.map { it.toDomainSummary() } }

    override fun observeGenericWorkouts(): Flow<List<Workout>> =
        dao.observeGenericWorkouts().map { list -> list.map { it.toDomainSummary() } }

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

    override suspend fun linkWorkoutToPlan(workoutId: Long, planId: Long) {
        val workout = getWorkout(workoutId) ?: error("Workout not found")
        val plan = getPlan(planId) ?: error("Plan not found")
        if (plan.workouts.any { it.id == workoutId }) return
        savePlan(plan.copy(workouts = plan.workouts + workout))
    }

    override suspend fun createPlanOwnedWorkout(planId: Long, name: String): Long {
        val workoutId = saveWorkout(
            Workout(
                name = name.trim().ifBlank { "Entreno del plan" },
                planId = planId,
            ),
        )
        val plan = getPlan(planId) ?: return workoutId
        val workout = getWorkout(workoutId) ?: return workoutId
        savePlan(plan.copy(workouts = plan.workouts + workout))
        return workoutId
    }

    override suspend fun copyGenericWorkoutIntoPlan(workoutId: Long, planId: Long): Long {
        val source = getWorkout(workoutId) ?: error("Workout not found")
        require(source.isGeneric) { "Only generic workouts can be copied into a plan" }
        val copyId = saveWorkout(
            source.copy(
                id = 0,
                planId = planId,
                name = source.name,
                createdAtEpochMs = 0,
                exercises = source.exercises.map { exercise ->
                    exercise.copy(
                        id = 0,
                        setTargets = exercise.setTargets.map { it.copy(id = 0) },
                    )
                },
            ),
        )
        val plan = getPlan(planId) ?: return copyId
        savePlan(plan.copy(workouts = plan.workouts + listOf(getWorkout(copyId)!!)))
        return copyId
    }

    override fun observePlans(): Flow<List<TrainingPlan>> =
        combine(
            dao.observePlans(),
            dao.observeAllPlanWorkouts(),
            dao.observeWorkouts(),
        ) { plans, links, workouts ->
            val workoutById = workouts.associateBy { it.id }
            val linksByPlan = links.groupBy { it.planId }
            plans.map { plan ->
                val planWorkouts = linksByPlan[plan.id].orEmpty()
                    .mapNotNull { link -> workoutById[link.workoutId]?.toDomainSummary() }
                plan.toDomainSummary().copy(workouts = planWorkouts)
            }
        }.distinctUntilChanged()

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
        val prefs = dao.observeTrainingPreferences().first()
        if (prefs?.activePlanId == id) {
            dao.upsertTrainingPreferences(TrainingPreferencesEntity(activePlanId = null))
        }
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

    override suspend fun getLatestSessionForWorkout(workoutId: Long): TrainingSession? {
        val session = dao.getLatestSessionForWorkout(
            workoutId = workoutId,
            status = SessionStatusValues.COMPLETED,
        ) ?: return null
        val exercises = dao.getSessionExercises(session.id).map { exercise ->
            val sets = dao.getSessionSets(exercise.id).map { it.toDomain() }
            exercise.toDomain(sets)
        }
        return session.toDomainSummary().copy(exercises = exercises)
    }

    override suspend fun startSessionFromWorkout(workoutId: Long): Long {
        val workout = getWorkout(workoutId) ?: error("Workout not found")
        val seeded = workout.mergedWithHistory(getLatestSessionForWorkout(workoutId))
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
            seeded.exercises.mapIndexed { index, item ->
                SessionExerciseEntity(
                    sessionId = sessionId,
                    exerciseId = item.exerciseId,
                    nameSnapshot = item.exerciseName,
                    sortOrder = index,
                )
            },
        )
        exerciseIds.forEachIndexed { index, sessionExerciseId ->
            val targets = seeded.exercises[index].setTargets
            if (targets.isEmpty()) return@forEachIndexed
            dao.insertSessionSets(
                targets.map { target ->
                    SessionSetEntity(
                        sessionExerciseId = sessionExerciseId,
                        setIndex = target.setIndex,
                        weightKg = target.targetWeightKg,
                        reps = target.targetReps,
                    )
                },
            )
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

    override suspend fun deleteSessionSet(setId: Long) {
        val set = dao.getSessionSet(setId) ?: return
        dao.deleteSessionSet(setId)
        dao.shiftSessionSetIndexesDown(set.sessionExerciseId, set.setIndex)
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

    override suspend fun restoreSession(session: TrainingSession) {
        dao.upsertSession(
            SessionEntity(
                id = session.id,
                workoutId = session.workoutId,
                name = session.name,
                status = session.status.toStorage(),
                startedAtEpochMs = session.startedAtEpochMs,
                finishedAtEpochMs = session.finishedAtEpochMs,
            ),
        )
        session.exercises.forEach { exercise ->
            dao.upsertSessionExercise(
                SessionExerciseEntity(
                    id = exercise.id,
                    sessionId = session.id,
                    exerciseId = exercise.exerciseId,
                    nameSnapshot = exercise.name,
                    sortOrder = exercise.sortOrder,
                ),
            )
            exercise.sets.forEach { set ->
                dao.upsertSessionSet(
                    SessionSetEntity(
                        id = set.id,
                        sessionExerciseId = exercise.id,
                        setIndex = set.setIndex,
                        weightKg = set.weightKg,
                        reps = set.reps,
                        isCompleted = set.isCompleted,
                    ),
                )
            }
        }
    }

    override suspend fun renameSession(id: Long, name: String) {
        val entity = dao.getSession(id) ?: return
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        dao.updateSession(entity.copy(name = trimmed))
    }

    override fun observeWorkoutSchedules(): Flow<List<WorkoutSchedule>> =
        combine(
            dao.observeWorkoutSchedules(),
            dao.observeWorkouts(),
        ) { schedules, workouts ->
            val names = workouts.associate { it.id to it.name }
            schedules.mapNotNull { row ->
                val name = names[row.workoutId] ?: return@mapNotNull null
                WorkoutSchedule(
                    dayEpochDay = row.dayEpochDay,
                    workoutId = row.workoutId,
                    workoutName = name,
                )
            }
        }.distinctUntilChanged()

    override suspend fun setWorkoutSchedule(dayEpochDay: Long, workoutId: Long) {
        dao.getWorkout(workoutId) ?: error("Workout not found")
        dao.upsertWorkoutSchedule(
            WorkoutScheduleEntity(
                dayEpochDay = dayEpochDay,
                workoutId = workoutId,
            ),
        )
    }

    override suspend fun clearWorkoutSchedule(dayEpochDay: Long) {
        dao.deleteWorkoutSchedule(dayEpochDay)
    }

    override fun observeActivePlanId(): Flow<Long?> =
        dao.observeTrainingPreferences().map { it?.activePlanId }

    override suspend fun setActivePlan(planId: Long?) {
        if (planId != null) {
            dao.getPlan(planId) ?: error("Plan not found")
        }
        dao.upsertTrainingPreferences(TrainingPreferencesEntity(activePlanId = planId))
        if (planId != null) {
            syncWeeklyPlanToCalendar(planId, weeksAhead = 12)
        }
    }

    override fun observePlanWeeklySchedule(planId: Long): Flow<List<PlanWeeklySlot>> =
        combine(
            dao.observePlanWeeklySchedule(planId),
            dao.observeWorkouts(),
        ) { slots, workouts ->
            val names = workouts.associate { it.id to it.name }
            slots.map { row ->
                PlanWeeklySlot(
                    planId = row.planId,
                    dayOfWeek = row.dayOfWeek,
                    workoutId = row.workoutId,
                    workoutName = row.workoutId?.let { names[it] },
                )
            }
        }

    override suspend fun setPlanWeeklySlot(planId: Long, dayOfWeek: Int, workoutId: Long?) {
        require(dayOfWeek in DayOfWeek.MONDAY.value..DayOfWeek.SUNDAY.value)
        if (workoutId != null) {
            val plan = getPlan(planId) ?: error("Plan not found")
            check(plan.workouts.any { it.id == workoutId }) { "Workout not in plan" }
        }
        dao.upsertPlanWeeklySlot(
            PlanWeeklyScheduleEntity(
                planId = planId,
                dayOfWeek = dayOfWeek,
                workoutId = workoutId,
            ),
        )
    }

    override suspend fun syncWeeklyPlanToCalendar(planId: Long, weeksAhead: Int) {
        val slots = dao.observePlanWeeklySchedule(planId).first()
        val byDay = slots.associate { it.dayOfWeek to it.workoutId }
        if (byDay.isEmpty()) return
        val start = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val totalDays = weeksAhead * 7
        val upserts = mutableListOf<WorkoutScheduleEntity>()
        val clears = mutableListOf<Long>()
        for (offset in 0 until totalDays) {
            val date = start.plusDays(offset.toLong())
            val dow = date.dayOfWeek.value
            if (!byDay.containsKey(dow)) continue
            val epoch = date.toEpochDay()
            val workoutId = byDay[dow]
            if (workoutId != null) {
                upserts += WorkoutScheduleEntity(dayEpochDay = epoch, workoutId = workoutId)
            } else {
                clears += epoch
            }
        }
        if (upserts.isNotEmpty()) dao.upsertWorkoutSchedules(upserts)
        if (clears.isNotEmpty()) dao.deleteWorkoutSchedules(clears)
    }
}
