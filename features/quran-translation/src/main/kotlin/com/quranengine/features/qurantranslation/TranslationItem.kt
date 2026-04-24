package com.quranengine.features.qurantranslation

import androidx.compose.ui.graphics.Color

sealed class TranslationItemId {
    data class PageHeader(val page: Int) : TranslationItemId()
    data class PageFooter(val page: Int) : TranslationItemId()
    data class Separator(val verse: Int) : TranslationItemId()
    data class SuraName(val sura: Int) : TranslationItemId()
    data class ArabicText(val verse: Int) : TranslationItemId()
    data class Translator(val verse: Int, val translationId: Long) : TranslationItemId()
    data class TranslationReference(val verse: Int, val translationId: Long) : TranslationItemId()
    data class TranslationTextChunk(val verse: Int, val translationId: Long, val chunkIndex: Int) : TranslationItemId()

    val ayah: Int?
        get() = when (this) {
            is PageHeader, is PageFooter -> null
            is SuraName -> sura
            is Separator -> verse
            is ArabicText -> verse
            is Translator -> verse
            is TranslationReference -> verse
            is TranslationTextChunk -> verse
        }
}

sealed class TranslationItem {
    abstract val id: TranslationItemId
    abstract val highlightColor: Color?

    data class PageHeader(val page: Int) : TranslationItem() {
        override val id = TranslationItemId.PageHeader(page)
        override val highlightColor: Color? = null
    }

    data class PageFooter(val page: Int) : TranslationItem() {
        override val id = TranslationItemId.PageFooter(page)
        override val highlightColor: Color? = null
    }

    data class VerseSeparator(
        val verse: Int,
        override val highlightColor: Color? = null,
    ) : TranslationItem() {
        override val id = TranslationItemId.Separator(verse)
    }

    data class SuraName(
        val sura: Int,
        val suraName: String,
        override val highlightColor: Color? = null,
    ) : TranslationItem() {
        override val id = TranslationItemId.SuraName(sura)
    }

    data class ArabicText(
        val verse: Int,
        val text: String,
        override val highlightColor: Color? = null,
    ) : TranslationItem() {
        override val id = TranslationItemId.ArabicText(verse)
    }

    data class TranslatorName(
        val verse: Int,
        val translationId: Long,
        val name: String,
        override val highlightColor: Color? = null,
    ) : TranslationItem() {
        override val id = TranslationItemId.Translator(verse, translationId)
    }

    data class TranslationReferenceVerse(
        val verse: Int,
        val translationId: Long,
        val referenceVerse: Int,
        override val highlightColor: Color? = null,
    ) : TranslationItem() {
        override val id = TranslationItemId.TranslationReference(verse, translationId)
    }

    data class TranslationTextChunk(
        val verse: Int,
        val translationId: Long,
        val chunkIndex: Int,
        val text: String,
        val isArabic: Boolean = false,
        val showReadMore: Boolean = false,
        override val highlightColor: Color? = null,
    ) : TranslationItem() {
        override val id = TranslationItemId.TranslationTextChunk(verse, translationId, chunkIndex)
    }
}
