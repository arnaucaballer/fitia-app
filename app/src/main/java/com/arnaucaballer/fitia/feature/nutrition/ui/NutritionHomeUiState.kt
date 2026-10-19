package com.arnaucaballer.fitia.feature.nutrition.ui

import com.arnaucaballer.fitia.feature.nutrition.domain.BodyProfile
import com.arnaucaballer.fitia.feature.nutrition.domain.DayLog
import com.arnaucaballer.fitia.feature.nutrition.domain.MacroTargets
import java.time.LocalDate

data class NutritionHomeUiState(
    val dayEpochDay: Long = LocalDate.now().toEpochDay(),
    val dayLog: DayLog = DayLog(dayEpochDay = LocalDate.now().toEpochDay(), meals = emptyList()),
    val targets: MacroTargets? = null,
    val profile: BodyProfile? = null,
)
