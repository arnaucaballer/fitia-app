package app.fitia.feature.nutrition.ui

import app.fitia.feature.nutrition.domain.BodyProfile
import app.fitia.feature.nutrition.domain.DayLog
import app.fitia.feature.nutrition.domain.MacroTargets
import app.fitia.feature.nutrition.domain.NutritionPlan
import app.fitia.feature.nutrition.domain.WaterLog
import java.time.LocalDate

data class NutritionHomeUiState(
    val dayEpochDay: Long = LocalDate.now().toEpochDay(),
    val dayLog: DayLog = DayLog(dayEpochDay = LocalDate.now().toEpochDay(), meals = emptyList()),
    val targets: MacroTargets? = null,
    val profile: BodyProfile? = null,
    val plans: List<NutritionPlan> = emptyList(),
    val waterMl: Int = 0,
    val waterLogs: List<WaterLog> = emptyList(),
)
