package com.arnaucaballer.fitia.feature.training.ui

object MuscleMap {
    val zones: List<MuscleZone> = listOf(
        zone(
            id = "delts_front",
            title = "Hombro",
            generalName = "Deltoides",
            front = true,
            crop = floatArrayOf(0.24f, 0.12f, 0.48f, 0.32f),
            hits = listOf(
                hit(0.28f, 0.15f, 0.43f, 0.28f),
                hit(0.57f, 0.15f, 0.72f, 0.28f),
            ),
            parts = listOf(
                part("Deltoides lateral", "Lateral", 0.00f, 0.00f, 0.62f, 0.55f),
                part("Deltoides anterior", "Anterior", 0.28f, 0.38f, 1.00f, 1.00f),
            ),
        ),
        zone(
            id = "biceps",
            title = "Bíceps",
            generalName = "Bíceps",
            front = true,
            crop = floatArrayOf(0.14f, 0.24f, 0.44f, 0.44f),
            hits = listOf(
                hit(0.16f, 0.27f, 0.40f, 0.40f),
                hit(0.60f, 0.27f, 0.84f, 0.40f),
            ),
            parts = listOf(
                part("Bíceps braquial", "Bíceps", 0.28f, 0.00f, 1.00f, 0.72f),
                part("Braquial", "Braquial", 0.00f, 0.40f, 0.48f, 1.00f),
            ),
        ),
        zone(
            id = "chest",
            title = "Pecho",
            generalName = "Pectoral",
            front = true,
            crop = floatArrayOf(0.34f, 0.16f, 0.68f, 0.36f),
            hits = listOf(
                hit(0.38f, 0.18f, 0.51f, 0.33f),
                hit(0.49f, 0.18f, 0.62f, 0.33f),
            ),
            parts = listOf(
                part("Pectoral menor", "Pectoral menor", 0.08f, 0.00f, 0.92f, 0.38f),
                part("Pectoral mayor", "Pectoral mayor", 0.04f, 0.32f, 0.96f, 1.00f),
            ),
        ),
        zone(
            id = "abs",
            title = "Abdomen",
            generalName = "Abdominales",
            front = true,
            crop = floatArrayOf(0.38f, 0.30f, 0.62f, 0.52f),
            hits = listOf(hit(0.42f, 0.32f, 0.58f, 0.50f)),
            parts = listOf(
                part("Recto abdominal", "Recto", 0.12f, 0.04f, 0.88f, 0.96f),
            ),
        ),
        zone(
            id = "obliques",
            title = "Oblicuo",
            generalName = "Oblicuos",
            front = true,
            crop = floatArrayOf(0.30f, 0.32f, 0.50f, 0.52f),
            hits = listOf(
                hit(0.33f, 0.34f, 0.45f, 0.50f),
                hit(0.55f, 0.34f, 0.67f, 0.50f),
            ),
            parts = listOf(
                part("Oblicuo externo", "Oblicuo externo", 0.08f, 0.04f, 0.92f, 0.96f),
            ),
        ),
        zone(
            id = "forearm_front",
            title = "Antebrazo",
            generalName = "Antebrazo",
            front = true,
            crop = floatArrayOf(0.02f, 0.34f, 0.32f, 0.54f),
            hits = listOf(
                hit(0.04f, 0.38f, 0.28f, 0.51f),
                hit(0.72f, 0.38f, 0.96f, 0.51f),
            ),
            parts = listOf(
                part("Antebrazo (flexores)", "Flexores", 0.06f, 0.04f, 0.94f, 0.96f),
            ),
        ),
        zone(
            id = "quads",
            title = "Cuádriceps",
            generalName = "Cuádriceps",
            front = true,
            crop = floatArrayOf(0.32f, 0.50f, 0.52f, 0.76f),
            hits = listOf(
                hit(0.34f, 0.52f, 0.48f, 0.72f),
                hit(0.52f, 0.52f, 0.66f, 0.72f),
            ),
            parts = listOf(
                part("Cuádriceps (vasto lateral)", "Vasto lateral", 0.00f, 0.04f, 0.38f, 0.96f),
                part("Cuádriceps (recto femoral)", "Recto femoral", 0.34f, 0.02f, 0.70f, 0.58f),
                part("Cuádriceps (vasto medial)", "Vasto medial", 0.48f, 0.50f, 1.00f, 0.98f),
            ),
        ),
        zone(
            id = "adductors",
            title = "Aductores",
            generalName = "Aductores",
            front = true,
            crop = floatArrayOf(0.42f, 0.52f, 0.58f, 0.72f),
            hits = listOf(
                hit(0.43f, 0.54f, 0.53f, 0.70f),
                hit(0.47f, 0.54f, 0.57f, 0.70f),
            ),
            parts = listOf(
                part("Aductores", "Aductores", 0.10f, 0.04f, 0.90f, 0.96f),
            ),
        ),
        zone(
            id = "delts_back",
            title = "Hombro posterior",
            generalName = "Deltoides",
            front = false,
            crop = floatArrayOf(0.20f, 0.119f, 0.46f, 0.317f),
            hits = listOf(
                hit(0.24f, 0.149f, 0.40f, 0.278f),
                hit(0.60f, 0.149f, 0.76f, 0.278f),
            ),
            parts = listOf(
                part("Deltoides posterior", "Posterior", 0.08f, 0.06f, 0.92f, 0.94f),
            ),
        ),
        zone(
            id = "traps",
            title = "Trapecio",
            generalName = "Trapecio",
            front = false,
            crop = floatArrayOf(0.30f, 0.099f, 0.70f, 0.298f),
            hits = listOf(hit(0.36f, 0.139f, 0.64f, 0.258f)),
            parts = listOf(
                part("Trapecio superior", "Superior", 0.06f, 0.00f, 0.94f, 0.55f),
                part("Trapecio medio", "Medio", 0.12f, 0.45f, 0.88f, 1.00f),
            ),
        ),
        zone(
            id = "infraspinatus",
            title = "Infraespinoso",
            generalName = "Infraespinoso",
            front = false,
            crop = floatArrayOf(0.26f, 0.218f, 0.50f, 0.397f),
            hits = listOf(
                hit(0.28f, 0.238f, 0.44f, 0.357f),
                hit(0.56f, 0.238f, 0.72f, 0.357f),
            ),
            parts = listOf(
                part("Infraespinoso", "Infraespinoso", 0.08f, 0.06f, 0.92f, 0.94f),
            ),
        ),
        zone(
            id = "rhomboids",
            title = "Romboide",
            generalName = "Romboide mayor",
            front = false,
            crop = floatArrayOf(0.38f, 0.159f, 0.62f, 0.357f),
            hits = listOf(hit(0.42f, 0.198f, 0.58f, 0.337f)),
            parts = listOf(
                part("Romboide mayor", "Romboide mayor", 0.10f, 0.04f, 0.90f, 0.96f),
            ),
        ),
        zone(
            id = "triceps",
            title = "Tríceps",
            generalName = "Tríceps braquial",
            front = false,
            crop = floatArrayOf(0.08f, 0.218f, 0.40f, 0.436f),
            hits = listOf(
                hit(0.12f, 0.258f, 0.36f, 0.397f),
                hit(0.64f, 0.258f, 0.88f, 0.397f),
            ),
            parts = listOf(
                part("Tríceps braquial (cabeza larga)", "Cabeza larga", 0.40f, 0.02f, 1.00f, 0.98f),
                part("Tríceps braquial (cabeza lateral)", "Cabeza lateral", 0.00f, 0.04f, 0.58f, 0.90f),
            ),
        ),
        zone(
            id = "lats",
            title = "Dorsal",
            generalName = "Dorsal ancho",
            front = false,
            crop = floatArrayOf(0.22f, 0.298f, 0.50f, 0.516f),
            hits = listOf(
                hit(0.26f, 0.317f, 0.46f, 0.496f),
                hit(0.54f, 0.317f, 0.74f, 0.496f),
            ),
            parts = listOf(
                part("Dorsal ancho", "Dorsal ancho", 0.06f, 0.04f, 0.94f, 0.96f),
            ),
        ),
        zone(
            id = "erectors",
            title = "Espalda baja",
            generalName = "Erectores espinales",
            front = false,
            crop = floatArrayOf(0.40f, 0.317f, 0.60f, 0.536f),
            hits = listOf(hit(0.44f, 0.337f, 0.56f, 0.496f)),
            parts = listOf(
                part("Erectores espinales", "Erectores", 0.22f, 0.02f, 0.78f, 0.98f),
            ),
        ),
        zone(
            id = "forearm_back",
            title = "Antebrazo",
            generalName = "Antebrazo",
            front = false,
            crop = floatArrayOf(0.00f, 0.337f, 0.30f, 0.536f),
            hits = listOf(
                hit(0.02f, 0.377f, 0.26f, 0.516f),
                hit(0.74f, 0.377f, 0.98f, 0.516f),
            ),
            parts = listOf(
                part("Antebrazo (extensores)", "Extensores", 0.06f, 0.04f, 0.94f, 0.96f),
            ),
        ),
        zone(
            id = "glutes",
            title = "Glúteo",
            generalName = "Glúteo",
            front = false,
            crop = floatArrayOf(0.30f, 0.456f, 0.70f, 0.655f),
            hits = listOf(
                hit(0.34f, 0.496f, 0.50f, 0.635f),
                hit(0.50f, 0.496f, 0.66f, 0.635f),
            ),
            parts = listOf(
                part("Glúteo medio", "Medio", 0.08f, 0.00f, 0.92f, 0.42f),
                part("Glúteo mayor", "Mayor", 0.04f, 0.36f, 0.96f, 1.00f),
            ),
        ),
        zone(
            id = "hamstrings",
            title = "Isquios",
            generalName = "Isquiosurales",
            front = false,
            crop = floatArrayOf(0.32f, 0.595f, 0.52f, 0.793f),
            hits = listOf(
                hit(0.36f, 0.625f, 0.49f, 0.774f),
                hit(0.51f, 0.625f, 0.64f, 0.774f),
            ),
            parts = listOf(
                part("Isquiosurales (bíceps femoral)", "Bíceps femoral", 0.00f, 0.02f, 0.55f, 0.98f),
                part("Isquiosurales (semitendinoso)", "Semitendinoso", 0.45f, 0.08f, 1.00f, 0.96f),
            ),
        ),
        zone(
            id = "calves",
            title = "Pantorrilla",
            generalName = "Pantorrilla",
            front = false,
            crop = floatArrayOf(0.34f, 0.734f, 0.54f, 0.952f),
            hits = listOf(
                hit(0.38f, 0.754f, 0.49f, 0.922f),
                hit(0.51f, 0.754f, 0.62f, 0.922f),
            ),
            parts = listOf(
                part("Gemelo (gastrocnemio)", "Gemelo", 0.06f, 0.00f, 0.94f, 0.58f),
                part("Sóleo", "Sóleo", 0.14f, 0.50f, 0.86f, 1.00f),
            ),
        ),
    )

    val knownNames: Set<String> = zones
        .flatMap { zone -> zone.parts.map { it.name } + zone.generalName }
        .map { it.lowercase() }
        .toSet()

    private fun zone(
        id: String,
        title: String,
        generalName: String,
        front: Boolean,
        crop: FloatArray,
        hits: List<MuscleSpot>,
        parts: List<MuscleSpot>,
    ) = MuscleZone(
        id = id,
        title = title,
        generalName = generalName,
        front = front,
        bodyHits = hits,
        cropLeft = crop[0],
        cropTop = crop[1],
        cropRight = crop[2],
        cropBottom = crop[3],
        parts = parts,
    )

    private fun hit(left: Float, top: Float, right: Float, bottom: Float) = MuscleSpot(
        name = "",
        label = "",
        left = left,
        top = top,
        right = right,
        bottom = bottom,
    )

    private fun part(
        name: String,
        label: String,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) = MuscleSpot(
        name = name,
        label = label,
        left = left,
        top = top,
        right = right,
        bottom = bottom,
    )
}
