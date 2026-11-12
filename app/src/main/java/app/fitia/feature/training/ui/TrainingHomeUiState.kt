package app.fitia.feature.training.ui

import app.fitia.feature.training.domain.TrainingPlan
import app.fitia.feature.training.domain.TrainingSession
import app.fitia.feature.training.domain.Workout

data class TrainingHomeUiState(
    val plans: List<TrainingPlan> = emptyList(),
    val genericWorkouts: List<Workout> = emptyList(),
    val activeSession: TrainingSession? = null,
)
