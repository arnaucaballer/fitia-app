package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.nutrition.domain.FoodItem
import com.arnaucaballer.fitia.feature.nutrition.domain.MealEntry
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddEntryViewModel(
    private val mealId: Long,
    private val repository: NutritionRepository,
) : ViewModel() {

    val foods: StateFlow<List<FoodItem>> = repository.observeFoods().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _saved = MutableSharedFlow<Unit>()
    val saved: SharedFlow<Unit> = _saved.asSharedFlow()

    fun addFood(food: FoodItem, grams: Double) {
        viewModelScope.launch {
            val portion = food.scaledTo(grams)
            repository.addEntry(
                MealEntry(
                    mealId = mealId,
                    foodItemId = food.id.takeIf { it > 0 },
                    name = food.name,
                    grams = portion.grams,
                    calories = portion.calories,
                    proteinGrams = portion.proteinGrams,
                    carbsGrams = portion.carbsGrams,
                    fatGrams = portion.fatGrams,
                ),
            )
            _saved.emit(Unit)
        }
    }

    fun addCustom(
        name: String,
        grams: Double,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addEntry(
                MealEntry(
                    mealId = mealId,
                    name = name.trim(),
                    grams = grams,
                    calories = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatGrams = fat,
                ),
            )
            _saved.emit(Unit)
        }
    }
}
