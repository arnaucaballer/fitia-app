package com.arnaucaballer.fitia.feature.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.feature.chat.domain.FoodPhotoEstimate

@Composable
fun FoodEstimateDialog(
    estimate: FoodPhotoEstimate,
    recalculating: Boolean = false,
    onDismiss: () -> Unit,
    onRecalculate: (name: String, grams: Double) -> Unit,
    onConfirm: (FoodPhotoEstimate, mealName: String) -> Unit,
) {
    var name by remember(estimate.name) { mutableStateOf(estimate.name) }
    var grams by remember(estimate.grams) { mutableStateOf(estimate.grams.toString()) }
    var calories by remember(estimate.calories) { mutableStateOf(estimate.calories.toString()) }
    var protein by remember(estimate.proteinGrams) { mutableStateOf(estimate.proteinGrams.toString()) }
    var carbs by remember(estimate.carbsGrams) { mutableStateOf(estimate.carbsGrams.toString()) }
    var fat by remember(estimate.fatGrams) { mutableStateOf(estimate.fatGrams.toString()) }
    var mealName by remember { mutableStateOf("Comida") }

    LaunchedEffect(estimate) {
        name = estimate.name
        grams = formatNumber(estimate.grams)
        calories = formatNumber(estimate.calories)
        protein = formatNumber(estimate.proteinGrams)
        carbs = formatNumber(estimate.carbsGrams)
        fat = formatNumber(estimate.fatGrams)
    }

    AlertDialog(
        onDismissRequest = { if (!recalculating) onDismiss() },
        title = { Text(stringResource(R.string.chat_confirm_food)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.nutrition_food_edit_hint),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.field_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !recalculating,
                )
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text(stringResource(R.string.nutrition_meal_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !recalculating,
                )
                OutlinedTextField(
                    value = grams,
                    onValueChange = { grams = it },
                    label = { Text(stringResource(R.string.field_grams)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !recalculating,
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text(stringResource(R.string.nutrition_calories)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !recalculating,
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text(stringResource(R.string.nutrition_protein)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !recalculating,
                )
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text(stringResource(R.string.nutrition_carbs)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !recalculating,
                )
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text(stringResource(R.string.nutrition_fat)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !recalculating,
                )
                if (recalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp),
                    )
                } else {
                    TextButton(
                        onClick = {
                            val g = grams.replace(',', '.').toDoubleOrNull() ?: estimate.grams
                            onRecalculate(name.trim().ifBlank { estimate.name }, g)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.nutrition_recalculate_macros))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !recalculating,
                onClick = {
                    onConfirm(
                        FoodPhotoEstimate(
                            name = name.trim().ifBlank { estimate.name },
                            grams = grams.replace(',', '.').toDoubleOrNull() ?: estimate.grams,
                            calories = calories.replace(',', '.').toDoubleOrNull() ?: estimate.calories,
                            proteinGrams = protein.replace(',', '.').toDoubleOrNull() ?: estimate.proteinGrams,
                            carbsGrams = carbs.replace(',', '.').toDoubleOrNull() ?: estimate.carbsGrams,
                            fatGrams = fat.replace(',', '.').toDoubleOrNull() ?: estimate.fatGrams,
                        ),
                        mealName.trim().ifBlank { "Comida" },
                    )
                },
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !recalculating) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
