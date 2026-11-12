package app.fitia.feature.training.domain

import java.text.Normalizer

internal fun matchExistingMuscles(
    requested: List<String>,
    existing: List<Muscle>,
    limit: Int = 4,
): List<ExerciseMuscle> {
    if (existing.isEmpty() || limit <= 0) return emptyList()
    val byName = existing.associateBy { normalizeMuscleName(it.name) }
    val chosen = LinkedHashMap<Long, ExerciseMuscle>()
    for (raw in requested) {
        if (chosen.size >= limit) break
        val key = normalizeMuscleName(raw)
        if (key.isEmpty()) continue
        val exact = byName[key]
        if (exact != null) {
            chosen.putIfAbsent(exact.id, ExerciseMuscle(exact.id, exact.name))
            continue
        }
        val alias = MUSCLE_ALIASES[key]
            ?.let { byName[normalizeMuscleName(it)] }
        if (alias != null) {
            chosen.putIfAbsent(alias.id, ExerciseMuscle(alias.id, alias.name))
            continue
        }
        if (key.length < 4) continue
        existing.filter { normalizeMuscleName(it.name).startsWith(key) }
            .forEach { muscle ->
                if (chosen.size < limit) {
                    chosen.putIfAbsent(muscle.id, ExerciseMuscle(muscle.id, muscle.name))
                }
            }
    }
    return chosen.values.toList()
}

internal fun normalizeMuscleName(value: String): String =
    Normalizer.normalize(value.trim().lowercase(), Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")

private val MUSCLE_ALIASES = mapOf(
    "pecho" to "Pectoral mayor",
    "pectoral" to "Pectoral mayor",
    "pectorales" to "Pectoral mayor",
    "hombro" to "Deltoides anterior",
    "deltoides" to "Deltoides anterior",
    "biceps" to "Bíceps braquial",
    "triceps" to "Tríceps braquial (cabeza larga)",
    "triceps braquial" to "Tríceps braquial (cabeza larga)",
    "dorsal" to "Dorsal ancho",
    "dorsales" to "Dorsal ancho",
    "espalda" to "Dorsal ancho",
    "lumbares" to "Erectores espinales",
    "espalda baja" to "Erectores espinales",
    "gluteo" to "Glúteo mayor",
    "gluteos" to "Glúteo mayor",
    "isquios" to "Isquiosurales (bíceps femoral)",
    "isquiotibiales" to "Isquiosurales (bíceps femoral)",
    "femoral" to "Isquiosurales (bíceps femoral)",
    "gemelo" to "Gemelo (gastrocnemio)",
    "gemelos" to "Gemelo (gastrocnemio)",
    "pantorrilla" to "Gemelo (gastrocnemio)",
    "cuadriceps" to "Cuádriceps (recto femoral)",
    "abdomen" to "Recto abdominal",
    "abdominales" to "Recto abdominal",
    "abdominal" to "Recto abdominal",
    "oblicuo" to "Oblicuo externo",
    "oblicuos" to "Oblicuo externo",
    "trapecio" to "Trapecio superior",
    "trapecios" to "Trapecio superior",
    "romboide" to "Romboide mayor",
    "romboides" to "Romboide mayor",
    "antebrazo" to "Antebrazo (flexores)",
    "soleo" to "Sóleo",
)
