package com.quranengine.model.qurankit.lastayahfinder

import com.quranengine.model.qurankit.AyahNumber

class SuraBasedLastAyahFinder : LastAyahFinder {
    override fun findLastAyah(startAyah: AyahNumber): AyahNumber =
        startAyah.sura.lastVerse
}
