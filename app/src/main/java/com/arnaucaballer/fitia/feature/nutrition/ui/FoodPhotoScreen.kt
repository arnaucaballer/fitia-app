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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.feature.chat.domain.FoodPhotoEstimate
import com.arnaucaballer.fitia.feature.chat.ui.FoodEstimateDialog
import com.arnaucaballer.fitia.feature.nutrition.domain.MealEntry
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodPhotoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = context.fitiaContainer()
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<FoodPhotoEstimate?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        loading = true
        error = null
        scope.launch {
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
                val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                val settings = container.aiSettingsStore.settings.value
                container.foodPhotoEstimator.estimate(settings, "image/jpeg", base64)
            }.onSuccess {
                loading = false
                pending = it
            }.onFailure { t ->
                loading = false
                error = t.message ?: "Error"
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nutrition_food_photo)) },
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.nutrition_food_photo_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (loading) {
                CircularProgressIndicator()
            } else {
                FitiaPrimaryButton(
                    text = stringResource(R.string.nutrition_pick_photo),
                    onClick = { picker.launch("image/*") },
                )
            }
        }
    }

    pending?.let { estimate ->
        FoodEstimateDialog(
            estimate = estimate,
            onDismiss = { pending = null },
            onConfirm = { confirmed, mealName ->
                scope.launch {
                    val day = LocalDate.now().toEpochDay()
                    container.nutritionRepository.ensureDefaultMeals(day)
                    val meals = container.nutritionRepository.observeDayLog(day).first().meals
                    val meal = meals.firstOrNull { it.name.equals(mealName, ignoreCase = true) }
                        ?: meals.firstOrNull()
                        ?: return@launch
                    container.nutritionRepository.addEntry(
                        MealEntry(
                            mealId = meal.id,
                            foodItemId = null,
                            name = confirmed.name,
                            grams = confirmed.grams,
                            calories = confirmed.calories,
                            proteinGrams = confirmed.proteinGrams,
                            carbsGrams = confirmed.carbsGrams,
                            fatGrams = confirmed.fatGrams,
                        ),
                    )
                    pending = null
                    onBack()
                }
            },
        )
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
