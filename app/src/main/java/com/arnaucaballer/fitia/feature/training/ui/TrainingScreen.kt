package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun TrainingScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TrainingRoutes.Home,
        modifier = modifier,
    ) {
        composable(TrainingRoutes.Home) {
            TrainingHomeScreen(
                onOpenPlans = { navController.navigate(TrainingRoutes.Plans) },
                onOpenWorkouts = { navController.navigate(TrainingRoutes.Workouts) },
                onOpenExercises = { navController.navigate(TrainingRoutes.Exercises) },
                onOpenHistory = { navController.navigate(TrainingRoutes.History) },
                onOpenPlan = { id -> navController.navigate(TrainingRoutes.planDetail(id)) },
                onOpenWorkout = { id -> navController.navigate(TrainingRoutes.workoutDetail(id)) },
                onOpenSession = { id -> navController.navigate(TrainingRoutes.session(id)) },
            )
        }
        composable(TrainingRoutes.Exercises) {
            ExercisesScreen(onBack = { navController.popBackStack() })
        }
        composable(TrainingRoutes.Workouts) {
            WorkoutsScreen(
                onBack = { navController.popBackStack() },
                onOpenWorkout = { id -> navController.navigate(TrainingRoutes.workoutDetail(id)) },
                onOpenSession = { id -> navController.navigate(TrainingRoutes.session(id)) },
            )
        }
        composable(
            route = TrainingRoutes.WorkoutDetail,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) { entry ->
            val workoutId = entry.arguments?.getLong("workoutId") ?: return@composable
            WorkoutDetailScreen(
                workoutId = workoutId,
                onBack = { navController.popBackStack() },
                onOpenSession = { id -> navController.navigate(TrainingRoutes.session(id)) },
            )
        }
        composable(TrainingRoutes.Plans) {
            PlansScreen(
                onBack = { navController.popBackStack() },
                onOpenPlan = { id -> navController.navigate(TrainingRoutes.planDetail(id)) },
            )
        }
        composable(
            route = TrainingRoutes.PlanDetail,
            arguments = listOf(navArgument("planId") { type = NavType.LongType }),
        ) { entry ->
            val planId = entry.arguments?.getLong("planId") ?: return@composable
            PlanDetailScreen(
                planId = planId,
                onBack = { navController.popBackStack() },
                onOpenWorkout = { id -> navController.navigate(TrainingRoutes.workoutDetail(id)) },
            )
        }
        composable(
            route = TrainingRoutes.Session,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
        ) { entry ->
            val sessionId = entry.arguments?.getLong("sessionId") ?: return@composable
            SessionScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(TrainingRoutes.History) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenSession = { id -> navController.navigate(TrainingRoutes.session(id)) },
            )
        }
    }
}
