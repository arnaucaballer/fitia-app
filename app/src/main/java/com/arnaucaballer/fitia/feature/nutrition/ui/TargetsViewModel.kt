package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.nutrition.domain.MacroTargets
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import com.arnaucaballer.fitia.feature.nutrition.domain.RecalculateTargetsUseCase
import com.arnaucaballer.fitia.feature.nutrition.domain.SaveManualTargetsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TargetsViewModel(
    repository: NutritionRepository,
    private val saveManualTargetsUseCase: SaveManualTargetsUseCase,
    private val recalculateTargetsUseCase: RecalculateTargetsUseCase,
) : ViewModel() {

    private val nutritionRepository = repository

    val targets: StateFlow<MacroTargets?> = repository.observeTargets().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun saveManual(targets: MacroTargets) {
        viewModelScope.launch {
            saveManualTargetsUseCase(targets)
        }
    }

    fun recalculate() {
        viewModelScope.launch {
            val profile = nutritionRepository.getProfile() ?: return@launch
            recalculateTargetsUseCase(profile, force = true)
        }
    }
}
