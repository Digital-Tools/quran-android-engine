package com.quranengine.model.qurantext

import com.quranengine.model.qurankit.AyahNumber

data class TranslationString(
    val text: String,
    val quranRanges: List<IntRange>,
    val footnoteRanges: List<IntRange>,
    val footnotes: List<String>
)

sealed class TranslationText {
    data class StringText(val value: TranslationString) : TranslationText()
    data class Reference(val ayah: AyahNumber) : TranslationText()
}

data class VerseText(
    val arabicText: String,
    val translations: List<TranslationText>,
    val arabicPrefix: List<String>,
    val arabicSuffix: List<String>
)

data class TranslatedVerses(
    val translations: List<Translation>,
    val verses: List<VerseText>
)
