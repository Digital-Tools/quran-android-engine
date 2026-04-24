package com.quranengine.model.qurantext

import com.quranengine.model.qurankit.AyahNumber

data class SearchResult(
    val text: String,
    val ranges: List<IntRange>,
    val ayah: AyahNumber
)

data class SearchResults(
    val source: Source,
    val items: List<SearchResult>
) {
    sealed class Source : Comparable<Source> {
        data object Quran : Source() {
            override val name: String get() = "Quran"
        }

        data class TranslationSource(val translation: Translation) : Source() {
            override val name: String get() = "Translation"
        }

        abstract val name: String

        override fun compareTo(other: Source): Int = when {
            this is Quran && other is Quran -> 0
            this is Quran -> -1
            other is Quran -> 1
            else -> {
                val thisTranslation = (this as TranslationSource).translation
                val otherTranslation = (other as TranslationSource).translation
                thisTranslation.compareTo(otherTranslation)
            }
        }
    }
}
