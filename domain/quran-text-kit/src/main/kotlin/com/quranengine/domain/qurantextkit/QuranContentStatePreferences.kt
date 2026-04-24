package com.quranengine.domain.qurantextkit

import com.quranengine.core.preferences.Preference
import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.PreferenceTransformer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.preferences.TransformedPreference
import com.quranengine.model.qurantext.QuranMode
import kotlinx.coroutines.flow.Flow

class QuranContentStatePreferences(preferences: Preferences) {

    private val quranModeDelegate = TransformedPreference(
        key = SHOW_QURAN_TRANSLATION_VIEW,
        preferences = preferences,
        transformer = QURAN_MODE_TRANSFORMER,
    )

    private val twoPagesDelegate = Preference(
        key = TWO_PAGES_ENABLED,
        preferences = preferences,
    )

    private val verticalScrollingDelegate = Preference(
        key = VERTICAL_SCROLLING_ENABLED,
        preferences = preferences,
    )

    var quranMode: QuranMode
        get() = quranModeDelegate.getValue(this, ::quranMode)
        set(value) = quranModeDelegate.setValue(this, ::quranMode, value)

    val quranModeFlow: Flow<QuranMode>
        get() = quranModeDelegate.flow

    var twoPagesEnabled: Boolean
        get() = twoPagesDelegate.getValue(this, ::twoPagesEnabled)
        set(value) = twoPagesDelegate.setValue(this, ::twoPagesEnabled, value)

    val twoPagesEnabledFlow: Flow<Boolean>
        get() = twoPagesDelegate.flow

    var verticalScrollingEnabled: Boolean
        get() = verticalScrollingDelegate.getValue(this, ::verticalScrollingEnabled)
        set(value) = verticalScrollingDelegate.setValue(this, ::verticalScrollingEnabled, value)

    val verticalScrollingEnabledFlow: Flow<Boolean>
        get() = verticalScrollingDelegate.flow

    companion object {
        private val SHOW_QURAN_TRANSLATION_VIEW =
            PreferenceKey("showQuranTranslationView", false)

        private val TWO_PAGES_ENABLED =
            PreferenceKey("twoPagesEnabled", TwoPagesUtils.settingDefaultValue)

        private val VERTICAL_SCROLLING_ENABLED =
            PreferenceKey("verticalScrollingEnabled", false)

        private val QURAN_MODE_TRANSFORMER = PreferenceTransformer<Boolean, QuranMode>(
            rawToValue = { if (it) QuranMode.TRANSLATION else QuranMode.ARABIC },
            valueToRaw = { it == QuranMode.TRANSLATION },
        )
    }
}
