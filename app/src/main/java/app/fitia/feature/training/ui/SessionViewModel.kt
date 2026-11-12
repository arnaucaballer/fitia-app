package app.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitia.feature.training.domain.AddExerciseToSessionUseCase
import app.fitia.feature.training.domain.DeleteSessionSetUseCase
import app.fitia.feature.training.domain.Exercise
import app.fitia.feature.training.domain.ExerciseMuscle
import app.fitia.feature.training.domain.FinishSessionUseCase
import app.fitia.feature.training.domain.GetSessionUseCase
import app.fitia.feature.training.domain.LogSessionSetUseCase
import app.fitia.feature.training.domain.Muscle
import app.fitia.feature.training.domain.ObserveExercisesUseCase
import app.fitia.feature.training.domain.ObserveMusclesUseCase
import app.fitia.feature.training.domain.SaveExerciseUseCase
import app.fitia.feature.training.domain.SaveMuscleUseCase
import app.fitia.feature.training.domain.SuggestExerciseMusclesUseCase
import app.fitia.feature.training.domain.TrainingSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel(
    private val sessionId: Long,
    private val getSessionUseCase: GetSessionUseCase,
    private val logSessionSetUseCase: LogSessionSetUseCase,
    private val deleteSessionSetUseCase: DeleteSessionSetUseCase,
    private val finishSessionUseCase: FinishSessionUseCase,
    private val addExerciseToSessionUseCase: AddExerciseToSessionUseCase,
    observeExercisesUseCase: ObserveExercisesUseCase,
    observeMusclesUseCase: ObserveMusclesUseCase,
    private val saveExerciseUseCase: SaveExerciseUseCase,
    private val saveMuscleUseCase: SaveMuscleUseCase,
    private val suggestExerciseMusclesUseCase: SuggestExerciseMusclesUseCase,
) : ViewModel() {

    private val _session = MutableStateFlow<TrainingSession?>(null)
    val session: StateFlow<TrainingSession?> = _session.asStateFlow()

    private val _finished = MutableSharedFlow<Unit>()
    val finished: SharedFlow<Unit> = _finished.asSharedFlow()

    val exercises: StateFlow<List<Exercise>> = observeExercisesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val muscles: StateFlow<List<Muscle>> = observeMusclesUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private var writeJob: Job = viewModelScope.launch { }
    private var suggestJob: Job? = null

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

    fun addExercise(exercise: Exercise) {
        if (exercise.id == 0L) return
        enqueueWrite { insertRegisteredExercise(exercise.id) }
    }

    fun addNewExercise(name: String, muscleIds: List<Long>) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || muscleIds.isEmpty()) return
        viewModelScope.launch {
            val existing = exercises.value.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
            val exerciseId = existing?.id ?: saveExerciseUseCase(
                Exercise(
                    name = trimmed,
                    muscleId = muscleIds.first(),
                    muscles = muscleIds.distinct().map { id ->
                        val known = muscles.value.firstOrNull { it.id == id }
                        ExerciseMuscle(id, known?.name.orEmpty())
                    },
                ),
            )
            enqueueWrite { insertRegisteredExercise(exerciseId) }
        }
    }

    fun suggestMuscles(
        exerciseName: String,
        onFinished: (Result<List<ExerciseMuscle>>) -> Unit,
    ) {
        suggestJob?.cancel()
        suggestJob = viewModelScope.launch {
            onFinished(runCatching { suggestExerciseMusclesUseCase(exerciseName) })
        }
    }

    fun createMuscle(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            saveMuscleUseCase(Muscle(name = name.trim()))
        }
    }

    private suspend fun insertRegisteredExercise(exerciseId: Long) {
        addExerciseToSessionUseCase(sessionId, exerciseId)
        _session.value = getSessionUseCase(sessionId)
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
