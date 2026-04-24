package com.quranengine.domain.reciterservice

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.Preferences

class ReciterPreferences(private val preferences: Preferences) {

    var lastSelectedReciterId: Int
        get() = preferences.valueForKey(LAST_SELECTED_RECITER_ID)
        set(value) = preferences.setValue(value, LAST_SELECTED_RECITER_ID)

    var recentReciterIds: LinkedHashSet<Int>
        get() {
            val raw = preferences.valueForKey(RECENT_RECITER_IDS)
            return LinkedHashSet(raw.split(",").mapNotNull { it.trim().toIntOrNull() })
        }
        set(value) {
            preferences.setValue(value.joinToString(","), RECENT_RECITER_IDS)
        }

    fun reset() {
        preferences.removeValueForKey(LAST_SELECTED_RECITER_ID)
        preferences.removeValueForKey(RECENT_RECITER_IDS)
    }

    companion object {
        private val LAST_SELECTED_RECITER_ID =
            PreferenceKey(key = "LastSelectedQariId", defaultValue = 41)
        private val RECENT_RECITER_IDS =
            PreferenceKey(key = "recentRecitersIdsKey", defaultValue = "")
    }
}
