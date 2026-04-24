package com.quranengine.features.qurancontent

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuranHighlights(
    val searchVerses: List<Int> = emptyList(),
    val readingVerses: List<Int> = emptyList(),
    val shareVerses: List<Int> = emptyList(),
    val noteVerses: Map<Int, Any> = emptyMap(),
    val pointedWord: Any? = null,
) {
    val highlightedVerses: Map<Int, Color>
        get() {
            val result = mutableMapOf<Int, Color>()
            for (v in searchVerses) result[v] = Color(0x4D9E9E9E) // gray highlight
            for (v in readingVerses) result[v] = Color(0x4D1B6B71) // identity highlight
            for (v in shareVerses) result[v] = Color(0x4D2196F3) // blue highlight
            for ((v, _) in noteVerses) result.putIfAbsent(v, Color(0x4DFFC107)) // amber for notes
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
