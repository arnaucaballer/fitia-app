package com.arnaucaballer.fitia.feature.training.domain

data class Exercise(
    val id: Long = 0,
    val name: String,
    val muscleId: Long? = null,
    /** Resolved muscle label for UI; falls back to legacy [muscleGroup]. */
    val muscleName: String = "",
    val muscleGroup: String = "",
    val notes: String = "",
    val createdAtEpochMs: Long = 0,
    /** Every muscle this exercise trains. The first entry is the primary one. */
    val muscles: List<ExerciseMuscle> = emptyList(),
) {
    /** Muscles to persist: the list, or the single legacy [muscleId] when empty. */
    val muscleIds: List<Long>
        get() = muscles.map { it.id }
            .ifEmpty { listOfNotNull(muscleId) }
            .distinct()

    /** Label shown in lists: the primary muscle. */
    val primaryMuscleName: String
        get() = muscles.firstOrNull()?.name ?: muscleName

    /** Extra muscles beyond the primary, for the row subtitle. */
    val extraMuscleNames: List<String>
        get() = muscles.drop(1).map { it.name }
}
