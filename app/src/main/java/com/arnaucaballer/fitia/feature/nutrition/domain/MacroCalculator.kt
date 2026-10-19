package com.arnaucaballer.fitia.feature.nutrition.domain

import kotlin.math.max
import kotlin.math.roundToInt

object MacroCalculator {
    const val DEFAULT_PROTEIN_PER_KG = 2.0
    const val DEFAULT_FAT_PER_KG = 0.8

    fun basalMetabolicRate(profile: BodyProfile): Double {
        val weight = profile.weightKg
        val height = profile.heightCm
        val age = profile.ageYears.toDouble()
        return when (profile.sex) {
            Sex.Male -> (10 * weight) + (6.25 * height) - (5 * age) + 5
            Sex.Female -> (10 * weight) + (6.25 * height) - (5 * age) - 161
        }
    }

    fun maintenanceCalories(profile: BodyProfile): Int {
        return (basalMetabolicRate(profile) * profile.activityLevel.multiplier)
            .roundToInt()
            .coerceAtLeast(1000)
    }

    fun suggestTargets(
        profile: BodyProfile,
        proteinPerKg: Double = DEFAULT_PROTEIN_PER_KG,
        fatPerKg: Double = DEFAULT_FAT_PER_KG,
        calorieAdjustment: Int = profile.goal.defaultCalorieAdjustment,
    ): MacroTargets {
        val maintenance = maintenanceCalories(profile)
        val calories = (maintenance + calorieAdjustment).coerceAtLeast(1200)
        val protein = proteinPerKg * profile.weightKg
        val fat = max(fatPerKg * profile.weightKg, calories * 0.2 / 9.0)
        val carbs = max(0.0, (calories - (protein * 4) - (fat * 9)) / 4.0)
        return MacroTargets(
            calories = calories,
            proteinGrams = protein.roundToOneDecimal(),
            carbsGrams = carbs.roundToOneDecimal(),
            fatGrams = fat.roundToOneDecimal(),
            maintenanceCalories = maintenance,
            calorieAdjustment = calorieAdjustment,
            proteinPerKg = proteinPerKg,
            fatPerKg = fatPerKg,
            manualOverride = false,
        )
    }

    private fun Double.roundToOneDecimal(): Double =
        (this * 10.0).roundToInt() / 10.0
}
