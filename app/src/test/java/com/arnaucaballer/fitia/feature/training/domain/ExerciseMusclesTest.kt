package com.arnaucaballer.fitia.feature.training.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseMusclesTest {

    @Test
    fun `keeps every selected muscle in order`() {
        val exercise = Exercise(
            name = "Press inclinado",
            muscles = listOf(
                ExerciseMuscle(1, "Pectoral mayor"),
                ExerciseMuscle(2, "Deltoides anterior"),
                ExerciseMuscle(3, "Tríceps braquial (cabeza larga)"),
            ),
        )

        assertEquals(listOf(1L, 2L, 3L), exercise.muscleIds)
        assertEquals("Pectoral mayor", exercise.primaryMuscleName)
        assertEquals(
            listOf("Deltoides anterior", "Tríceps braquial (cabeza larga)"),
            exercise.extraMuscleNames,
        )
    }

    @Test
    fun `falls back to the legacy single muscle`() {
        val exercise = Exercise(name = "Remo", muscleId = 7, muscleName = "Dorsal ancho")

        assertEquals(listOf(7L), exercise.muscleIds)
        assertEquals("Dorsal ancho", exercise.primaryMuscleName)
        assertEquals(emptyList<String>(), exercise.extraMuscleNames)
    }

    @Test
    fun `ignores duplicate muscles`() {
        val exercise = Exercise(
            name = "Fondos",
            muscles = listOf(
                ExerciseMuscle(4, "Pectoral mayor"),
                ExerciseMuscle(4, "Pectoral mayor"),
                ExerciseMuscle(5, "Tríceps braquial"),
            ),
        )

        assertEquals(listOf(4L, 5L), exercise.muscleIds)
    }
}
