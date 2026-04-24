package com.quranengine.data.wordtext

import com.quranengine.model.qurankit.Word

interface WordTextPersistence {
    suspend fun translationForWord(word: Word): String?
    suspend fun transliterationForWord(word: Word): String?
}
