package com.quranengine.domain.settingsservice

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.preferences.Preference
import com.quranengine.core.preferences.TransformedPreference
import com.quranengine.core.preferences.PreferenceTransformer
import com.quranengine.core.preferences.optionalTransformer

class ReviewPersistence(preferences: Preferences) {

    private val appOpenedCounterDelegate = Preference(APP_OPENED_COUNTER_KEY, preferences)
    var appOpenedCounter: Int by appOpenedCounterDelegate

    private val appInstalledDateDelegate = TransformedPreference(
        key = APP_INSTALLED_DATE_KEY,
        preferences = preferences,
        transformer = DATE_TRANSFORMER,
    )
    var appInstalledDate: Long by appInstalledDateDelegate

    private val requestReviewDateDelegate = TransformedPreference(
        key = REQUEST_REVIEW_DATE_KEY,
        preferences = preferences,
        transformer = optionalTransformer(DATE_TRANSFORMER),
    )
    var requestReviewDate: Long? by requestReviewDateDelegate

    companion object {
        private val APP_OPENED_COUNTER_KEY = PreferenceKey("appOpenedCounter", 0)
        private val APP_INSTALLED_DATE_KEY = PreferenceKey("appInstalledDate", 0L)
        private val REQUEST_REVIEW_DATE_KEY = PreferenceKey<Long?>("requestReviewDate", null)

        // Store dates as epoch millis
        private val DATE_TRANSFORMER = PreferenceTransformer<Long, Long>(
            rawToValue = { it },
            valueToRaw = { it },
        )
    }
}
