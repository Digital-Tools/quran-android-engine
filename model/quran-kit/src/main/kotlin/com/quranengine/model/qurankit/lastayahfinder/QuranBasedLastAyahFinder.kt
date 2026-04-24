package com.quranengine.model.qurankit.lastayahfinder

import com.quranengine.model.qurankit.AyahNumber

class QuranBasedLastAyahFinder : LastAyahFinder {
    override fun findLastAyah(startAyah: AyahNumber): AyahNumber =
        startAyah.quran.suras.last().lastVerse
}
