package com.arnaucaballer.fitia.feature.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.feature.chat.domain.FoodPhotoEstimate

@Composable
fun FoodEstimateDialog(
    estimate: FoodPhotoEstimate,
    onDismiss: () -> Unit,
    onConfirm: (FoodPhotoEstimate, mealName: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(estimate.name) }
    var grams by rememberSaveable { mutableStateOf(estimate.grams.toString()) }
    var calories by rememberSaveable { mutableStateOf(estimate.calories.toString()) }
    var protein by rememberSaveable { mutableStateOf(estimate.proteinGrams.toString()) }
    var carbs by rememberSaveable { mutableStateOf(estimate.carbsGrams.toString()) }
    var fat by rememberSaveable { mutableStateOf(estimate.fatGrams.toString()) }
    var mealName by rememberSaveable { mutableStateOf("Comida") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chat_confirm_food)) },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.field_name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(mealName, { mealName = it }, label = { Text(stringResource(R.string.nutrition_meal_name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(grams, { grams = it }, label = { Text(stringResource(R.string.field_grams)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(calories, { calories = it }, label = { Text(stringResource(R.string.nutrition_calories)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(protein, { protein = it }, label = { Text(stringResource(R.string.nutrition_protein)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(carbs, { carbs = it }, label = { Text(stringResource(R.string.nutrition_carbs)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(fat, { fat = it }, label = { Text(stringResource(R.string.nutrition_fat)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
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
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
