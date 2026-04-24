package com.quranengine.features.quranimage

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.quranengine.ui.quran.ImageDecorations
import com.quranengine.ui.quran.QuranImageRenderMode
import com.quranengine.ui.quran.WordHighlight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContentImageViewModel(
    private val page: Int,
    private val scope: CoroutineScope,
) {
    var state by mutableStateOf(ContentImageState(pageNumber = page.toString()))
        private set

    fun loadImagePage(
        loadBitmap: suspend (Int) -> Bitmap?,
        loadQuarterName: (Int) -> String,
        loadSuraNames: (Int) -> String,
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val bitmap = loadBitmap(page)
                val quarterName = loadQuarterName(page)
                val suraNames = loadSuraNames(page)

                state = state.copy(
                    bitmap = bitmap,
                    quarterName = quarterName,
                    suraNames = suraNames,
                    isLoading = false,
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e,
                )
            }
        }
    }

    fun updateHighlights(highlights: Map<Int, androidx.compose.ui.graphics.Color>) {
        val wordHighlights = highlights.map { (_, color) ->
            WordHighlight(
                rect = android.graphics.RectF(),
                color = color,
            )
        }
        state = state.copy(
            decorations = state.decorations.copy(wordHighlights = wordHighlights)
        )
    }

    fun updateRenderMode(mode: QuranImageRenderMode) {
        state = state.copy(renderMode = mode)
    }
}
