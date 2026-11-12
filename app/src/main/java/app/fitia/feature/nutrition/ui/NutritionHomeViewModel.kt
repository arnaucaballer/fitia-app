package app.fitia.feature.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fitia.feature.nutrition.domain.Meal
import app.fitia.feature.nutrition.domain.MealEntry
import app.fitia.feature.nutrition.domain.NutritionPlan
import app.fitia.feature.nutrition.domain.NutritionRepository
import app.fitia.feature.nutrition.domain.WaterLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionHomeViewModel(
    private val repository: NutritionRepository,
) : ViewModel() {

    private val selectedDay = MutableStateFlow(LocalDate.now().toEpochDay())

    private var undoToken = 0
    private var pendingUndo: (suspend () -> Unit)? = null
    private val _undoPrompts = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val undoPrompts: SharedFlow<Int> = _undoPrompts.asSharedFlow()

    private val dayLogFlow = selectedDay.flatMapLatest { day ->
        flow {
            repository.ensureDefaultMeals(day)
            emitAll(repository.observeDayLog(day))
        }
    }

    private val waterLogsFlow = selectedDay.flatMapLatest { day ->
        repository.observeWaterLogs(day)
    }

    val uiState: StateFlow<NutritionHomeUiState> = combine(
        combine(
            dayLogFlow,
            waterLogsFlow,
            repository.observeTargets(),
            repository.observeProfile(),
        ) { dayLog, waterLogs, targets, profile ->
            Quad(dayLog, waterLogs, targets, profile)
        },
        combine(
            repository.observePlans(),
            selectedDay,
        ) { plans, day ->
            plans to day
        },
    ) { quad, plansAndDay ->
        val (plans, day) = plansAndDay
        NutritionHomeUiState(
            dayEpochDay = day,
            dayLog = quad.a,
            waterMl = quad.b.sumOf { it.amountMl },
            waterLogs = quad.b,
            targets = quad.c,
            profile = quad.d,
            plans = plans,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionHomeUiState(),
    )

    fun shiftDay(delta: Long) {
        selectedDay.value = selectedDay.value + delta
    }

    fun selectDay(dayEpochDay: Long) {
        selectedDay.value = dayEpochDay
    }

    fun addMeal(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addMeal(selectedDay.value, trimmed)
        }
    }

    fun renameMeal(mealId: Long, name: String) {
        viewModelScope.launch {
            repository.renameMeal(mealId, name)
        }
    }

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch {
            repository.deleteMeal(meal.id)
            offerUndo { repository.restoreMeal(meal) }
        }
    }

    fun updateEntry(entry: MealEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry)
        }
    }

    fun deleteEntry(entry: MealEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry.id)
            offerUndo { repository.addEntry(entry) }
        }
    }

    fun savePlan(plan: NutritionPlan) {
        viewModelScope.launch {
            repository.savePlan(plan)
        }
    }

    fun deletePlan(plan: NutritionPlan) {
        viewModelScope.launch {
            repository.deletePlan(plan.id)
            offerUndo { repository.savePlan(plan) }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(selectedDay.value, amountMl)
        }
    }

    fun deleteWater(log: WaterLog) {
        viewModelScope.launch {
            repository.deleteWaterLog(log.id)
            offerUndo { repository.restoreWaterLog(log) }
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

    private data class Quad<A, B, C, D>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
    )
}
