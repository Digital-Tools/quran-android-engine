package com.quranengine.domain.qurantextkit

import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurantext.SearchResults

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
            suraNames.flatMap { name ->
                term.buildSearchResults(listOf(sura.firstVerse to name))
            }
        }
        return listOf(SearchResults(source = SearchResults.Source.Quran, items = items))
    }
}
