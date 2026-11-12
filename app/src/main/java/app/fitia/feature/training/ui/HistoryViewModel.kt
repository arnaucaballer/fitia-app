package app.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitia.feature.training.domain.ObserveSessionsUseCase
import app.fitia.feature.training.domain.TrainingSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    observeSessionsUseCase: ObserveSessionsUseCase,
) : ViewModel() {
    val sessions: StateFlow<List<TrainingSession>> = observeSessionsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
}
