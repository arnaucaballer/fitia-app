package com.arnaucaballer.fitia.feature.nutrition.ui

object NutritionRoutes {
    const val Home = "nutrition/home"
    const val Profile = "nutrition/profile"
    const val Targets = "nutrition/targets"
    const val Plans = "nutrition/plans"
    const val Foods = "nutrition/foods"
    const val AddEntry = "nutrition/add-entry/{mealId}"
    const val FoodPhoto = "nutrition/food-photo"
    const val PlanWizard = "nutrition/plan-wizard"

    fun addEntry(mealId: Long) = "nutrition/add-entry/$mealId"
}
