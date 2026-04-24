package com.quranengine.model.qurankit

interface QuranReadingInfoRawData {
    val arabicBesmAllah: String
    val startPageOfSura: List<Int>
    val startSuraOfPage: List<Int>
    val startAyahOfPage: List<Int>
    val numberOfAyahsInSura: List<Int>
    val isMakkiSura: List<Boolean>
    val quarters: List<SuraAyah>
}

data class SuraAyah(val sura: Int, val ayah: Int)
