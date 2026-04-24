package com.quranengine.domain.annotationservice

import com.quranengine.model.quranannotations.QuranHighlights
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import timber.log.Timber

class QuranHighlightsService {

    private val _highlights = MutableStateFlow(QuranHighlights())

    val highlights: StateFlow<QuranHighlights> = _highlights.asStateFlow()

    var highlightsValue: QuranHighlights
        get() = _highlights.value
        set(value) {
            _highlights.value = value
            Timber.i("Highlights updated")
        }

    val scrolling: Flow<Unit> =
        _highlights
            .zip(_highlights.drop(1)) { oldValue, newValue -> oldValue to newValue }
            .filter { (oldValue, newValue) -> newValue.needsScrolling(oldValue) }
            .map { }

    fun reset() {
        highlightsValue = QuranHighlights()
    }
}
