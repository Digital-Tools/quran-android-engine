package com.quranengine.model.qurankit

data class Word(
    val verse: AyahNumber,
    val wordNumber: Int
) : Comparable<Word> {
    override fun compareTo(other: Word): Int {
        val verseCompare = verse.compareTo(other.verse)
        if (verseCompare != 0) return verseCompare
        return wordNumber.compareTo(other.wordNumber)
    }
}
