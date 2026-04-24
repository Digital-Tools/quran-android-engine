package com.quranengine.domain.qurantextkit

import com.quranengine.data.versetext.VerseTextPersistence
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurantext.SearchResult
import com.quranengine.model.qurantext.SearchResults

class NumberSearcher(
    private val quranVerseTextPersistence: VerseTextPersistence,
    private val localizedSuraName: (suraNumber: Int, withPrefix: Boolean) -> String,
    private val localizedJuzName: (juzNumber: Int) -> String,
    private val localizedHizbName: (hizbNumber: Int) -> String,
    private val localizedPageName: (pageNumber: Int) -> String,
) : Searcher {

    override suspend fun autocomplete(term: SearchTerm, quran: Quran): List<String> {
        return if (term.compactQuery.toIntOrNull() != null) {
            term.buildAutocompletions(listOf(term.compactQuery))
        } else {
            emptyList()
        }
    }

    override suspend fun search(term: SearchTerm, quran: Quran): List<SearchResults> {
        val items: List<SearchResult> = searchInternal(term, quran)
        return listOf(SearchResults(source = SearchResults.Source.Quran, items = items))
    }

    private suspend fun searchInternal(term: SearchTerm, quran: Quran): List<SearchResult> {
        val components = parseIntArray(term.compactQuery)
        if (components.isEmpty()) return emptyList()

        return if (components.size == 2) {
            listOfNotNull(parseVerseResult(components[0], components[1], quran))
        } else {
            listOfNotNull(
                parseSuraResult(components[0], quran),
                parseJuzResult(components[0], quran),
                parseHizbResult(components[0], quran),
                parsePageResult(components[0], quran),
            )
        }
    }

    private suspend fun parseVerseResult(sura: Int, verse: Int, quran: Quran): SearchResult? {
        val ayah = quran.verses.firstOrNull {
            it.sura.suraNumber == sura && it.ayah == verse
        } ?: return null
        val ayahText = quranVerseTextPersistence.textForVerse(ayah)
        return SearchResult(text = ayahText, ranges = emptyList(), ayah = ayah)
    }

    private fun parseSuraResult(sura: Int, quran: Quran): SearchResult? {
        val s = quran.suras.firstOrNull { it.suraNumber == sura } ?: return null
        return SearchResult(
            text = localizedSuraName(s.suraNumber, true),
            ranges = emptyList(),
            ayah = s.firstVerse,
        )
    }

    private fun parsePageResult(page: Int, quran: Quran): SearchResult? {
        val p = quran.pages.firstOrNull { it.pageNumber == page } ?: return null
        return SearchResult(
            text = localizedPageName(p.pageNumber),
            ranges = emptyList(),
            ayah = p.firstVerse,
        )
    }

    private fun parseJuzResult(juz: Int, quran: Quran): SearchResult? {
        val j = quran.juzs.firstOrNull { it.juzNumber == juz } ?: return null
        return SearchResult(
            text = localizedJuzName(j.juzNumber),
            ranges = emptyList(),
            ayah = j.firstVerse,
        )
    }

    private fun parseHizbResult(hizb: Int, quran: Quran): SearchResult? {
        val h = quran.hizbs.firstOrNull { it.hizbNumber == hizb } ?: return null
        return SearchResult(
            text = localizedHizbName(h.hizbNumber),
            ranges = emptyList(),
            ayah = h.firstVerse,
        )
    }

    private fun parseIntArray(term: String): List<Int> {
        val components = term.split(":")
        if (components.isEmpty() || components.size > 2) return emptyList()
        val result = components.mapNotNull { parseAsInt(it) }
        return if (result.size != components.size) emptyList() else result
    }

    private fun parseAsInt(value: String): Int? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        // Only accept integer values (no decimals)
        if (trimmed.contains('.')) return null
        return trimmed.toIntOrNull()
    }
}
