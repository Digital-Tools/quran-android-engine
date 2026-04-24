package com.quranengine.features.home

import com.quranengine.model.qurankit.Juz
import com.quranengine.model.qurankit.Quarter

data class QuarterItem(
    val quarter: Quarter,
    val juz: Juz,
    val localizedName: String,
    val localizedJuzName: String,
    val pageDescription: String,
)
