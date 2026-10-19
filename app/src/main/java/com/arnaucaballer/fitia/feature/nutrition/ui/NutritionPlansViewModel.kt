package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionPlan
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NutritionPlansViewModel(
    private val repository: NutritionRepository,
) : ViewModel() {

    val plans: StateFlow<List<NutritionPlan>> = repository.observePlans().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun createFromCurrentTargets(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val targets = repository.getTargets() ?: return@launch
            repository.savePlan(
                NutritionPlan(
                    name = name.trim(),
                    calories = targets.calories,
                    proteinGrams = targets.proteinGrams,
                    carbsGrams = targets.carbsGrams,
                    fatGrams = targets.fatGrams,
                ),
            )
        }
    }

    fun apply(planId: Long) {
        viewModelScope.launch {
            repository.applyPlan(planId)
        }
    }

    fun delete(planId: Long) {
        viewModelScope.launch {
            repository.deletePlan(planId)
        }
    }
}
