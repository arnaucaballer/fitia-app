package com.arnaucaballer.fitia.feature.training.ui

object TrainingRoutes {
    const val Home = "training/home"
    const val Exercises = "training/exercises"
    const val Workouts = "training/workouts"
    const val WorkoutDetail = "training/workout/{workoutId}"
    const val Plans = "training/plans"
    const val PlanDetail = "training/plan/{planId}"
    const val Session = "training/session/{sessionId}"
    const val History = "training/history"

    fun workoutDetail(workoutId: Long) = "training/workout/$workoutId"
    fun planDetail(planId: Long) = "training/plan/$planId"
    fun session(sessionId: Long) = "training/session/$sessionId"
}
