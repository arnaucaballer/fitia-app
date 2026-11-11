package com.arnaucaballer.fitia.feature.training.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.training.domain.DeletePlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObserveActiveTrainingPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.ObservePlansUseCase
import com.arnaucaballer.fitia.feature.training.domain.SavePlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.SetActiveTrainingPlanUseCase
import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlansViewModel(
    observePlansUseCase: ObservePlansUseCase,
    observeActiveTrainingPlanUseCase: ObserveActiveTrainingPlanUseCase,
    private val savePlanUseCase: SavePlanUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val setActiveTrainingPlanUseCase: SetActiveTrainingPlanUseCase,
) : ViewModel() {

    val activePlanId: StateFlow<Long?> = observeActiveTrainingPlanUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val plans: StateFlow<List<TrainingPlan>> = observePlansUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun createPlan(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            savePlanUseCase(TrainingPlan(name = name.trim()))
        }
    }

    fun deletePlan(id: Long) {
        viewModelScope.launch {
            deletePlanUseCase(id)
        }
    }

    fun followPlan(planId: Long, onFollowed: () -> Unit) {
        viewModelScope.launch {
            setActiveTrainingPlanUseCase(planId)
            onFollowed()
        }
    }

    fun stopFollowingPlan() {
        viewModelScope.launch {
            setActiveTrainingPlanUseCase(null)
        }
    }
}
