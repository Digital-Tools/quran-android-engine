package com.quranengine.model.qurankit

class Page internal constructor(
    val quran: Quran,
    val pageNumber: Int
) : Navigatable<Page>, QuranGroup {

    companion object {
        internal operator fun invoke(quran: Quran, pageNumber: Int): Page? {
            if (pageNumber !in quran.pagesRange) return null
            return Page(quran, pageNumber)
        }
    }

    val startSura: Sura
        get() = Sura(quran, quran.raw.startSuraOfPage[pageNumber - 1])!!

    val startJuz: Juz
        get() = quran.juzs.binarySearchFirst { this >= it.page }

    val quarter: Quarter?
        get() = quran.quarters.firstOrNull { it.page == this }

    override val firstVerse: AyahNumber
        get() = AyahNumber(startSura, quran.raw.startAyahOfPage[pageNumber - 1])!!

    override val lastVerse: AyahNumber
        get() {
            val nextPage = next ?: return quran.lastVerse
            return nextPage.firstVerse.previous!!
        }

    override val next: Page?
        get() {
            if (this == quran.pages.last()) return null
            return Page(quran, pageNumber + 1)
        }

    override val previous: Page?
        get() {
            if (this == quran.pages.first()) return null
            return Page(quran, pageNumber - 1)
        }

    override fun compareTo(other: Page): Int = pageNumber.compareTo(other.pageNumber)
    override fun equals(other: Any?): Boolean = other is Page && pageNumber == other.pageNumber
    override fun hashCode(): Int = pageNumber
    override fun toString(): String = "<Page value=$pageNumber>"
}
