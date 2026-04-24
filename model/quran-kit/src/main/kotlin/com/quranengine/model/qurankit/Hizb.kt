package com.quranengine.model.qurankit

class Hizb internal constructor(
    val quran: Quran,
    val hizbNumber: Int
) : Navigatable<Hizb>, QuranGroup {

    val quarter: Quarter
        get() {
            val quarterNumber = (hizbNumber - 1) * (quran.quarters.size / quran.hizbs.size) + 1
            return quran.quarters[quarterNumber - 1]
        }

    val juz: Juz get() = quarter.juz

    override val firstVerse: AyahNumber get() = quarter.firstVerse

    override val lastVerse: AyahNumber
        get() {
            val nextHizb = next ?: return quran.lastVerse
            return nextHizb.firstVerse.previous!!
        }

    override val next: Hizb?
        get() {
            if (this == quran.hizbs.last()) return null
            return Hizb(quran, hizbNumber + 1)
        }

    override val previous: Hizb?
        get() {
            if (this == quran.hizbs.first()) return null
            return Hizb(quran, hizbNumber - 1)
        }

    override fun compareTo(other: Hizb): Int = hizbNumber.compareTo(other.hizbNumber)
    override fun equals(other: Any?): Boolean = other is Hizb && hizbNumber == other.hizbNumber
    override fun hashCode(): Int = hizbNumber
    override fun toString(): String = "<Hizb value=$hizbNumber>"
}
