package com.arnaucaballer.fitia.feature.nutrition.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.arnaucaballer.fitia.R
import com.arnaucaballer.fitia.design.FitiaPrimaryButton
import com.arnaucaballer.fitia.design.FitiaSecondaryButton
import com.arnaucaballer.fitia.di.fitiaContainer
import com.arnaucaballer.fitia.feature.chat.domain.FoodPhotoEstimate
import com.arnaucaballer.fitia.feature.chat.ui.FoodEstimateDialog
import com.arnaucaballer.fitia.feature.nutrition.domain.MealEntry
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var recalculating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<FoodPhotoEstimate?>(null) }
    var lastMime by remember { mutableStateOf("image/jpeg") }
    var lastBase64 by remember { mutableStateOf<String?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun analyzeUri(uri: Uri) {
        loading = true
        error = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val bitmap = decodeBitmap(context, uri)
                    val scaled = scaleBitmap(bitmap, 1280)
                    val stream = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    val settings = container.aiSettingsStore.settings.value
                    base64 to container.foodPhotoEstimator.estimate(settings, "image/jpeg", base64)
                }
            }.onSuccess { (base64, estimate) ->
                lastMime = "image/jpeg"
                lastBase64 = base64
                pending = estimate
                loading = false
            }.onFailure { t ->
                loading = false
                error = t.message ?: "Error"
            }
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) analyzeUri(uri)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = cameraUri
        if (ok && uri != null) analyzeUri(uri)
    }

    fun startCameraCapture() {
        val file = File(context.cacheDir, "food_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri
        takePictureLauncher.launch(uri)
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startCameraCapture()
        } else {
            error = context.getString(R.string.nutrition_camera_permission)
        }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            startCameraCapture()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                FitiaPrimaryButton(
                    text = stringResource(R.string.nutrition_take_photo),
                    onClick = { launchCamera() },
                )
                FitiaSecondaryButton(
                    text = stringResource(R.string.nutrition_pick_photo),
                    onClick = { galleryPicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    pending?.let { estimate ->
        FoodEstimateDialog(
            estimate = estimate,
            recalculating = recalculating,
            onDismiss = { pending = null },
            onRecalculate = { name, grams ->
                recalculating = true
                error = null
                scope.launch {
                    runCatching {
                        container.foodPhotoEstimator.recalculate(
                            settings = container.aiSettingsStore.settings.value,
                            name = name,
                            grams = grams,
                            mimeType = lastMime,
                            base64Data = lastBase64,
                        )
                    }.onSuccess {
                        pending = it
                        recalculating = false
                    }.onFailure { t ->
                        recalculating = false
                        error = t.message ?: "Error"
                    }
                }
            },
            onConfirm = { confirmed, mealName ->
                scope.launch {
                    runCatching {
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
                    }.onSuccess {
                        pending = null
                        onBack()
                    }.onFailure { t ->
                        error = t.message ?: "Error"
                    }
                }
            },
        )
    }
}

private fun decodeBitmap(context: android.content.Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
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
