package app.fitia.feature.training.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutHistoryMergeTest {

    private fun workout() = Workout(
        id = 1,
        name = "Empuje",
        exercises = listOf(
            WorkoutExercise(
                id = 1,
                exerciseId = 10,
                exerciseName = "Press banca",
                sortOrder = 0,
                setTargets = listOf(
                    SetTarget(setIndex = 0, targetReps = 10),
                    SetTarget(setIndex = 1, targetReps = 10),
                ),
            ),
            WorkoutExercise(
                id = 2,
                exerciseId = 20,
                exerciseName = "Press militar",
                sortOrder = 1,
                setTargets = listOf(SetTarget(setIndex = 0, targetReps = 8)),
            ),
        ),
    )

    private fun session() = TrainingSession(
        id = 5,
        workoutId = 1,
        name = "Empuje",
        status = SessionStatus.Completed,
        startedAtEpochMs = 1_000,
        exercises = listOf(
            SessionExercise(
                id = 50,
                exerciseId = 10,
                name = "Press banca",
                sortOrder = 0,
                sets = listOf(
                    SessionSet(id = 1, setIndex = 0, weightKg = 60.0, reps = 10, isCompleted = true),
                    SessionSet(id = 2, setIndex = 1, weightKg = 62.5, reps = 8, isCompleted = true),
                    SessionSet(id = 3, setIndex = 2, weightKg = null, reps = null),
                ),
            ),
        ),
    )

    @Test
    fun `uses last session weights and reps for logged sets`() {
        val merged = workout().mergedWithHistory(session())
        val bench = merged.exercises.first()

        assertEquals(2, bench.setTargets.size)
        assertEquals(60.0, bench.setTargets[0].targetWeightKg)
        assertEquals(10, bench.setTargets[0].targetReps)
        assertEquals(62.5, bench.setTargets[1].targetWeightKg)
        assertEquals(8, bench.setTargets[1].targetReps)
    }

    @Test
    fun `keeps configured targets when the exercise has no history`() {
        val merged = workout().mergedWithHistory(session())
        val press = merged.exercises.last()

        assertEquals(null, press.setTargets.single().targetWeightKg)
        assertEquals(8, press.setTargets.single().targetReps)
    }

    @Test
    fun `without a previous session the workout is untouched`() {
        val original = workout()

        assertEquals(original, original.mergedWithHistory(null))
    }

    @Test
    fun `matches by name when the exercise id is gone`() {
        val orphan = session().copy(
            exercises = listOf(
                session().exercises.first().copy(exerciseId = null, name = "Press banca"),
            ),
        )

        val bench = workout().mergedWithHistory(orphan).exercises.first()

        assertEquals(60.0, bench.setTargets.first().targetWeightKg)
    }
}
