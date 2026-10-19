package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.FinishSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.LogSessionSetUseCase
import com.arnaucaballer.fitia.feature.training.domain.TrainingSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionViewModel(
    private val sessionId: Long,
    private val getSessionUseCase: GetSessionUseCase,
    private val logSessionSetUseCase: LogSessionSetUseCase,
    private val finishSessionUseCase: FinishSessionUseCase,
) : ViewModel() {

    private val _session = MutableStateFlow<TrainingSession?>(null)
    val session: StateFlow<TrainingSession?> = _session.asStateFlow()

    private val _finished = MutableSharedFlow<Unit>()
    val finished: SharedFlow<Unit> = _finished.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _session.value = getSessionUseCase(sessionId)
        }
    }

    fun saveSet(
        sessionExerciseId: Long,
        setId: Long,
        setIndex: Int,
        weightKg: Double?,
        reps: Int?,
        isCompleted: Boolean,
    ) {
        viewModelScope.launch {
            logSessionSetUseCase(
                sessionExerciseId = sessionExerciseId,
                setId = setId,
                setIndex = setIndex,
                weightKg = weightKg,
                reps = reps,
                isCompleted = isCompleted,
            )
            refresh()
        }
    }

    fun finish() {
        viewModelScope.launch {
            finishSessionUseCase(sessionId)
            _finished.emit(Unit)
        }
    }
}
