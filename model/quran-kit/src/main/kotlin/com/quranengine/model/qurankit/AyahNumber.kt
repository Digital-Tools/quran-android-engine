package com.quranengine.model.qurankit

class AyahNumber private constructor(
    val sura: Sura,
    val ayah: Int
) : Navigatable<AyahNumber> {

    companion object {
        operator fun invoke(quran: Quran, sura: Int, ayah: Int): AyahNumber? {
            val s = Sura(quran, sura) ?: return null
            return invoke(s, ayah)
        }

        operator fun invoke(sura: Sura, ayah: Int): AyahNumber? {
            if (ayah !in 1..sura.numberOfVerses) return null
            return AyahNumber(sura, ayah)
        }
    }

    val quran: Quran get() = sura.quran

    val page: Page
        get() = quran.pages.binarySearchFirst { this >= it.firstVerse }

    override val previous: AyahNumber?
        get() {
            if (this != sura.firstVerse) {
                return AyahNumber(sura, ayah - 1)
            }
            return sura.previous?.lastVerse
        }

    override val next: AyahNumber?
        get() {
            if (this != sura.lastVerse) {
                return AyahNumber(sura, ayah + 1)
            }
            return sura.next?.firstVerse
        }

    override fun compareTo(other: AyahNumber): Int {
        val suraCompare = sura.compareTo(other.sura)
        if (suraCompare != 0) return suraCompare
        return ayah.compareTo(other.ayah)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AyahNumber) return false
        return sura == other.sura && ayah == other.ayah
    }

    override fun hashCode(): Int = 31 * sura.hashCode() + ayah
    override fun toString(): String = "<AyahNumber sura=${sura.suraNumber} ayah=$ayah>"
}
