package com.quranengine.domain.qurantextkit

import com.quranengine.data.versetext.SearchableTextPersistence
import com.quranengine.domain.translationservice.LocalTranslationsRetriever
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurantext.SearchResults
import com.quranengine.model.qurantext.Translation

class TranslationSearcher(
    private val localTranslationRetriever: LocalTranslationsRetriever,
    private val versePersistenceBuilder: (Translation) -> SearchableTextPersistence,
) : Searcher {

    override suspend fun autocomplete(term: SearchTerm, quran: Quran): List<String> {
        val translations = getDownloadedTranslations()
        for (translation in translations) {
            val persistence = versePersistenceBuilder(translation)
            val persistenceSearcher = PersistenceSearcher(
                versePersistence = persistence,
                source = SearchResults.Source.TranslationSource(translation),
            )
            val results = persistenceSearcher.autocomplete(term, quran)
            if (results.isNotEmpty()) return results
        }
        return emptyList()
    }

    override suspend fun search(term: SearchTerm, quran: Quran): List<SearchResults> {
        val translations = getDownloadedTranslations()
        return translations.flatMap { translation ->
            val persistence = versePersistenceBuilder(translation)
            val persistenceSearcher = PersistenceSearcher(
                versePersistence = persistence,
                source = SearchResults.Source.TranslationSource(translation),
            )
            persistenceSearcher.search(term, quran)
        }
    }

    private suspend fun getDownloadedTranslations(): List<Translation> {
        return localTranslationRetriever.getLocalTranslations().filter { it.isDownloaded }
    }
}
