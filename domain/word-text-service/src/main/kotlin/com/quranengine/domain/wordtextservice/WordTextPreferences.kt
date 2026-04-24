package com.quranengine.domain.wordtextservice

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.PreferenceTransformer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.preferences.TransformedPreference
import com.quranengine.model.qurantext.WordTextType

class WordTextPreferences(preferences: Preferences) {

    private val wordTextTypeDelegate = TransformedPreference(
        key = WORD_TEXT_TYPE_KEY,
        preferences = preferences,
        transformer = PreferenceTransformer.enumTransformer(
            defaultValue = { DEFAULT_WORD_TEXT_TYPE },
            valueOf = { raw -> WordTextType.entries.firstOrNull { it.ordinal == raw } },
            toRaw = { it.ordinal },
        ),
    )

    var wordTextType: WordTextType by wordTextTypeDelegate

    companion object {
        private val DEFAULT_WORD_TEXT_TYPE = WordTextType.TRANSLATION
        private val WORD_TEXT_TYPE_KEY = PreferenceKey("wordTranslationType", DEFAULT_WORD_TEXT_TYPE.ordinal)
    }
}
