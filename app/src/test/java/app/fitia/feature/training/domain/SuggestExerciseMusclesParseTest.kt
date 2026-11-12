package app.fitia.feature.training.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestExerciseMusclesParseTest {

    @Test
    fun readsMuscleArrayAndDropsBlanks() {
        val names = parseSuggestedMuscleNames(
            """{"muscles":["Pectoral"," Tríceps braquial ","", "Pectoral"]}""",
        )
        assertEquals(listOf("Pectoral", "Tríceps braquial", "Pectoral"), names)
    }

    @Test
    fun ignoresMarkdownFence() {
        val names = parseSuggestedMuscleNames(
            """
            ```json
            {"muscles":["Dorsal ancho"]}
            ```
            """.trimIndent(),
        )
        assertEquals(listOf("Dorsal ancho"), names)
    }

    @Test
    fun returnsEmptyWhenTheAnswerIsNotJson() {
        assertEquals(emptyList<String>(), parseSuggestedMuscleNames("no lo sé"))
    }
}
