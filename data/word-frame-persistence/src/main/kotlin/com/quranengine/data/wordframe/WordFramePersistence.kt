package com.quranengine.data.wordframe

import com.quranengine.model.qurangeometry.AyahNumberLocation
import com.quranengine.model.qurangeometry.SuraHeaderLocation
import com.quranengine.model.qurangeometry.WordFrame
import com.quranengine.model.qurankit.Page

interface WordFramePersistence {
    suspend fun wordFrameCollectionForPage(page: Page): List<WordFrame>
    suspend fun suraHeaders(page: Page): List<SuraHeaderLocation>
    suspend fun ayahNumbers(page: Page): List<AyahNumberLocation>
}
