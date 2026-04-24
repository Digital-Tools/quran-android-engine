package com.quranengine.features.quranview

data class QuranInput(
    val initialPage: Int,
    val lastPage: Int? = null,
    val highlightingSearchAyah: Int? = null,
)
