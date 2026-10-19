package com.arnaucaballer.fitia.feature.nutrition.domain

class SaveManualTargetsUseCase(
    private val repository: NutritionRepository,
) {
    suspend operator fun invoke(targets: MacroTargets) {
        repository.saveTargets(targets.copy(manualOverride = true))
    }
}
