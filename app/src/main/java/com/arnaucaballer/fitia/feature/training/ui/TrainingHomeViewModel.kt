package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.ObserveActiveSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObservePlansUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveWorkoutsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TrainingHomeViewModel(
    observePlansUseCase: ObservePlansUseCase,
    observeWorkoutsUseCase: ObserveWorkoutsUseCase,
    observeActiveSessionUseCase: ObserveActiveSessionUseCase,
) : ViewModel() {

    val uiState: StateFlow<TrainingHomeUiState> = combine(
        observePlansUseCase(),
        observeWorkoutsUseCase(),
        observeActiveSessionUseCase(),
    ) { plans, workouts, activeSession ->
        TrainingHomeUiState(
            plans = plans,
            workouts = workouts,
            activeSession = activeSession,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingHomeUiState(),
    )
}
