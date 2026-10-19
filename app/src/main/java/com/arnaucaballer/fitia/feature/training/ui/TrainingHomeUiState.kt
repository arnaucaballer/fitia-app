package com.arnaucaballer.fitia.feature.training.ui

import com.arnaucaballer.fitia.feature.training.domain.TrainingPlan
import com.arnaucaballer.fitia.feature.training.domain.TrainingSession
import com.arnaucaballer.fitia.feature.training.domain.Workout

data class TrainingHomeUiState(
    val plans: List<TrainingPlan> = emptyList(),
    val workouts: List<Workout> = emptyList(),
    val activeSession: TrainingSession? = null,
)
