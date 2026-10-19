package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.ObserveActiveSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveGenericWorkoutsUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObservePlansUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TrainingHomeViewModel(
    observePlansUseCase: ObservePlansUseCase,
    observeGenericWorkoutsUseCase: ObserveGenericWorkoutsUseCase,
    observeActiveSessionUseCase: ObserveActiveSessionUseCase,
) : ViewModel() {

    val uiState: StateFlow<TrainingHomeUiState> = combine(
        observePlansUseCase(),
        observeGenericWorkoutsUseCase(),
        observeActiveSessionUseCase(),
    ) { plans, workouts, activeSession ->
        TrainingHomeUiState(
            plans = plans,
            genericWorkouts = workouts,
            activeSession = activeSession,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingHomeUiState(),
    )
}
