package com.quranengine.ui.theme

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.PreferenceTransformer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.preferences.TransformedPreference
import kotlinx.coroutines.flow.Flow

class ThemePreferences(preferences: Preferences) {

    private val themeStyleDelegate = TransformedPreference(
        key = THEME_STYLE_KEY,
        preferences = preferences,
        transformer = THEME_STYLE_TRANSFORMER,
    )

    private val appearanceModeDelegate = TransformedPreference(
        key = APPEARANCE_MODE_KEY,
        preferences = preferences,
        transformer = APPEARANCE_MODE_TRANSFORMER,
    )

    var themeStyle: ThemeStyle
        get() = themeStyleDelegate.getValue(this, ::themeStyle)
        set(value) = themeStyleDelegate.setValue(this, ::themeStyle, value)

    val themeStyleFlow: Flow<ThemeStyle>
        get() = themeStyleDelegate.flow

    var appearanceMode: AppearanceMode
        get() = appearanceModeDelegate.getValue(this, ::appearanceMode)
        set(value) = appearanceModeDelegate.setValue(this, ::appearanceMode, value)

    val appearanceModeFlow: Flow<AppearanceMode>
        get() = appearanceModeDelegate.flow

    companion object {
        private val THEME_STYLE_KEY =
            PreferenceKey("themeStyle", ThemeStyle.defaultStyle.name)

        private val APPEARANCE_MODE_KEY =
            PreferenceKey("appearanceMode", AppearanceMode.defaultMode.name)

        private val THEME_STYLE_TRANSFORMER = PreferenceTransformer.enumTransformer(
            defaultValue = { ThemeStyle.defaultStyle },
            valueOf = { raw: String -> ThemeStyle.entries.firstOrNull { it.name == raw } },
            toRaw = { it.name },
        )

        private val APPEARANCE_MODE_TRANSFORMER = PreferenceTransformer.enumTransformer(
            defaultValue = { AppearanceMode.defaultMode },
            valueOf = { raw: String -> AppearanceMode.entries.firstOrNull { it.name == raw } },
            toRaw = { it.name },
        )
    }
}
