package com.quranengine.domain.qurantextkit

import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurantext.SearchResult
import com.quranengine.model.qurantext.SearchResults
import java.text.Normalizer

class SuraSearcher(
    private val localizedSuraName: (suraNumber: Int, withPrefix: Boolean, language: String?) -> String,
) : Searcher {

    override suspend fun autocomplete(term: SearchTerm, quran: Quran): List<String> {
        val suraNames = mutableSetOf<String>()
        for (sura in quran.suras) {
            suraNames.add(localizedSuraName(sura.suraNumber, true, null))
            suraNames.add(localizedSuraName(sura.suraNumber, true, "ar"))
        }
        return term.buildAutocompletions(suraNames.toList())
    }

    override suspend fun search(term: SearchTerm, quran: Quran): List<SearchResults> {
        val items = quran.suras.flatMap { sura ->
            val defaultName = localizedSuraName(sura.suraNumber, true, null)
            val arabicName = localizedSuraName(sura.suraNumber, true, "ar")
            val suraNames = setOf(defaultName, arabicName)
            val matches = suraNames.flatMap { name ->
                term.buildSearchResults(listOf(sura.firstVerse to name))
            }
            if (matches.isNotEmpty()) return@flatMap matches

            // Typed names rarely carry the transliteration's hyphens or ʿ marks:
            // "al kahf", "alkahf" and "ali imran" should still find the sura.
            val looseNames = defaultName.looseSpellings()
            if (looseNames.any { term.buildSearchResults(listOf(sura.firstVerse to it)).isNotEmpty() }) {
                listOf(SearchResult(text = defaultName, ranges = listOf(defaultName.indices), ayah = sura.firstVerse))
            } else {
                emptyList()
            }
        }
        return listOf(SearchResults(source = SearchResults.Source.Quran, items = items))
    }
}

/** "Āli-ʿImrān" -> ["Ali Imran", "AliImran"]. */
internal fun String.looseSpellings(): List<String> {
    val spaced = Normalizer.normalize(this, Normalizer.Form.NFKD)
        .replace(Regex("[\\p{M}ʿʾ'’`]"), "")
        .replace('-', ' ')
    return listOf(spaced, spaced.replace(" ", ""))
}
