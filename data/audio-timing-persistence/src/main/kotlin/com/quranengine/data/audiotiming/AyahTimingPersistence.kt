package com.quranengine.data.audiotiming

import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.quranaudio.SuraTiming

interface AyahTimingPersistence {
    suspend fun getVersion(): Int
    suspend fun getOrderedTimingForSura(startAyah: AyahNumber): SuraTiming
}
