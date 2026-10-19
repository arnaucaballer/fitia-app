package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.arnaucaballer.fitia.feature.nutrition.domain.MacroTargets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: TargetsViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            TargetsViewModel(
                repository = c.nutritionRepository,
                saveManualTargetsUseCase = c.saveManualTargetsUseCase,
                recalculateTargetsUseCase = c.recalculateTargetsUseCase,
            )
        },
    )
    val targets by viewModel.targets.collectAsStateWithLifecycle()

    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var carbs by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }
    var adjustment by rememberSaveable { mutableStateOf("") }
    var proteinPerKg by rememberSaveable { mutableStateOf("") }
    var fatPerKg by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(targets) {
        targets?.let {
            calories = it.calories.toString()
            protein = it.proteinGrams.toString()
            carbs = it.carbsGrams.toString()
            fat = it.fatGrams.toString()
            adjustment = it.calorieAdjustment.toString()
            proteinPerKg = it.proteinPerKg.toString()
            fatPerKg = it.fatPerKg.toString()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nutrition_targets)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(24.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            targets?.let {
                Text(
                    text = stringResource(
                        R.string.nutrition_maintenance_label,
                        it.maintenanceCalories,
                    ),
                )
            }
            NumberField(calories, { calories = it }, stringResource(R.string.nutrition_calories))
            NumberField(protein, { protein = it }, stringResource(R.string.nutrition_protein))
            NumberField(carbs, { carbs = it }, stringResource(R.string.nutrition_carbs))
            NumberField(fat, { fat = it }, stringResource(R.string.nutrition_fat))
            NumberField(adjustment, { adjustment = it }, stringResource(R.string.nutrition_adjustment))
            NumberField(proteinPerKg, { proteinPerKg = it }, stringResource(R.string.nutrition_protein_per_kg))
            NumberField(fatPerKg, { fatPerKg = it }, stringResource(R.string.nutrition_fat_per_kg))

            Button(
                onClick = {
                    val current = targets ?: return@Button
                    viewModel.saveManual(
                        MacroTargets(
                            calories = calories.toIntOrNull() ?: return@Button,
                            proteinGrams = protein.replace(',', '.').toDoubleOrNull() ?: return@Button,
                            carbsGrams = carbs.replace(',', '.').toDoubleOrNull() ?: return@Button,
                            fatGrams = fat.replace(',', '.').toDoubleOrNull() ?: return@Button,
                            maintenanceCalories = current.maintenanceCalories,
                            calorieAdjustment = adjustment.toIntOrNull() ?: current.calorieAdjustment,
                            proteinPerKg = proteinPerKg.replace(',', '.').toDoubleOrNull()
                                ?: current.proteinPerKg,
                            fatPerKg = fatPerKg.replace(',', '.').toDoubleOrNull()
                                ?: current.fatPerKg,
                            manualOverride = true,
                        ),
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_save))
            }
            TextButton(onClick = viewModel::recalculate) {
                Text(stringResource(R.string.nutrition_recalculate))
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}
