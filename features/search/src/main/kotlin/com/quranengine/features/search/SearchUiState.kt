package com.quranengine.features.search

import com.quranengine.model.qurantext.SearchResults

sealed class SearchUiState {
    data class Entry(
        val recents: List<String>,
        val populars: List<String>,
    ) : SearchUiState()

    data class Searching(val term: String) : SearchUiState()

    data class Results(
        val term: String,
        val results: List<SearchResults>,
    ) : SearchUiState()

    data class NoResults(val term: String) : SearchUiState()
}
