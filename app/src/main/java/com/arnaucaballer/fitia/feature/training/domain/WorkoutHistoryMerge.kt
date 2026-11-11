package com.arnaucaballer.fitia.feature.training.domain

/**
 * Returns this workout with each exercise's set targets replaced by what was actually
 * lifted in [session], so opening a workout again shows the last session's weights and reps.
 * Exercises with no record in that session keep their configured targets.
 */
fun Workout.mergedWithHistory(session: TrainingSession?): Workout {
    if (session == null || session.exercises.isEmpty()) return this
    val byExerciseId = session.exercises
        .filter { it.exerciseId != null }
        .associateBy { it.exerciseId }
    val byName = session.exercises.associateBy { it.name.trim().lowercase() }
    val merged = exercises.map { exercise ->
        val previous = byExerciseId[exercise.exerciseId]
            ?: byName[exercise.exerciseName.trim().lowercase()]
            ?: return@map exercise
        val sets = previous.sets.filter { it.weightKg != null || it.reps != null }
        if (sets.isEmpty()) return@map exercise
        exercise.copy(
            setTargets = sets.mapIndexed { index, set ->
                SetTarget(
                    setIndex = index,
                    targetWeightKg = set.weightKg,
                    targetReps = set.reps,
                )
            },
        )
    }
    return copy(exercises = merged)
}
