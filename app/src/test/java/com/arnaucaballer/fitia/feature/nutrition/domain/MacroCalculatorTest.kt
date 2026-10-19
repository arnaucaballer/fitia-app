package com.arnaucaballer.fitia.feature.nutrition.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MacroCalculatorTest {
    private val profile = BodyProfile(
        weightKg = 80.0,
        heightCm = 180.0,
        ageYears = 28,
        sex = Sex.Male,
        activityLevel = ActivityLevel.Moderate,
        goal = NutritionGoal.LoseFat,
    )

    @Test
    fun maintenanceUsesMifflinAndActivity() {
        val bmr = MacroCalculator.basalMetabolicRate(profile)
        assertEquals(1790.0, bmr, 0.1)
        val maintenance = MacroCalculator.maintenanceCalories(profile)
        assertEquals(kotlin.math.round(1790.0 * 1.55).toInt(), maintenance)
    }

    @Test
    fun suggestTargetsKeepsHighProteinInDeficit() {
        val targets = MacroCalculator.suggestTargets(profile)
        assertEquals(160.0, targets.proteinGrams, 0.1)
        assertTrue(targets.calories < targets.maintenanceCalories)
        assertTrue(targets.carbsGrams >= 0.0)
        assertTrue(targets.fatGrams > 0.0)
    }
}
