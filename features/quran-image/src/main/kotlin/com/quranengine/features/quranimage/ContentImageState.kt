package com.quranengine.features.quranimage

import android.graphics.Bitmap
import android.graphics.RectF
import com.quranengine.ui.quran.ImageDecorations
import com.quranengine.ui.quran.QuranImageRenderMode

data class ContentImageState(
    val bitmap: Bitmap? = null,
    val renderMode: QuranImageRenderMode = QuranImageRenderMode.TINTED,
    val decorations: ImageDecorations = ImageDecorations(),
    val quarterName: String = "",
    val suraNames: String = "",
    val pageNumber: String = "",
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)
