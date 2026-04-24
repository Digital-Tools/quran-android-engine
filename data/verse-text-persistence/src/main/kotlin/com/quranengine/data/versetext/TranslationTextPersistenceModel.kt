package com.quranengine.data.versetext

import com.quranengine.model.qurankit.AyahNumber

sealed class TranslationTextPersistenceModel {
    data class Text(val text: String) : TranslationTextPersistenceModel()
    data class Reference(val verse: AyahNumber) : TranslationTextPersistenceModel()
}
