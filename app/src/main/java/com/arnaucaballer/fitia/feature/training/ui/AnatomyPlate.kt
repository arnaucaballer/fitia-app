package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import com.arnaucaballer.fitia.R

@Composable
fun AnatomyPlate(
    front: Boolean,
    modifier: Modifier = Modifier,
    cropLeft: Float = 0f,
    cropTop: Float = 0f,
    cropRight: Float = 1f,
    cropBottom: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
) {
    val image = rememberBodyBitmap(
        if (front) R.drawable.body_front else R.drawable.body_back,
    )
    val cropWidth = (cropRight - cropLeft).coerceAtLeast(0.05f)
    val cropHeight = (cropBottom - cropTop).coerceAtLeast(0.05f)
    val bitmapAspect = if (image != null) {
        image.width.toFloat() / image.height.toFloat()
    } else {
        bodyAspect(front)
    }
    val aspect = bitmapAspect * cropWidth / cropHeight

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val boxAspect = maxWidth.value / maxHeight.value.coerceAtLeast(1f)
            val drawWidth = if (boxAspect > aspect) maxHeight * aspect else maxWidth
            val drawHeight = if (boxAspect > aspect) maxHeight else maxWidth / aspect
            Box(
                modifier = Modifier
                    .size(drawWidth, drawHeight)
                    .align(Alignment.Center),
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
                ) {
                    val bitmap = image ?: return@Canvas
                    drawBodyRegion(
                        image = bitmap,
                        left = cropLeft,
                        top = cropTop,
                        right = cropRight,
                        bottom = cropBottom,
                    )
                }
                content()
            }
        }
    }
}
