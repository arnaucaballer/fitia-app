package com.arnaucaballer.fitia.feature.training.ui

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

internal fun DrawScope.drawBodyRegion(
    image: ImageBitmap,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    colorFilter: ColorFilter? = null,
) {
    val spanX = (right - left).coerceAtLeast(0.001f)
    val spanY = (bottom - top).coerceAtLeast(0.001f)
    val srcX = (image.width * left).roundToInt().coerceIn(0, image.width - 1)
    val srcY = (image.height * top).roundToInt().coerceIn(0, image.height - 1)
    val srcW = (image.width * spanX).roundToInt().coerceIn(1, image.width - srcX)
    val srcH = (image.height * spanY).roundToInt().coerceIn(1, image.height - srcY)
    drawImage(
        image = image,
        srcOffset = IntOffset(srcX, srcY),
        srcSize = IntSize(srcW, srcH),
        dstSize = IntSize(
            size.width.roundToInt().coerceAtLeast(1),
            size.height.roundToInt().coerceAtLeast(1),
        ),
        filterQuality = FilterQuality.High,
        colorFilter = colorFilter,
    )
}
