package com.arnaucaballer.fitia.feature.nutrition.domain

data class DayLog(
    val dayEpochDay: Long,
    val meals: List<Meal>,
) {
    val totals: MacroPortion
        get() {
            val entries = meals.flatMap { it.entries }
            return MacroPortion(
                grams = entries.sumOf { it.grams },
                calories = entries.sumOf { it.calories },
                proteinGrams = entries.sumOf { it.proteinGrams },
                carbsGrams = entries.sumOf { it.carbsGrams },
                fatGrams = entries.sumOf { it.fatGrams },
            )
        }
}
