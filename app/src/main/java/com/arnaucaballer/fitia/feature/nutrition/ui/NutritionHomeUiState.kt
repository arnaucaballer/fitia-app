package com.arnaucaballer.fitia.feature.nutrition.ui

import com.arnaucaballer.fitia.feature.nutrition.domain.BodyProfile
import com.arnaucaballer.fitia.feature.nutrition.domain.DayLog
import com.arnaucaballer.fitia.feature.nutrition.domain.MacroTargets
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionPlan
import java.time.LocalDate

data class NutritionHomeUiState(
    val dayEpochDay: Long = LocalDate.now().toEpochDay(),
    val dayLog: DayLog = DayLog(dayEpochDay = LocalDate.now().toEpochDay(), meals = emptyList()),
    val targets: MacroTargets? = null,
    val profile: BodyProfile? = null,
    val plans: List<NutritionPlan> = emptyList(),
    val waterMl: Int = 0,
)
