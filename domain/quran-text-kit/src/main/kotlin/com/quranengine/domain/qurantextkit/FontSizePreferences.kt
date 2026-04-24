package com.quranengine.domain.qurantextkit

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.PreferenceTransformer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.preferences.TransformedPreference
import com.quranengine.model.qurantext.FontSize
import kotlinx.coroutines.flow.Flow

class FontSizePreferences(preferences: Preferences) {

    private val translationDelegate = TransformedPreference(
        key = TRANSLATION_FONT_SIZE_KEY,
        preferences = preferences,
        transformer = FONT_SIZE_TRANSFORMER,
    )

    private val arabicDelegate = TransformedPreference(
        key = ARABIC_FONT_SIZE_KEY,
        preferences = preferences,
        transformer = FONT_SIZE_TRANSFORMER,
    )

    var translationFontSize: FontSize
        get() = translationDelegate.getValue(this, ::translationFontSize)
        set(value) = translationDelegate.setValue(this, ::translationFontSize, value)

    val translationFontSizeFlow: Flow<FontSize>
        get() = translationDelegate.flow

    var arabicFontSize: FontSize
        get() = arabicDelegate.getValue(this, ::arabicFontSize)
        set(value) = arabicDelegate.setValue(this, ::arabicFontSize, value)

    val arabicFontSizeFlow: Flow<FontSize>
        get() = arabicDelegate.flow

    companion object {
        private val DEFAULT_VALUE = FontSize.LARGE

        private val TRANSLATION_FONT_SIZE_KEY =
            PreferenceKey("translationFontSize", DEFAULT_VALUE.rawValue)

        private val ARABIC_FONT_SIZE_KEY =
            PreferenceKey("arabicFont", DEFAULT_VALUE.rawValue)

        private val FONT_SIZE_TRANSFORMER = PreferenceTransformer<Int, FontSize>(
            rawToValue = { FontSize.fromRawValue(it) ?: DEFAULT_VALUE },
            valueToRaw = { it.rawValue },
        )
    }
}
