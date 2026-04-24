package com.quranengine.model.qurankit.lastayahfinder

import com.quranengine.model.qurankit.AyahNumber

class JuzBasedLastAyahFinder : LastAyahFinder {
    override fun findLastAyah(startAyah: AyahNumber): AyahNumber =
        startAyah.page.startJuz.lastVerse
}
