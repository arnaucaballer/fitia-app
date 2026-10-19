package com.arnaucaballer.fitia.feature.nutrition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.arnaucaballer.fitia.feature.nutrition.domain.ActivityLevel
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionGoal
import com.arnaucaballer.fitia.feature.nutrition.domain.Sex

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.fitiaContainer()
    val viewModel: ProfileViewModel = viewModel(
        factory = fitiaViewModelFactory(container) { c ->
            ProfileViewModel(
                repository = c.nutritionRepository,
                recalculateTargetsUseCase = c.recalculateTargetsUseCase,
            )
        },
    )
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    var weight by rememberSaveable { mutableStateOf("80") }
    var height by rememberSaveable { mutableStateOf("180") }
    var age by rememberSaveable { mutableStateOf("28") }
    var sex by rememberSaveable { mutableStateOf(Sex.Male.name) }
    var activity by rememberSaveable { mutableStateOf(ActivityLevel.Moderate.name) }
    var goal by rememberSaveable { mutableStateOf(NutritionGoal.LoseFat.name) }

    LaunchedEffect(profile) {
        profile?.let {
            weight = it.weightKg.toString()
            height = it.heightCm.toString()
            age = it.ageYears.toString()
            sex = it.sex.name
            activity = it.activityLevel.name
            goal = it.goal.name
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nutrition_profile)) },
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
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(stringResource(R.string.field_weight_kg)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text(stringResource(R.string.field_height_cm)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text(stringResource(R.string.field_age)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(stringResource(R.string.field_sex))
            RowChips(
                options = Sex.entries.map { it.name to sexLabel(it) },
                selected = sex,
                onSelect = { sex = it },
            )

            Text(stringResource(R.string.field_activity))
            RowChips(
                options = ActivityLevel.entries.map { it.name to activityLabel(it) },
                selected = activity,
                onSelect = { activity = it },
            )

            Text(stringResource(R.string.field_goal))
            RowChips(
                options = NutritionGoal.entries.map { it.name to goalLabel(it) },
                selected = goal,
                onSelect = { goal = it },
            )

            Button(
                onClick = {
                    val weightKg = weight.replace(',', '.').toDoubleOrNull() ?: return@Button
                    val heightCm = height.replace(',', '.').toDoubleOrNull() ?: return@Button
                    val ageYears = age.toIntOrNull() ?: return@Button
                    if (weightKg !in 30.0..300.0 || heightCm !in 100.0..250.0 || ageYears !in 10..100) {
                        return@Button
                    }
                    val sexValue = runCatching { Sex.valueOf(sex) }.getOrDefault(Sex.Male)
                    val activityValue = runCatching { ActivityLevel.valueOf(activity) }
                        .getOrDefault(ActivityLevel.Moderate)
                    val goalValue = runCatching { NutritionGoal.valueOf(goal) }
                        .getOrDefault(NutritionGoal.Maintain)
                    viewModel.save(
                        weightKg = weightKg,
                        heightCm = heightCm,
                        ageYears = ageYears,
                        sex = sexValue,
                        activityLevel = activityValue,
                        goal = goalValue,
                        recalculate = true,
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.nutrition_save_and_recalculate))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RowChips(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun sexLabel(sex: Sex): String = when (sex) {
    Sex.Male -> stringResource(R.string.sex_male)
    Sex.Female -> stringResource(R.string.sex_female)
}

@Composable
private fun activityLabel(level: ActivityLevel): String = when (level) {
    ActivityLevel.Sedentary -> stringResource(R.string.activity_sedentary)
    ActivityLevel.Light -> stringResource(R.string.activity_light)
    ActivityLevel.Moderate -> stringResource(R.string.activity_moderate)
    ActivityLevel.Active -> stringResource(R.string.activity_active)
    ActivityLevel.VeryActive -> stringResource(R.string.activity_very_active)
}

@Composable
private fun goalLabel(goal: NutritionGoal): String = when (goal) {
    NutritionGoal.LoseFat -> stringResource(R.string.goal_lose)
    NutritionGoal.Maintain -> stringResource(R.string.goal_maintain)
    NutritionGoal.GainMuscle -> stringResource(R.string.goal_gain)
}
