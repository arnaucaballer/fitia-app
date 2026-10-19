package com.arnaucaballer.fitia.feature.nutrition.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.design.FitiaSecondaryButton
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.feature.nutrition.domain.ActivityLevel
import com.arnaucaballer.fitia.feature.nutrition.domain.BodyPhotoInput
import com.arnaucaballer.fitia.feature.nutrition.domain.BodyProfile
import com.arnaucaballer.fitia.feature.nutrition.domain.NutritionGoal
import com.arnaucaballer.fitia.feature.nutrition.domain.Sex
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionPlanWizardScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = context.fitiaContainer()
    val scope = rememberCoroutineScope()

    var weight by rememberSaveable { mutableStateOf("80") }
    var height by rememberSaveable { mutableStateOf("180") }
    var age by rememberSaveable { mutableStateOf("28") }
    var sex by remember { mutableStateOf(Sex.Male) }
    var activity by remember { mutableStateOf(ActivityLevel.Moderate) }
    var goal by remember { mutableStateOf(NutritionGoal.LoseFat) }
    var notes by rememberSaveable { mutableStateOf("") }
    var front by remember { mutableStateOf<BodyPhotoInput?>(null) }
    var side by remember { mutableStateOf<BodyPhotoInput?>(null) }
    var back by remember { mutableStateOf<BodyPhotoInput?>(null) }
    var pendingSlot by remember { mutableStateOf("front") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            val scaled = scaleBitmap(bitmap, 1280)
            val stream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val photo = BodyPhotoInput(
                mimeType = "image/jpeg",
                base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP),
                label = pendingSlot,
            )
            when (pendingSlot) {
                "side" -> side = photo
                "back" -> back = photo
                else -> front = photo
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nutrition_create_plan_ai)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.nutrition_wizard_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            Text(stringResource(R.string.field_sex), style = MaterialTheme.typography.labelLarge)
            Sex.entries.forEach { option ->
                FilterChip(
                    selected = sex == option,
                    onClick = { sex = option },
                    label = { Text(option.name) },
                )
            }
            Text(stringResource(R.string.field_activity), style = MaterialTheme.typography.labelLarge)
            ActivityLevel.entries.forEach { option ->
                FilterChip(
                    selected = activity == option,
                    onClick = { activity = option },
                    label = { Text(option.name) },
                )
            }
            Text(stringResource(R.string.field_goal), style = MaterialTheme.typography.labelLarge)
            NutritionGoal.entries.forEach { option ->
                FilterChip(
                    selected = goal == option,
                    onClick = { goal = option },
                    label = { Text(option.name) },
                )
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.field_notes_optional)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Text(stringResource(R.string.nutrition_body_photos), style = MaterialTheme.typography.titleMedium)
            FitiaSecondaryButton(
                text = if (front != null) "✓ Frente" else stringResource(R.string.nutrition_photo_front),
                onClick = {
                    pendingSlot = "front"
                    picker.launch("image/*")
                },
            )
            FitiaSecondaryButton(
                text = if (side != null) "✓ Lateral" else stringResource(R.string.nutrition_photo_side),
                onClick = {
                    pendingSlot = "side"
                    picker.launch("image/*")
                },
            )
            FitiaSecondaryButton(
                text = if (back != null) "✓ Espalda" else stringResource(R.string.nutrition_photo_back),
                onClick = {
                    pendingSlot = "back"
                    picker.launch("image/*")
                },
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                FitiaPrimaryButton(
                    text = stringResource(R.string.generate),
                    onClick = {
                        val profile = BodyProfile(
                            weightKg = weight.toDoubleOrNull() ?: return@FitiaPrimaryButton,
                            heightCm = height.toDoubleOrNull() ?: return@FitiaPrimaryButton,
                            ageYears = age.toIntOrNull() ?: return@FitiaPrimaryButton,
                            sex = sex,
                            activityLevel = activity,
                            goal = goal,
                        )
                        loading = true
                        error = null
                        scope.launch {
                            runCatching {
                                container.nutritionPlanGenerator.generateFromFormAndPhotos(
                                    profile = profile,
                                    photos = listOfNotNull(front, side, back),
                                    extraNotes = notes,
                                )
                            }.onSuccess {
                                loading = false
                                onCreated()
                            }.onFailure { t ->
                                loading = false
                                error = t.message ?: "Error"
                            }
                        }
                    },
                )
            }
        }
    }
}

private fun scaleBitmap(source: Bitmap, maxSide: Int): Bitmap {
    val largest = maxOf(source.width, source.height)
    if (largest <= maxSide) return source
    val scale = maxSide.toFloat() / largest.toFloat()
    return Bitmap.createScaledBitmap(
        source,
        (source.width * scale).toInt().coerceAtLeast(1),
        (source.height * scale).toInt().coerceAtLeast(1),
        true,
    )
}
