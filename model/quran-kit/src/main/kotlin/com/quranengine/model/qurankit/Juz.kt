package com.quranengine.model.qurankit

class Juz internal constructor(
    val quran: Quran,
    val juzNumber: Int,
) : QuranGroup, Navigatable<Juz> {

    val hizb: Hizb
        get() {
            val hizbNumber = (juzNumber - 1) * (quran.hizbs.size / quran.juzs.size) + 1
            return quran.hizbs[hizbNumber - 1]
        }

    val quarter: Quarter get() = hizb.quarter

    val page: Page get() = firstVerse.page

    override val firstVerse: AyahNumber get() = quarter.firstVerse

    override val lastVerse: AyahNumber
        get() = next?.firstVerse?.previous ?: quran.lastVerse

    override val next: Juz?
        get() = if (this == quran.juzs.last()) null
        else Juz(quran, juzNumber + 1)

    override val previous: Juz?
        get() = if (this == quran.juzs.first()) null
        else Juz(quran, juzNumber - 1)

    override fun compareTo(other: Juz): Int = juzNumber.compareTo(other.juzNumber)

    override fun equals(other: Any?): Boolean =
        other is Juz && juzNumber == other.juzNumber

    override fun hashCode(): Int = juzNumber

    override fun toString(): String = "<Juz value=$juzNumber>"
}
