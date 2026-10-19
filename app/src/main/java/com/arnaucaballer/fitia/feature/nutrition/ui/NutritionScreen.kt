package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun NutritionScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NutritionRoutes.Home,
        modifier = modifier,
    ) {
        composable(NutritionRoutes.Home) {
            NutritionHomeScreen(
                onOpenProfile = { navController.navigate(NutritionRoutes.Profile) },
                onOpenTargets = { navController.navigate(NutritionRoutes.Targets) },
                onOpenPlans = { navController.navigate(NutritionRoutes.Plans) },
                onOpenFoods = { navController.navigate(NutritionRoutes.Foods) },
                onAddEntry = { mealId -> navController.navigate(NutritionRoutes.addEntry(mealId)) },
            )
        }
        composable(NutritionRoutes.Profile) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(NutritionRoutes.Targets) {
            TargetsScreen(onBack = { navController.popBackStack() })
        }
        composable(NutritionRoutes.Plans) {
            NutritionPlansScreen(onBack = { navController.popBackStack() })
        }
        composable(NutritionRoutes.Foods) {
            FoodsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = NutritionRoutes.AddEntry,
            arguments = listOf(navArgument("mealId") { type = NavType.LongType }),
        ) { entry ->
            val mealId = entry.arguments?.getLong("mealId") ?: return@composable
            AddEntryScreen(
                mealId = mealId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
