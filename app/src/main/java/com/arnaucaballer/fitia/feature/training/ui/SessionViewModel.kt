package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.DeleteSessionSetUseCase
import com.arnaucaballer.fitia.feature.training.domain.FinishSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.GetSessionUseCase
import com.arnaucaballer.fitia.feature.training.domain.LogSessionSetUseCase
import com.arnaucaballer.fitia.feature.training.domain.TrainingSession
import kotlinx.coroutines.Job
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
    private val deleteSessionSetUseCase: DeleteSessionSetUseCase,
    private val finishSessionUseCase: FinishSessionUseCase,
) : ViewModel() {

    private val _session = MutableStateFlow<TrainingSession?>(null)
    val session: StateFlow<TrainingSession?> = _session.asStateFlow()

    private val _finished = MutableSharedFlow<Unit>()
    val finished: SharedFlow<Unit> = _finished.asSharedFlow()

    private var writeJob: Job = viewModelScope.launch { }

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
        enqueueWrite {
            logSessionSetUseCase(
                sessionExerciseId = sessionExerciseId,
                setId = setId,
                setIndex = setIndex,
                weightKg = weightKg,
                reps = reps,
                isCompleted = isCompleted,
            )
        }
    }

    fun addSet(sessionExerciseId: Long) {
        val exercise = _session.value?.exercises?.firstOrNull { it.id == sessionExerciseId } ?: return
        val setIndex = exercise.sets.size
        enqueueWrite {
            logSessionSetUseCase(
                sessionExerciseId = sessionExerciseId,
                setId = null,
                setIndex = setIndex,
                weightKg = null,
                reps = null,
                isCompleted = false,
            )
            _session.value = getSessionUseCase(sessionId)
        }
    }

    fun removeSet(setId: Long) {
        enqueueWrite {
            deleteSessionSetUseCase(setId)
            _session.value = getSessionUseCase(sessionId)
        }
    }

    private fun enqueueWrite(block: suspend () -> Unit) {
        val previous = writeJob
        writeJob = viewModelScope.launch {
            previous.join()
            block()
        }
    }

    fun finish() {
        viewModelScope.launch {
            finishSessionUseCase(sessionId)
            _finished.emit(Unit)
        }
    }
}
