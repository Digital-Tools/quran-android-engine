package com.quranengine.domain.qurantextkit

import com.quranengine.data.versetext.SearchableTextPersistence
import com.quranengine.data.versetext.VerseTextPersistence
import com.quranengine.domain.translationservice.LocalTranslationsRetriever
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurantext.SearchResult
import com.quranengine.model.qurantext.SearchResults
import com.quranengine.model.qurantext.Translation
import timber.log.Timber

class CompositeSearcher(
    private val simpleSearchers: List<Searcher>,
    private val translationsSearcher: Searcher,
) {
    constructor(
        quranVerseTextPersistence: VerseTextPersistence,
        localTranslationRetriever: LocalTranslationsRetriever,
        versePersistenceBuilder: (Translation) -> SearchableTextPersistence,
        localizedSuraName: (suraNumber: Int, withPrefix: Boolean, language: String?) -> String,
        localizedJuzName: (juzNumber: Int) -> String,
        localizedHizbName: (hizbNumber: Int) -> String,
        localizedPageName: (pageNumber: Int) -> String,
    ) : this(
        simpleSearchers = listOf(
            NumberSearcher(
                quranVerseTextPersistence = quranVerseTextPersistence,
                localizedSuraName = { num, prefix -> localizedSuraName(num, prefix, null) },
                localizedJuzName = localizedJuzName,
                localizedHizbName = localizedHizbName,
                localizedPageName = localizedPageName,
            ),
            SuraSearcher(localizedSuraName),
            PersistenceSearcher(
                versePersistence = quranVerseTextPersistence,
                source = SearchResults.Source.Quran,
            ),
        ),
        translationsSearcher = TranslationSearcher(
            localTranslationRetriever = localTranslationRetriever,
            versePersistenceBuilder = versePersistenceBuilder,
        ),
    )

    suspend fun autocomplete(term: String, quran: Quran): List<String> {
        val searchTerm = SearchTerm(term) ?: return emptyList()
        Timber.i("Autocompleting term: ${searchTerm.compactQuery}")

        val autocompletions = simpleSearchers.flatMap { searcher ->
            runCatching { searcher.autocomplete(searchTerm, quran) }.getOrDefault(emptyList())
        }
        val results = autocompletions.toMutableList()

        if (shouldPerformTranslationSearch(results, searchTerm.compactQuery)) {
            results += runCatching {
                translationsSearcher.autocomplete(searchTerm, quran)
            }.getOrDefault(emptyList())
        }
        if (!results.contains(searchTerm.compactQuery)) {
            results.add(0, searchTerm.compactQuery)
        }
        return results.orderedUnique()
    }

    suspend fun search(term: String, quran: Quran): List<SearchResults> {
        val searchTerm = SearchTerm(term) ?: return emptyList()
        Timber.i("Search for: ${searchTerm.compactQuery}")

        val searchResults = simpleSearchers.flatMap { searcher ->
            searcher.search(searchTerm, quran)
        }
        val results = searchResults.filter { it.items.isNotEmpty() }.toMutableList()

        if (shouldPerformTranslationSearch(results, searchTerm.compactQuery)) {
            results += translationsSearcher.search(searchTerm, quran)
                .filter { it.items.isNotEmpty() }
        }

        return groupedResults(results)
    }

    private fun groupedResults(results: List<SearchResults>): List<SearchResults> {
        val resultsPerSource = mutableMapOf<SearchResults.Source, MutableList<SearchResult>>()
        for (result in results) {
            resultsPerSource.getOrPut(result.source) { mutableListOf() }
                .addAll(result.items)
        }
        return resultsPerSource
            .map { (source, items) -> SearchResults(source = source, items = items) }
            .sortedBy { it.source }
    }

    private fun shouldPerformTranslationSearch(simpleSearchResults: List<Any>, term: String): Boolean =
        simpleSearchResults.isEmpty() || (!term.containsArabic() && !term.containsOnlyNumbers())
}
