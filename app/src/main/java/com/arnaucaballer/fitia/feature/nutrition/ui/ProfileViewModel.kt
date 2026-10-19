package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.nutrition.domain.ActivityLevel
import com.arnaucaballer.fitia.feature.nutrition.domain.BodyProfile
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionGoal
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import com.arnaucaballer.fitia.feature.nutrition.domain.RecalculateTargetsUseCase
import com.arnaucaballer.fitia.feature.nutrition.domain.Sex
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    repository: NutritionRepository,
    private val recalculateTargetsUseCase: RecalculateTargetsUseCase,
) : ViewModel() {

    val profile: StateFlow<BodyProfile?> = repository.observeProfile().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun save(
        weightKg: Double,
        heightCm: Double,
        ageYears: Int,
        sex: Sex,
        activityLevel: ActivityLevel,
        goal: NutritionGoal,
        recalculate: Boolean,
    ) {
        viewModelScope.launch {
            val profile = BodyProfile(
                weightKg = weightKg,
                heightCm = heightCm,
                ageYears = ageYears,
                sex = sex,
                activityLevel = activityLevel,
                goal = goal,
            )
            if (recalculate) {
                recalculateTargetsUseCase(profile, force = true)
            } else {
                recalculateTargetsUseCase(profile, force = false)
            }
        }
    }
}
