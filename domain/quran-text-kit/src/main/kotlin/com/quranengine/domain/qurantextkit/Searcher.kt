package com.quranengine.domain.qurantextkit

import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurantext.SearchResults

interface Searcher {
    suspend fun autocomplete(term: SearchTerm, quran: Quran): List<String>
    suspend fun search(term: SearchTerm, quran: Quran): List<SearchResults>
}
