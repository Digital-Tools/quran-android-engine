package com.quranengine.model.quranannotations

import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Word

data class QuranHighlights(
    val readingVerses: List<AyahNumber> = emptyList(),
    val shareVerses: List<AyahNumber> = emptyList(),
    val searchVerses: List<AyahNumber> = emptyList(),
    val noteVerses: Map<AyahNumber, Note> = emptyMap(),
    val pointedWord: Word? = null
) {
    fun needsScrolling(oldValue: QuranHighlights): Boolean {
        if (oldValue.readingVerses != readingVerses) return true
        if (oldValue.searchVerses != searchVerses) return true
        return false
    }

    fun firstScrollingVerse(): AyahNumber? =
        readingVerses.firstOrNull() ?: searchVerses.firstOrNull()

    fun verseToScrollTo(oldValue: QuranHighlights): AyahNumber? {
        fun verseToScrollToIfChanged(
            current: List<AyahNumber>,
            previous: List<AyahNumber>
        ): AyahNumber? {
            if (current != previous) {
                return current.lastOrNull()
            }
            return null
        }

        return verseToScrollToIfChanged(shareVerses, oldValue.shareVerses)
            ?: verseToScrollToIfChanged(readingVerses, oldValue.readingVerses)
    }
}
