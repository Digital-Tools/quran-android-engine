package com.quranengine.model.qurankit

class Quarter internal constructor(
    val quran: Quran,
    val quarterNumber: Int
) : Navigatable<Quarter>, QuranGroup {

    override val firstVerse: AyahNumber
        get() {
            val verse = quran.raw.quarters[quarterNumber - 1]
            return AyahNumber(quran, verse.sura, verse.ayah)!!
        }

    override val lastVerse: AyahNumber
        get() {
            val nextQuarter = next ?: return quran.lastVerse
            return nextQuarter.firstVerse.previous!!
        }

    val page: Page get() = firstVerse.page

    val hizb: Hizb
        get() {
            val hizbNumber = (quarterNumber - 1) / (quran.quarters.size / quran.hizbs.size) + 1
            return quran.hizbs[hizbNumber - 1]
        }

    val juz: Juz
        get() {
            val juzNumber = (quarterNumber - 1) / (quran.quarters.size / quran.juzs.size) + 1
            return quran.juzs[juzNumber - 1]
        }

    override val next: Quarter?
        get() {
            if (this == quran.quarters.last()) return null
            return Quarter(quran, quarterNumber + 1)
        }

    override val previous: Quarter?
        get() {
            if (this == quran.quarters.first()) return null
            return Quarter(quran, quarterNumber - 1)
        }

    override fun compareTo(other: Quarter): Int = quarterNumber.compareTo(other.quarterNumber)
    override fun equals(other: Any?): Boolean = other is Quarter && quarterNumber == other.quarterNumber
    override fun hashCode(): Int = quarterNumber
    override fun toString(): String = "<Quarter value=$quarterNumber>"
}
