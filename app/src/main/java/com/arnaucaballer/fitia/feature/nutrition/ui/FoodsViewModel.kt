package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnaucaballer.fitia.feature.nutrition.data.OpenFoodFactsClient
import com.arnaucaballer.fitia.feature.nutrition.domain.FoodItem
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodsViewModel(
    private val repository: NutritionRepository,
    private val openFoodFactsClient: OpenFoodFactsClient,
) : ViewModel() {

    val foods: StateFlow<List<FoodItem>> = repository.observeFoods().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _searchResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val searchResults: StateFlow<List<FoodItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun search(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = runCatching { openFoodFactsClient.search(query) }
                .getOrDefault(emptyList())
            _isSearching.value = false
        }
    }

    fun saveFood(food: FoodItem) {
        viewModelScope.launch {
            repository.saveFood(food)
        }
    }

    fun createCustom(
        name: String,
        grams: Double,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.saveFood(
                FoodItem(
                    name = name.trim(),
                    servingGrams = grams,
                    calories = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatGrams = fat,
                ),
            )
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.deleteFood(id)
        }
    }
}
