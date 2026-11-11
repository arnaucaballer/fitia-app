package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import com.arnaucaballer.fitia.R

@Composable
fun MuscleTint(
    front: Boolean,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    modifier: Modifier = Modifier,
    cropLeft: Float = 0f,
    cropTop: Float = 0f,
    cropRight: Float = 1f,
    cropBottom: Float = 1f,
) {
    val mask = rememberBodyBitmap(
        if (front) R.drawable.body_front_mask else R.drawable.body_back_mask,
    )
    val wash = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
    Canvas(
        modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        val bitmap = mask ?: return@Canvas
        val spanX = (cropRight - cropLeft).coerceAtLeast(0.05f)
        val spanY = (cropBottom - cropTop).coerceAtLeast(0.05f)
        drawBodyRegion(
            image = bitmap,
            left = cropLeft + left * spanX,
            top = cropTop + top * spanY,
            right = cropLeft + right * spanX,
            bottom = cropTop + bottom * spanY,
            colorFilter = ColorFilter.tint(wash, BlendMode.SrcIn),
        )
    }
}
