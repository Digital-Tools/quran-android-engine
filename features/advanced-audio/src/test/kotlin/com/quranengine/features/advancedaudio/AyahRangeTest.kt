package com.quranengine.features.advancedaudio

import com.google.common.truth.Truth.assertThat
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurankit.lastayahfinder.PageBasedLastAyahFinder
import org.junit.Test

class AyahRangeTest {
    private val quran = Quran.hafsMadani1405
    private val fatihah = quran.suras[0]
    private val baqarah = quran.suras[1]
    private val imran = quran.suras[2]

    @Test
    fun `changing to a longer surah keeps the ayah number`() {
        val selection = fatihah.lastVerse.selecting(baqarah, minimum = null)

        assertThat(selection).isEqualTo(AyahNumber(baqarah, 7))
    }

    @Test
    fun `changing to a shorter surah clamps to its last ayah`() {
        val selection = baqarah.lastVerse.selecting(fatihah, minimum = null)

        assertThat(selection).isEqualTo(fatihah.lastVerse)
    }

    @Test
    fun `To never goes before From in the same surah`() {
        val from = AyahNumber(baqarah, 100)!!
        val selection = imran.firstVerse.selecting(baqarah, minimum = from)

        assertThat(selection).isEqualTo(from)
    }

    @Test
    fun `end of a surah reads as Surah`() {
        assertThat(deduceEndAt(fatihah.firstVerse, fatihah.lastVerse)).isEqualTo(EndAtChoice.SURA)
    }

    @Test
    fun `end of the start page reads as Page`() {
        val start = baqarah.firstVerse
        val end = PageBasedLastAyahFinder().findLastAyah(start)

        assertThat(deduceEndAt(start, end)).isEqualTo(EndAtChoice.PAGE)
    }

    @Test
    fun `any other end reads as Custom`() {
        val start = baqarah.firstVerse

        assertThat(deduceEndAt(start, start.next!!)).isEqualTo(EndAtChoice.CUSTOM)
    }
}
