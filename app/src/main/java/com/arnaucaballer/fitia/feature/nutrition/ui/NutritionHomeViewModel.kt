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

    val uiState: StateFlow<NutritionHomeUiState> = combine(
        selectedDay.flatMapLatest { day ->
            flow {
                repository.ensureDefaultMeals(day)
                emitAll(repository.observeDayLog(day))
            }
        },
        repository.observeTargets(),
        repository.observeProfile(),
        selectedDay,
    ) { dayLog, targets, profile, day ->
        NutritionHomeUiState(
            dayEpochDay = day,
            dayLog = dayLog,
            targets = targets,
            profile = profile,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionHomeUiState(),
    )

    fun shiftDay(delta: Long) {
        selectedDay.value = selectedDay.value + delta
    }

    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
        }
    }
}
