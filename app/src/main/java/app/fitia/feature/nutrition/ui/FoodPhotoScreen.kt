package app.fitia.feature.nutrition.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
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
import app.fitia.R
import app.fitia.core.images.ImageScaling
import app.fitia.design.FitiaPrimaryButton
import app.fitia.design.FitiaSpace
import app.fitia.design.FitiaSecondaryButton
import app.fitia.di.fitiaContainer
import app.fitia.feature.chat.domain.FoodPhotoEstimate
import app.fitia.feature.chat.ui.FoodEstimateDialog
import app.fitia.feature.nutrition.domain.MealEntry
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.fitia.design.fitiaPress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodPhotoScreen(
    dayEpochDay: Long,
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
    var mealNames by remember { mutableStateOf(listOf("Desayuno", "Comida", "Cena", "Snacks")) }
    var lastMime by remember { mutableStateOf("image/jpeg") }
    var lastBase64 by remember { mutableStateOf<String?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(dayEpochDay) {
        container.nutritionRepository.ensureDefaultMeals(dayEpochDay)
        val names = container.nutritionRepository.observeDayLog(dayEpochDay).first().meals.map { it.name }
        if (names.isNotEmpty()) mealNames = names
    }

    fun analyzeUri(uri: Uri) {
        loading = true
        error = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val bitmap = ImageScaling.decode(context, uri, 1024)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                    if (!bitmap.isRecycled) bitmap.recycle()
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
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(cameraDir, "food_${System.currentTimeMillis()}.jpg")
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
                    IconButton(modifier = Modifier.fitiaPress(), onClick = onBack) {
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
                .padding(FitiaSpace.page())
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(FitiaSpace.stack),
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
            meals = mealNames,
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
                        container.nutritionRepository.ensureDefaultMeals(dayEpochDay)
                        val meals = container.nutritionRepository.observeDayLog(dayEpochDay).first().meals
                        val mealId = meals.firstOrNull { it.name.equals(mealName, ignoreCase = true) }?.id
                            ?: container.nutritionRepository.addMeal(dayEpochDay, mealName)
                        container.nutritionRepository.addEntry(
                            MealEntry(
                                mealId = mealId,
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
