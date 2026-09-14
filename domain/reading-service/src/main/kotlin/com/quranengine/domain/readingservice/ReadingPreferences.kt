package com.quranengine.domain.readingservice

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.Preferences
import com.quranengine.model.qurankit.Reading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** How the currently-playing ayah is highlighted during audio playback. */
enum class ReadingHighlightStyle {
    /** A single box that tracks the reciter word by word. */
    WORD,

    /** The whole ayah's line(s), highlighted as one continuous bar for its duration. */
    LINE,
}

class ReadingPreferences(private val preferences: Preferences) {

    var reading: Reading
        get() {
            val raw = preferences.valueForKey(READING_KEY)
            return Reading.fromValue(raw) ?: DEFAULT_READING
        }
        set(value) {
            preferences.setValue(value.value, READING_KEY)
        }

    /** Emits the current [Reading] immediately and then on every subsequent change. */
    val readingFlow: Flow<Reading>
        get() = preferences.notifications
            .filter { it == READING_KEY.key }
            .map { reading }
            .onStart { emit(reading) }

    var highlightStyle: ReadingHighlightStyle
        get() {
            val raw = preferences.valueForKey(HIGHLIGHT_STYLE_KEY)
            return ReadingHighlightStyle.entries.firstOrNull { it.ordinal == raw }
                ?: DEFAULT_HIGHLIGHT_STYLE
        }
        set(value) {
            preferences.setValue(value.ordinal, HIGHLIGHT_STYLE_KEY)
        }

    /** Emits the current [ReadingHighlightStyle] immediately and then on every subsequent change. */
    val highlightStyleFlow: Flow<ReadingHighlightStyle>
        get() = preferences.notifications
            .filter { it == HIGHLIGHT_STYLE_KEY.key }
            .map { highlightStyle }
            .onStart { emit(highlightStyle) }

    companion object {
        private val DEFAULT_READING = Reading.HAFS_1405
        private val READING_KEY = PreferenceKey("quranReading", DEFAULT_READING.value)

        private val DEFAULT_HIGHLIGHT_STYLE = ReadingHighlightStyle.WORD
        private val HIGHLIGHT_STYLE_KEY =
            PreferenceKey("readingHighlightStyle", DEFAULT_HIGHLIGHT_STYLE.ordinal)
    }
}
