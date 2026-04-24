package com.quranengine.model.qurankit

interface QuranGroup {
    val firstVerse: AyahNumber
    val lastVerse: AyahNumber
}

val QuranGroup.verses: List<AyahNumber>
    get() = firstVerse.arrayTo(lastVerse)
