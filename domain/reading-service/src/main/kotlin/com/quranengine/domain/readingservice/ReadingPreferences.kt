package com.quranengine.domain.readingservice

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.Preferences
import com.quranengine.model.qurankit.Reading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

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

    companion object {
        private val DEFAULT_READING = Reading.HAFS_1405
        private val READING_KEY = PreferenceKey("quranReading", DEFAULT_READING.value)
    }
}
