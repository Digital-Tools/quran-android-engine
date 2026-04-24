package com.quranengine.domain.qurantextkit

import com.quranengine.data.versetext.SearchableTextPersistence
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurantext.SearchResults

class PersistenceSearcher(
    private val versePersistence: SearchableTextPersistence,
    private val source: SearchResults.Source,
) : Searcher {

    override suspend fun autocomplete(term: SearchTerm, quran: Quran): List<String> {
        val matches = versePersistence.autocomplete(term.persistenceQuery)
        return term.buildAutocompletions(matches)
    }

    override suspend fun search(term: SearchTerm, quran: Quran): List<SearchResults> {
        val persistenceSearchTerm = term.persistenceQueryReplacingArabicSimilarityCharactersWithUnderscore()
        if (persistenceSearchTerm.isEmpty()) return emptyList()

        val matches = versePersistence.search(persistenceSearchTerm, quran)
        val items = term.buildSearchResults(matches.map { it.verse to it.text })
        return listOf(SearchResults(source = source, items = items))
    }
}
