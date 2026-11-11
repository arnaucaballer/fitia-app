package com.arnaucaballer.fitia.feature.training.data

import com.arnaucaballer.fitia.core.database.ExerciseMuscleEntity
import com.arnaucaballer.fitia.core.database.ExerciseEntity
import com.arnaucaballer.fitia.core.database.MuscleEntity
import com.arnaucaballer.fitia.core.database.PlanEntity
import com.arnaucaballer.fitia.core.database.SessionEntity
import com.arnaucaballer.fitia.core.database.SessionExerciseEntity
import com.arnaucaballer.fitia.core.database.SessionSetEntity
import com.arnaucaballer.fitia.core.database.SessionStatusValues
import com.arnaucaballer.fitia.core.database.WorkoutEntity
import com.arnaucaballer.fitia.core.database.WorkoutExerciseEntity
import com.arnaucaballer.fitia.core.database.WorkoutSetTargetEntity
import com.arnaucaballer.fitia.feature.training.domain.Exercise
import com.arnaucaballer.fitia.feature.training.domain.ExerciseMuscle
import com.arnaucaballer.fitia.feature.training.domain.Muscle
import com.arnaucaballer.fitia.feature.training.domain.SessionExercise
import com.arnaucaballer.fitia.feature.training.domain.SessionSet
import com.arnaucaballer.fitia.feature.training.domain.SessionStatus
import com.arnaucaballer.fitia.feature.training.domain.SetTarget
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.TrainingSession
import com.arnaucaballer.fitia.feature.training.domain.Workout
import com.arnaucaballer.fitia.feature.training.domain.WorkoutExercise

internal fun MuscleEntity.toDomain(): Muscle = Muscle(
    id = id,
    name = name,
    createdAtEpochMs = createdAtEpochMs,
)

internal fun Muscle.toEntity(): MuscleEntity = MuscleEntity(
    id = id,
    name = name.trim(),
    createdAtEpochMs = createdAtEpochMs,
)

internal fun ExerciseEntity.toDomain(
    resolvedMuscleName: String = "",
    resolvedMuscles: List<ExerciseMuscle> = emptyList(),
): Exercise {
    val label = resolvedMuscleName.ifBlank { muscleGroup }
    val muscles = resolvedMuscles.ifEmpty {
        if (muscleId != null && label.isNotBlank()) {
            listOf(ExerciseMuscle(muscleId, label))
        } else {
            emptyList()
        }
    }
    return Exercise(
        id = id,
        name = name,
        muscleId = muscleId,
        muscleName = label,
        muscleGroup = muscleGroup,
        notes = notes,
        createdAtEpochMs = createdAtEpochMs,
        muscles = muscles,
    )
}

internal fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name.trim(),
    muscleId = muscleId,
    muscleGroup = muscleGroup.trim(),
    notes = notes.trim(),
    createdAtEpochMs = createdAtEpochMs,
)

internal fun WorkoutEntity.toDomainSummary(): Workout = Workout(
    id = id,
    name = name,
    notes = notes,
    planId = planId,
    createdAtEpochMs = createdAtEpochMs,
)

internal fun Workout.toEntity(): WorkoutEntity = WorkoutEntity(
    id = id,
    name = name.trim(),
    notes = notes.trim(),
    planId = planId,
    createdAtEpochMs = createdAtEpochMs,
)

internal fun WorkoutExerciseEntity.toDomain(
    exerciseName: String,
    targets: List<SetTarget>,
): WorkoutExercise = WorkoutExercise(
    id = id,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    sortOrder = sortOrder,
    setTargets = targets,
)

internal fun WorkoutSetTargetEntity.toDomain(): SetTarget = SetTarget(
    id = id,
    setIndex = setIndex,
    targetWeightKg = targetWeightKg,
    targetReps = targetReps,
)

internal fun SetTarget.toEntity(workoutExerciseId: Long): WorkoutSetTargetEntity =
    WorkoutSetTargetEntity(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setIndex = setIndex,
        targetWeightKg = targetWeightKg,
        targetReps = targetReps,
    )

internal fun PlanEntity.toDomainSummary(): TrainingPlan = TrainingPlan(
    id = id,
    name = name,
    notes = notes,
    createdAtEpochMs = createdAtEpochMs,
)

internal fun TrainingPlan.toEntity(): PlanEntity = PlanEntity(
    id = id,
    name = name.trim(),
    notes = notes.trim(),
    createdAtEpochMs = createdAtEpochMs,
)

internal fun SessionStatus.toStorage(): String = when (this) {
    SessionStatus.InProgress -> SessionStatusValues.IN_PROGRESS
    SessionStatus.Completed -> SessionStatusValues.COMPLETED
}

internal fun String.toSessionStatus(): SessionStatus = when (this) {
    SessionStatusValues.COMPLETED -> SessionStatus.Completed
    else -> SessionStatus.InProgress
}

internal fun SessionEntity.toDomainSummary(): TrainingSession = TrainingSession(
    id = id,
    workoutId = workoutId,
    name = name,
    status = status.toSessionStatus(),
    startedAtEpochMs = startedAtEpochMs,
    finishedAtEpochMs = finishedAtEpochMs,
)

internal fun SessionExerciseEntity.toDomain(sets: List<SessionSet>): SessionExercise =
    SessionExercise(
        id = id,
        exerciseId = exerciseId,
        name = nameSnapshot,
        sortOrder = sortOrder,
        sets = sets,
    )

internal fun SessionSetEntity.toDomain(): SessionSet = SessionSet(
    id = id,
    setIndex = setIndex,
    weightKg = weightKg,
    reps = reps,
    isCompleted = isCompleted,
)
