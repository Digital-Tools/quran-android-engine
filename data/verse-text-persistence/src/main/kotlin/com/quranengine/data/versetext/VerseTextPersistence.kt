package com.quranengine.data.versetext

import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Quran

interface SearchableTextPersistence {
    suspend fun autocomplete(term: String): List<String>
    suspend fun search(term: String, quran: Quran): List<SearchResult>
}

data class SearchResult(
    val verse: AyahNumber,
    val text: String
)

interface VerseTextPersistence : SearchableTextPersistence {
    suspend fun textForVerse(verse: AyahNumber): String
    suspend fun textForVerses(verses: List<AyahNumber>): Map<AyahNumber, String>
}

interface TranslationVerseTextPersistence : SearchableTextPersistence {
    suspend fun textForVerse(verse: AyahNumber): TranslationTextPersistenceModel
    suspend fun textForVerses(verses: List<AyahNumber>): Map<AyahNumber, TranslationTextPersistenceModel>
}

interface DatabaseVersionPersistence {
    suspend fun getTextVersion(): Int
}
