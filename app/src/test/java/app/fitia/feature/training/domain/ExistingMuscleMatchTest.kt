package app.fitia.feature.training.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ExistingMuscleMatchTest {
    @Test
    fun keepsCatalogNamesAndDropsInventedOnes() {
        val existing = listOf(
            muscle(1, "Pectoral mayor"),
            muscle(2, "Tríceps braquial (cabeza larga)"),
        )
        val matched = matchExistingMuscles(
            listOf("Pectoral mayor", "Serrato anterior", "Triceps braquial (cabeza larga)"),
            existing,
        )
        assertEquals(listOf(1L, 2L), matched.map { it.id })
        assertEquals(
            listOf("Pectoral mayor", "Tríceps braquial (cabeza larga)"),
            matched.map { it.name },
        )
    }

    @Test
    fun mapsALooseNameOntoTheExistingMuscle() {
        val existing = listOf(
            muscle(1, "Pectoral mayor"),
            muscle(2, "Pectoral menor"),
        )
        val matched = matchExistingMuscles(listOf("Pectoral"), existing)
        assertEquals(listOf("Pectoral mayor"), matched.map { it.name })
    }

    @Test
    fun usesACustomMuscleWhenItAlreadyExists() {
        val existing = listOf(muscle(9, "Serrato"))
        val matched = matchExistingMuscles(listOf("serrato"), existing)
        assertEquals(listOf("Serrato"), matched.map { it.name })
    }

    @Test
    fun returnsNothingWhenNoSavedMuscleMatches() {
        val existing = listOf(muscle(1, "Dorsal ancho"))
        assertEquals(
            emptyList<ExerciseMuscle>(),
            matchExistingMuscles(listOf("Supraespinoso"), existing),
        )
    }

    private fun muscle(id: Long, name: String) = Muscle(id = id, name = name)
}
