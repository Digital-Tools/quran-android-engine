package com.quranengine.ui.quran

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale

enum class QuranImageRenderMode {
    TINTED,
    INVERT_IN_DARK_MODE,
}

@Composable
fun QuranThemedImage(
    bitmap: Bitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    renderMode: QuranImageRenderMode = QuranImageRenderMode.TINTED,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val isDark = isSystemInDarkTheme()
    val processedBitmap = remember(bitmap, isDark, renderMode) {
        when (renderMode) {
            QuranImageRenderMode.TINTED -> bitmap
            QuranImageRenderMode.INVERT_IN_DARK_MODE -> {
                if (isDark) invertBitmap(bitmap) else bitmap
            }
        }
    }

    Image(
        painter = BitmapPainter(processedBitmap.asImageBitmap()),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}

private fun invertBitmap(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)
    val paint = android.graphics.Paint()
    val invertMatrix = ColorMatrix(
        floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f,
        )
    )
    paint.colorFilter = ColorMatrixColorFilter(invertMatrix)
    canvas.drawBitmap(source, 0f, 0f, paint)
    return result
}
