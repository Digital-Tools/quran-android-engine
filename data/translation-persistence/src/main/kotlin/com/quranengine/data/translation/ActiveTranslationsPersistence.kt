package com.quranengine.data.translation

import com.quranengine.model.qurantext.Translation

interface ActiveTranslationsPersistence {
    suspend fun retrieveAll(): List<Translation>
    suspend fun insert(translation: Translation)
    suspend fun remove(translation: Translation)
    suspend fun update(translation: Translation)
}
