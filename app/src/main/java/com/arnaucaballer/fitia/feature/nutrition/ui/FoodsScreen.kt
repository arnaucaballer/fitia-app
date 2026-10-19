package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.di.fitiaViewModelFactory
import com.arnaucaballer.fitia.feature.nutrition.domain.FoodItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: FoodsViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            FoodsViewModel(
                repository = c.nutritionRepository,
                openFoodFactsClient = c.openFoodFactsClient,
            )
        },
    )
    val foods by viewModel.foods.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val searching by viewModel.isSearching.collectAsStateWithLifecycle()

    var query by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var grams by rememberSaveable { mutableStateOf("100") }
    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var carbs by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nutrition_foods)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.nutrition_search_foods)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Button(
                    onClick = { viewModel.search(query) },
                    enabled = query.isNotBlank() && !searching,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        if (searching) {
                            stringResource(R.string.nutrition_searching)
                        } else {
                            stringResource(R.string.action_search)
                        },
                    )
                }
            }

            items(results) { food ->
                FoodResultRow(food = food, onSave = { viewModel.saveFood(food) })
            }

            item {
                Text(
                    text = stringResource(R.string.nutrition_custom_food),
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.field_name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(grams, { grams = it }, label = { Text(stringResource(R.string.field_grams)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(calories, { calories = it }, label = { Text(stringResource(R.string.nutrition_calories)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(protein, { protein = it }, label = { Text(stringResource(R.string.nutrition_protein)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(carbs, { carbs = it }, label = { Text(stringResource(R.string.nutrition_carbs)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(fat, { fat = it }, label = { Text(stringResource(R.string.nutrition_fat)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        viewModel.createCustom(
                            name = name,
                            grams = grams.replace(',', '.').toDoubleOrNull() ?: return@Button,
                            calories = calories.replace(',', '.').toDoubleOrNull() ?: return@Button,
                            protein = protein.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            carbs = carbs.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            fat = fat.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        )
                        name = ""
                        calories = ""
                        protein = ""
                        carbs = ""
                        fat = ""
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }

            item {
                Text(
                    text = stringResource(R.string.nutrition_saved_foods),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            items(foods, key = { it.id }) { food ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(food.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(
                                R.string.nutrition_food_serving,
                                food.servingGrams.toInt(),
                                food.calories.toInt(),
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { viewModel.delete(food.id) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.action_delete))
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodResultRow(
    food: FoodItem,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(food.name, style = MaterialTheme.typography.titleMedium)
            if (food.brand.isNotBlank()) {
                Text(food.brand, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        TextButton(onClick = onSave) {
            Text(stringResource(R.string.action_save))
        }
    }
}
