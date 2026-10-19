package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    mealId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: AddEntryViewModel = viewModel(
        key = "add-entry-$mealId",
        factory = fitiaViewModelFactory(container) { c ->
            AddEntryViewModel(mealId = mealId, repository = c.nutritionRepository)
        },
    )
    val foods by viewModel.foods.collectAsStateWithLifecycle()
    var grams by rememberSaveable { mutableStateOf("100") }
    var customName by rememberSaveable { mutableStateOf("") }
    var customCalories by rememberSaveable { mutableStateOf("") }
    var customProtein by rememberSaveable { mutableStateOf("") }
    var customCarbs by rememberSaveable { mutableStateOf("") }
    var customFat by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.saved.collect { onBack() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nutrition_add_food)) },
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
                    value = grams,
                    onValueChange = { grams = it },
                    label = { Text(stringResource(R.string.field_grams)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            items(foods, key = { it.id }) { food ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(food.name, style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = {
                            val g = grams.replace(',', '.').toDoubleOrNull() ?: return@Button
                            viewModel.addFood(food, g)
                        },
                    ) {
                        Text(stringResource(R.string.nutrition_add_to_meal))
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.nutrition_custom_entry),
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedTextField(customName, { customName = it }, label = { Text(stringResource(R.string.field_name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(customCalories, { customCalories = it }, label = { Text(stringResource(R.string.nutrition_calories)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(customProtein, { customProtein = it }, label = { Text(stringResource(R.string.nutrition_protein)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(customCarbs, { customCarbs = it }, label = { Text(stringResource(R.string.nutrition_carbs)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(customFat, { customFat = it }, label = { Text(stringResource(R.string.nutrition_fat)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        viewModel.addCustom(
                            name = customName,
                            grams = grams.replace(',', '.').toDoubleOrNull() ?: return@Button,
                            calories = customCalories.replace(',', '.').toDoubleOrNull() ?: return@Button,
                            protein = customProtein.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            carbs = customCarbs.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            fat = customFat.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        )
                    },
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}
