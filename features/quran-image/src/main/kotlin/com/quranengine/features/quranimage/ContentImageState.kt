package com.quranengine.features.quranimage

import android.graphics.Bitmap
import android.graphics.RectF
import com.quranengine.ui.quran.ImageDecorations
import com.quranengine.ui.quran.QuranImageRenderMode
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurangeometry.WordFrame

data class ContentImageState(
    val bitmap: Bitmap? = null,
    val renderMode: QuranImageRenderMode = QuranImageRenderMode.TINTED,
    val decorations: ImageDecorations = ImageDecorations(),
    val quarterName: String = "",
    val suraNames: String = "",
    val pageNumber: String = "",
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val wordFramesByAyah: Map<AyahNumber, List<WordFrame>> = emptyMap(),
)
