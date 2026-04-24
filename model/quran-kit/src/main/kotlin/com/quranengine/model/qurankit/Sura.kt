package com.quranengine.model.qurankit

class Sura internal constructor(
    val quran: Quran,
    val suraNumber: Int
) : Navigatable<Sura>, QuranGroup {

    companion object {
        operator fun invoke(quran: Quran, suraNumber: Int): Sura? {
            if (suraNumber !in quran.surasRange) return null
            return Sura(quran, suraNumber)
        }
    }

    val startsWithBesmAllah: Boolean
        get() = this != quran.suras.first() && suraNumber != 9

    val isMakki: Boolean
        get() = quran.raw.isMakkiSura[suraNumber - 1]

    val page: Page
        get() = Page(quran, quran.raw.startPageOfSura[suraNumber - 1])!!

    override val firstVerse: AyahNumber
        get() = AyahNumber(this, 1)!!

    override val lastVerse: AyahNumber
        get() = AyahNumber(this, numberOfVerses)!!

    internal val numberOfVerses: Int
        get() = quran.raw.numberOfAyahsInSura[suraNumber - 1]

    override val next: Sura?
        get() {
            if (this == quran.suras.last()) return null
            return Sura(quran, suraNumber + 1)
        }

    override val previous: Sura?
        get() {
            if (this == quran.suras.first()) return null
            return Sura(quran, suraNumber - 1)
        }

    override fun compareTo(other: Sura): Int = suraNumber.compareTo(other.suraNumber)
    override fun equals(other: Any?): Boolean = other is Sura && suraNumber == other.suraNumber
    override fun hashCode(): Int = suraNumber
    override fun toString(): String = "<Sura value=$suraNumber>"
}
