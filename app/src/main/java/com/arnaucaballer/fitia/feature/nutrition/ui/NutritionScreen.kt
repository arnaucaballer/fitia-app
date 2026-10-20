package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private const val SelectedDayKey = "selected_day"
private const val NoDay = Long.MIN_VALUE

@Composable
fun NutritionScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NutritionRoutes.Home,
        modifier = modifier,
    ) {
        composable(NutritionRoutes.Home) { entry ->
            val selectedDayFlow = entry.savedStateHandle.getStateFlow(SelectedDayKey, NoDay)
            NutritionHomeScreen(
                selectedDayFlow = selectedDayFlow,
                onSelectedDayConsumed = { entry.savedStateHandle[SelectedDayKey] = NoDay },
                onOpenProfile = { navController.navigate(NutritionRoutes.Profile) },
                onOpenTargets = { navController.navigate(NutritionRoutes.Targets) },
                onOpenPlans = { navController.navigate(NutritionRoutes.Plans) },
                onOpenFoods = { navController.navigate(NutritionRoutes.Foods) },
                onOpenCalendar = { navController.navigate(NutritionRoutes.Calendar) },
                onAddEntry = { mealId -> navController.navigate(NutritionRoutes.addEntry(mealId)) },
                onFoodPhoto = { navController.navigate(NutritionRoutes.FoodPhoto) },
                onCreatePlanWizard = { navController.navigate(NutritionRoutes.PlanWizard) },
            )
        }
        composable(NutritionRoutes.Calendar) {
            NutritionCalendarScreen(
                onBack = { navController.popBackStack() },
                onSelectDay = { day ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(SelectedDayKey, day)
                    navController.popBackStack()
                },
            )
        }
        composable(NutritionRoutes.FoodPhoto) {
            FoodPhotoScreen(onBack = { navController.popBackStack() })
        }
        composable(NutritionRoutes.PlanWizard) {
            NutritionPlanWizardScreen(
                onBack = { navController.popBackStack() },
                onCreated = {
                    navController.popBackStack()
                    navController.navigate(NutritionRoutes.Plans)
                },
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
