package com.quranengine.domain.wordtextservice

import com.quranengine.data.wordtext.WordTextPersistence
import com.quranengine.model.qurankit.Word
import com.quranengine.model.qurantext.WordTextType

class WordTextService(
    private val persistence: WordTextPersistence,
    private val preferences: WordTextPreferences,
) {
    suspend fun textForWord(word: Word): String? {
        return when (preferences.wordTextType) {
            WordTextType.TRANSLATION -> persistence.translationForWord(word)
            WordTextType.TRANSLITERATION -> persistence.transliterationForWord(word)
        }
    }
}
