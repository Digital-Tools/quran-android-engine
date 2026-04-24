package com.quranengine.model.qurankit.lastayahfinder

import com.quranengine.model.qurankit.AyahNumber

interface LastAyahFinder {
    fun findLastAyah(startAyah: AyahNumber): AyahNumber
}
