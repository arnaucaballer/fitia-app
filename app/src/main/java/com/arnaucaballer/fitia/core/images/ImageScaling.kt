package com.arnaucaballer.fitia.core.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlin.math.max

object ImageScaling {
    fun decode(context: Context, uri: Uri, maxSide: Int): Bitmap {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxSide)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: error("No se pudo leer la imagen")
        return fit(decoded, maxSide)
    }

    private fun sampleSize(width: Int, height: Int, maxSide: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sample = 1
        var largest = max(width, height)
        while (largest / 2 >= maxSide) {
            sample *= 2
            largest /= 2
        }
        return sample
    }

    private fun fit(source: Bitmap, maxSide: Int): Bitmap {
        val largest = max(source.width, source.height)
        if (largest <= maxSide) return source
        val scale = maxSide.toFloat() / largest.toFloat()
        val scaled = Bitmap.createScaledBitmap(
            source,
            (source.width * scale).toInt().coerceAtLeast(1),
            (source.height * scale).toInt().coerceAtLeast(1),
            true,
        )
        if (scaled !== source) source.recycle()
        return scaled
    }
}
