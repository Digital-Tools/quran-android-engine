package com.quranengine.features.qurancontent

import androidx.compose.ui.graphics.Color
import com.quranengine.model.quranannotations.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Maps Note.Color enum to Compose highlight colors, matching the iOS palette. */
fun Note.Color.toHighlightColor(): Color = when (this) {
    Note.Color.RED -> Color(0x4DFF6B8B)
    Note.Color.GREEN -> Color(0x4DC1EC71)
    Note.Color.BLUE -> Color(0x4DADD7FE)
    Note.Color.YELLOW -> Color(0x4DFDEC63)
    Note.Color.PURPLE -> Color(0x4DD8B1FE)
}

data class QuranHighlights(
    val searchVerses: List<Int> = emptyList(),
    val readingVerses: List<Int> = emptyList(),
    val shareVerses: List<Int> = emptyList(),
    val noteVerses: Map<Int, Note.Color> = emptyMap(),
    val pointedWord: Any? = null,
) {
    val highlightedVerses: Map<Int, Color>
        get() {
            val result = mutableMapOf<Int, Color>()
            for (v in searchVerses) result[v] = Color(0x4D9E9E9E) // gray highlight
            for (v in readingVerses) result[v] = Color(0x4DD4AF37) // gold highlight (was green)
            for (v in shareVerses) result[v] = Color(0x4D1A6B4A) // mizan green (was blue)
            for ((v, noteColor) in noteVerses) result.putIfAbsent(v, noteColor.toHighlightColor())
            return result
        }
}

class ContentStateManager {
    private val _visiblePages = MutableStateFlow<List<Int>>(emptyList())
    val visiblePages: StateFlow<List<Int>> = _visiblePages.asStateFlow()

    private val _highlights = MutableStateFlow(QuranHighlights())
    val highlights: StateFlow<QuranHighlights> = _highlights.asStateFlow()

    private val _contentMode = MutableStateFlow(QuranContentMode.ARABIC)
    val contentMode: StateFlow<QuranContentMode> = _contentMode.asStateFlow()

    private val _twoPagesEnabled = MutableStateFlow(false)
    val twoPagesEnabled: StateFlow<Boolean> = _twoPagesEnabled.asStateFlow()

    fun updateVisiblePages(pages: List<Int>) {
        _visiblePages.value = pages
        // Clear search highlights on page change
        _highlights.value = _highlights.value.copy(searchVerses = emptyList())
    }

    fun updateContentMode(mode: QuranContentMode) {
        _contentMode.value = mode
    }

    fun updateTwoPagesEnabled(enabled: Boolean) {
        _twoPagesEnabled.value = enabled
    }

    fun updateHighlights(transform: (QuranHighlights) -> QuranHighlights) {
        _highlights.value = transform(_highlights.value)
    }

    fun clearShareHighlights() {
        _highlights.value = _highlights.value.copy(shareVerses = emptyList())
    }
}
