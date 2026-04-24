package com.quranengine.features.translationverse

import com.quranengine.features.qurantranslation.TranslationItem

data class TranslationVerseState(
    val verse: Int = 0,
    val items: List<TranslationItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: Throwable? = null,
)
