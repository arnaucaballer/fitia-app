package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    private val dayLogFlow = selectedDay.flatMapLatest { day ->
        flow {
            repository.ensureDefaultMeals(day)
            emitAll(repository.observeDayLog(day))
        }
    }

    private val waterFlow = selectedDay.flatMapLatest { day ->
        repository.observeWaterTotal(day)
    }

    val uiState: StateFlow<NutritionHomeUiState> = combine(
        combine(
            dayLogFlow,
            waterFlow,
            repository.observeTargets(),
            repository.observeProfile(),
            repository.observePlans(),
        ) { dayLog, waterMl, targets, profile, plans ->
            Quint(dayLog, waterMl, targets, profile, plans)
        },
        selectedDay,
    ) { quint, day ->
        NutritionHomeUiState(
            dayEpochDay = day,
            dayLog = quint.a,
            waterMl = quint.b,
            targets = quint.c,
            profile = quint.d,
            plans = quint.e,
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

    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(selectedDay.value, amountMl)
        }
    }

    private data class Quint<A, B, C, D, E>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
        val e: E,
    )
}
