package app.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitia.feature.training.domain.DeleteWorkoutUseCase
import app.fitia.feature.training.domain.Exercise
import app.fitia.feature.training.domain.ExerciseMuscle
import app.fitia.feature.training.domain.GetWorkoutUseCase
import app.fitia.feature.training.domain.GetLatestSessionForWorkoutUseCase
import app.fitia.feature.training.domain.Muscle
import app.fitia.feature.training.domain.ObserveExercisesUseCase
import app.fitia.feature.training.domain.ObserveMusclesUseCase
import app.fitia.feature.training.domain.ObserveSessionsUseCase
import app.fitia.feature.training.domain.SaveExerciseUseCase
import app.fitia.feature.training.domain.SaveMuscleUseCase
import app.fitia.feature.training.domain.SaveWorkoutUseCase
import app.fitia.feature.training.domain.SetTarget
import app.fitia.feature.training.domain.SessionStatus
import app.fitia.feature.training.domain.StartSessionFromWorkoutUseCase
import app.fitia.feature.training.domain.SuggestExerciseMusclesUseCase
import app.fitia.feature.training.domain.TrainingSession
import app.fitia.feature.training.domain.UpdateWorkoutSetTargetUseCase
import app.fitia.feature.training.domain.Workout
import app.fitia.feature.training.domain.WorkoutExercise
import app.fitia.feature.training.domain.mergedWithHistory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(
    private val workoutId: Long,
    private val getWorkoutUseCase: GetWorkoutUseCase,
    private val getLatestSessionForWorkoutUseCase: GetLatestSessionForWorkoutUseCase,
    observeExercisesUseCase: ObserveExercisesUseCase,
    observeSessionsUseCase: ObserveSessionsUseCase,
    observeMusclesUseCase: ObserveMusclesUseCase,
    private val saveExerciseUseCase: SaveExerciseUseCase,
    private val saveMuscleUseCase: SaveMuscleUseCase,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val updateWorkoutSetTargetUseCase: UpdateWorkoutSetTargetUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val startSessionFromWorkoutUseCase: StartSessionFromWorkoutUseCase,
    private val suggestExerciseMusclesUseCase: SuggestExerciseMusclesUseCase,
) : ViewModel() {
    private var suggestJob: Job? = null

    private val _workout = MutableStateFlow<Workout?>(null)
    val workout: StateFlow<Workout?> = _workout.asStateFlow()

    private val _lastSessionAtEpochMs = MutableStateFlow<Long?>(null)
    val lastSessionAtEpochMs: StateFlow<Long?> = _lastSessionAtEpochMs.asStateFlow()

    val previousSessions: StateFlow<List<TrainingSession>> = observeSessionsUseCase()
        .map { sessions ->
            sessions
                .filter { it.workoutId == workoutId && it.status == SessionStatus.Completed }
                .take(5)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

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

    private val _sessionStarted = MutableSharedFlow<Long>()
    val sessionStarted: SharedFlow<Long> = _sessionStarted.asSharedFlow()

    private val _deleted = MutableSharedFlow<Unit>()
    val deleted: SharedFlow<Unit> = _deleted.asSharedFlow()

    private var saveJob: Job = viewModelScope.launch { }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val stored = getWorkoutUseCase(workoutId)
            val history = getLatestSessionForWorkoutUseCase(workoutId)
            _lastSessionAtEpochMs.value = history?.startedAtEpochMs
            // Only the first load borrows the last session's numbers; later refreshes
            // reflect what is stored, so edits are not overwritten by old values.
            _workout.value = if (_workout.value == null) {
                stored?.mergedWithHistory(history)
            } else {
                stored
            }
        }
    }

    fun addExercise(exercise: Exercise) {
        val current = _workout.value ?: return
        val next = current.copy(
            exercises = current.exercises + WorkoutExercise(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                sortOrder = current.exercises.size,
                setTargets = listOf(SetTarget(setIndex = 0)),
            ),
        )
        persist(next)
    }

    fun addNewExercise(name: String, muscleIds: List<Long>) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || muscleIds.isEmpty()) return
        viewModelScope.launch {
            val existing = exercises.value.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
            if (existing != null) {
                addExercise(existing)
                return@launch
            }
            val resolved = muscleIds.distinct().map { id ->
                val known = muscles.value.firstOrNull { it.id == id }
                ExerciseMuscle(id, known?.name.orEmpty())
            }
            val id = saveExerciseUseCase(
                Exercise(
                    name = trimmed,
                    muscleId = muscleIds.first(),
                    muscles = resolved,
                ),
            )
            addExercise(
                Exercise(
                    id = id,
                    name = trimmed,
                    muscleId = muscleIds.first(),
                    muscles = resolved,
                ),
            )
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

    fun addSet(sortOrder: Int) {
        val current = _workout.value ?: return
        val exercises = current.exercises.map { item ->
            if (item.sortOrder != sortOrder) {
                item
            } else {
                item.copy(
                    setTargets = item.setTargets + SetTarget(
                        setIndex = item.setTargets.size,
                        targetReps = null,
                        targetWeightKg = null,
                    ),
                )
            }
        }
        persist(current.copy(exercises = exercises))
    }

    fun removeSet(sortOrder: Int, setIndex: Int) {
        val current = _workout.value ?: return
        val exercises = current.exercises.map { item ->
            if (item.sortOrder != sortOrder) {
                item
            } else {
                item.copy(
                    setTargets = item.setTargets
                        .filterNot { it.setIndex == setIndex }
                        .mapIndexed { index, target -> target.copy(setIndex = index) },
                )
            }
        }
        persist(current.copy(exercises = exercises))
    }

    fun updateSet(
        sortOrder: Int,
        setIndex: Int,
        weightKg: Double?,
        reps: Int?,
    ) {
        val current = _workout.value ?: return
        val exercise = current.exercises.firstOrNull { it.sortOrder == sortOrder } ?: return
        val existing = exercise.setTargets.firstOrNull { it.setIndex == setIndex } ?: return
        if (existing.targetWeightKg == weightKg && existing.targetReps == reps) return
        val exercises = current.exercises.map { item ->
            if (item.sortOrder != sortOrder) {
                item
            } else {
                item.copy(
                    setTargets = item.setTargets.map { target ->
                        if (target.setIndex == setIndex) {
                            target.copy(targetWeightKg = weightKg, targetReps = reps)
                        } else {
                            target
                        }
                    },
                )
            }
        }
        val next = current.copy(exercises = exercises)
        _workout.value = next
        val workoutExerciseId = exercise.id
        enqueueSave {
            val updated = updateWorkoutSetTargetUseCase(
                workoutExerciseId,
                setIndex,
                weightKg,
                reps,
            )
            if (!updated) saveWorkoutUseCase(next)
        }
    }

    fun removeExercise(sortOrder: Int) {
        val current = _workout.value ?: return
        val exercises = current.exercises
            .filterNot { it.sortOrder == sortOrder }
            .mapIndexed { index, item -> item.copy(sortOrder = index) }
        persist(current.copy(exercises = exercises))
    }

    fun startSession() {
        val pending = saveJob
        viewModelScope.launch {
            pending.join()
            val sessionId = startSessionFromWorkoutUseCase(workoutId)
            _sessionStarted.emit(sessionId)
        }
    }

    fun deleteWorkout() {
        viewModelScope.launch {
            deleteWorkoutUseCase(workoutId)
            _deleted.emit(Unit)
        }
    }

    private fun persist(workout: Workout) {
        _workout.value = workout
        enqueueSave { saveWorkoutUseCase(workout) }
    }

    private fun enqueueSave(block: suspend () -> Unit) {
        val previous = saveJob
        saveJob = viewModelScope.launch {
            previous.join()
            block()
        }
    }
}
