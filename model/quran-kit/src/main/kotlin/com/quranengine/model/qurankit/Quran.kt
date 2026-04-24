package com.quranengine.model.qurankit

class Quran internal constructor(internal val raw: QuranReadingInfoRawData) {

    companion object {
        val hafsMadani1405: Quran by lazy { Quran(Madani1405QuranReadingInfoRawData) }
        val hafsMadani1440: Quran by lazy { Quran(Madani1440QuranReadingInfoRawData) }
    }

    val arabicBesmAllah: String get() = raw.arabicBesmAllah

    val suras: List<Sura> by lazy {
        surasRange.map { Sura(this, it)!! }
    }

    val pages: List<Page> by lazy {
        pagesRange.map { Page(this, it)!! }
    }

    val juzs: List<Juz> by lazy {
        (1..numberOfJuzs).map { Juz(this, it) }
    }

    val quarters: List<Quarter> by lazy {
        (1..raw.quarters.size).map { Quarter(this, it) }
    }

    val hizbs: List<Hizb> by lazy {
        (1..numberOfHizbs).map { Hizb(this, it) }
    }

    val verses: List<AyahNumber> by lazy {
        suras.flatMap { it.verses }
    }

    val firstVerse: AyahNumber get() = verses.first()
    val lastVerse: AyahNumber get() = verses.last()
    val firstSura: Sura get() = suras.first()

    internal val pagesRange: IntRange get() = 1..raw.startSuraOfPage.size
    internal val surasRange: IntRange get() = 1..raw.startPageOfSura.size

    private val numberOfHizbs: Int get() = raw.quarters.size / QUARTERS_PER_HIZB
    private val numberOfJuzs: Int get() = numberOfHizbs / HIZBS_PER_JUZ

    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
    override fun toString(): String = "Quran(pages=${pagesRange.last}, suras=${surasRange.last})"
}

private const val QUARTERS_PER_HIZB = 4
private const val HIZBS_PER_JUZ = 2
