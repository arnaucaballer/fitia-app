package app.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitia.feature.training.domain.DeletePlanUseCase
import app.fitia.feature.training.domain.DeleteSessionUseCase
import app.fitia.feature.training.domain.DeleteWorkoutUseCase
import app.fitia.feature.training.domain.GetPlanUseCase
import app.fitia.feature.training.domain.GetSessionUseCase
import app.fitia.feature.training.domain.GetWorkoutUseCase
import app.fitia.feature.training.domain.ObserveActiveSessionUseCase
import app.fitia.feature.training.domain.ObserveGenericWorkoutsUseCase
import app.fitia.feature.training.domain.ObservePlansUseCase
import app.fitia.feature.training.domain.RenameSessionUseCase
import app.fitia.feature.training.domain.RestoreSessionUseCase
import app.fitia.feature.training.domain.SavePlanUseCase
import app.fitia.feature.training.domain.SaveWorkoutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrainingHomeViewModel(
    observePlansUseCase: ObservePlansUseCase,
    observeGenericWorkoutsUseCase: ObserveGenericWorkoutsUseCase,
    observeActiveSessionUseCase: ObserveActiveSessionUseCase,
    private val getPlanUseCase: GetPlanUseCase,
    private val savePlanUseCase: SavePlanUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val getWorkoutUseCase: GetWorkoutUseCase,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val renameSessionUseCase: RenameSessionUseCase,
) : ViewModel() {

    private var undoToken = 0
    private var pendingUndo: (suspend () -> Unit)? = null
    private val _undoPrompts = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val undoPrompts: SharedFlow<Int> = _undoPrompts.asSharedFlow()

    val uiState: StateFlow<TrainingHomeUiState> = combine(
        observePlansUseCase().catch { emit(emptyList()) },
        observeGenericWorkoutsUseCase().catch { emit(emptyList()) },
        observeActiveSessionUseCase().catch { emit(null) },
    ) { plans, workouts, activeSession ->
        TrainingHomeUiState(
            plans = plans,
            genericWorkouts = workouts,
            activeSession = activeSession,
        )
    }.catch {
        emit(TrainingHomeUiState())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingHomeUiState(),
    )

    fun renamePlan(planId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val plan = getPlanUseCase(planId) ?: return@launch
            savePlanUseCase(plan.copy(name = trimmed))
        }
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch {
            val plan = getPlanUseCase(planId) ?: return@launch
            val owned = plan.workouts.filter { it.planId == planId }
            deletePlanUseCase(planId)
            owned.forEach { workout -> deleteWorkoutUseCase(workout.id) }
            offerUndo {
                owned.forEach { workout -> saveWorkoutUseCase(workout) }
                savePlanUseCase(plan)
            }
        }
    }

    fun removeWorkoutFromPlan(planId: Long, workoutId: Long) {
        viewModelScope.launch {
            val plan = getPlanUseCase(planId) ?: return@launch
            val workout = plan.workouts.firstOrNull { it.id == workoutId } ?: return@launch
            savePlanUseCase(plan.copy(workouts = plan.workouts.filterNot { it.id == workoutId }))
            if (!workout.isGeneric) {
                deleteWorkoutUseCase(workout.id)
            }
            offerUndo {
                if (!workout.isGeneric) {
                    saveWorkoutUseCase(workout)
                }
                savePlanUseCase(plan)
            }
        }
    }

    fun renameWorkout(workoutId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val workout = getWorkoutUseCase(workoutId) ?: return@launch
            saveWorkoutUseCase(workout.copy(name = trimmed))
        }
    }

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch {
            val workout = getWorkoutUseCase(workoutId) ?: return@launch
            deleteWorkoutUseCase(workoutId)
            offerUndo { saveWorkoutUseCase(workout) }
        }
    }

    fun renameSession(sessionId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            renameSessionUseCase(sessionId, trimmed)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            val session = getSessionUseCase(sessionId) ?: return@launch
            deleteSessionUseCase(sessionId)
            offerUndo { restoreSessionUseCase(session) }
        }
    }

    fun undo(token: Int) {
        if (token != undoToken) return
        val action = pendingUndo ?: return
        pendingUndo = null
        viewModelScope.launch { action() }
    }

    private fun offerUndo(action: suspend () -> Unit) {
        undoToken += 1
        pendingUndo = action
        _undoPrompts.tryEmit(undoToken)
    }
}
