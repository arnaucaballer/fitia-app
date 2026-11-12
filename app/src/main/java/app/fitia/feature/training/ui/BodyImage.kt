package app.fitia.feature.training.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal fun bodyAspect(front: Boolean): Float = if (front) 1338f / 1908f else 1086f / 1599f

private val decodedBodies = ConcurrentHashMap<Int, ImageBitmap>()
private val decodeMutex = Mutex()

@Composable
internal fun rememberBodyBitmap(resId: Int): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(resId) { mutableStateOf(decodedBodies[resId]) }
    LaunchedEffect(resId) {
        decodedBodies[resId]?.let {
            bitmap = it
            return@LaunchedEffect
        }
        bitmap = withContext(Dispatchers.IO) {
            decodeMutex.withLock {
                decodedBodies[resId] ?: decodeBody(context.applicationContext, resId).also { decoded ->
                    decodedBodies[resId] = decoded
                }
            }
        }
    }
    return bitmap
}

private fun decodeBody(context: Context, resId: Int): ImageBitmap {
    val options = BitmapFactory.Options().apply {
        inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        inScaled = false
    }
    val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)
        ?: error("Missing drawable $resId")
    return bitmap.asImageBitmap()
}

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
        filterQuality = FilterQuality.Medium,
        colorFilter = colorFilter,
    )
}
