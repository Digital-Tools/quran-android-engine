package com.quranengine.domain.quranaudiokit

import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.lastayahfinder.JuzBasedLastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.LastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.PageBasedLastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.QuranBasedLastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.SuraBasedLastAyahFinder
import com.quranengine.model.quranaudio.AudioEnd

class PreferencesLastAyahFinder(
    private val audioPreferences: AudioPreferences,
) : LastAyahFinder {

    override fun findLastAyah(startAyah: AyahNumber): AyahNumber {
        val pageLastVerse = pageFinder.findLastAyah(startAyah)
        val lastVerse = finder.findLastAyah(startAyah)
        return maxOf(lastVerse, pageLastVerse)
    }

    private val finder: LastAyahFinder
        get() = when (audioPreferences.audioEnd) {
            AudioEnd.JUZ -> JuzBasedLastAyahFinder()
            AudioEnd.SURA -> SuraBasedLastAyahFinder()
            AudioEnd.PAGE -> pageFinder
            AudioEnd.QURAN -> QuranBasedLastAyahFinder()
        }

    private val pageFinder: LastAyahFinder
        get() = PageBasedLastAyahFinder()
}
