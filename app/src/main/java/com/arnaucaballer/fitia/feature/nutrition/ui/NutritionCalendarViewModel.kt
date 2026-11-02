package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class NutritionCalendarViewModel(
    repository: NutritionRepository,
) : ViewModel() {
    val daysWithFood: StateFlow<List<Long>> = repository.observeDaysWithFood().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
}
