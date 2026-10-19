package com.arnaucaballer.fitia.feature.nutrition.domain

class RecalculateTargetsUseCase(
    private val repository: NutritionRepository,
) {
    suspend operator fun invoke(
        profile: BodyProfile,
        force: Boolean = false,
        proteinPerKg: Double? = null,
        fatPerKg: Double? = null,
        calorieAdjustment: Int? = null,
    ): MacroTargets {
        val current = repository.getTargets()
        if (current?.manualOverride == true && !force) {
            return current
        }
        val suggested = MacroCalculator.suggestTargets(
            profile = profile,
            proteinPerKg = proteinPerKg ?: current?.proteinPerKg
                ?: MacroCalculator.DEFAULT_PROTEIN_PER_KG,
            fatPerKg = fatPerKg ?: current?.fatPerKg ?: MacroCalculator.DEFAULT_FAT_PER_KG,
            calorieAdjustment = calorieAdjustment
                ?: current?.calorieAdjustment
                ?: profile.goal.defaultCalorieAdjustment,
        )
        repository.saveProfile(profile)
        repository.saveTargets(suggested)
        return suggested
    }
}
