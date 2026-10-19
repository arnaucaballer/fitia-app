package com.arnaucaballer.fitia.feature.nutrition.domain

data class FoodItem(
    val id: Long = 0,
    val name: String,
    val brand: String = "",
    val servingGrams: Double,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val source: String = "custom",
    val barcode: String = "",
) {
    fun scaledTo(grams: Double): MacroPortion {
        val factor = if (servingGrams <= 0.0) 0.0 else grams / servingGrams
        return MacroPortion(
            grams = grams,
            calories = calories * factor,
            proteinGrams = proteinGrams * factor,
            carbsGrams = carbsGrams * factor,
            fatGrams = fatGrams * factor,
        )
    }
}
