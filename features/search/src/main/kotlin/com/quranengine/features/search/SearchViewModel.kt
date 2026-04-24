package com.quranengine.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.domain.qurantextkit.SearchRecentsService
import com.quranengine.domain.qurantextkit.SearchTerm
import com.quranengine.domain.qurantextkit.Searcher
import com.quranengine.model.qurankit.Quran
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searcher: Searcher,
    private val recentsService: SearchRecentsService,
    private val quran: Quran,
) : ViewModel() {
    val searchTerm = MutableStateFlow("")

    private val searchResultState = MutableStateFlow<SearchResultInternal?>(null)

    val autocompletions: StateFlow<List<String>> = searchTerm
        .debounce(300)
        .distinctUntilChanged()
        .map { term ->
            val parsed = SearchTerm(term) ?: return@map emptyList()
            try {
                searcher.autocomplete(parsed, quran)
            } catch (e: Exception) {
                Timber.e(e, "Autocomplete failed for: %s", term)
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<SearchUiState> = combine(
        searchResultState,
        recentsService.recentSearchItemsFlow,
    ) { result, recents ->
        when (result) {
            null -> SearchUiState.Entry(
                recents = recents,
                populars = recentsService.popularTerms,
            )
            is SearchResultInternal.Loading -> SearchUiState.Searching(term = result.term)
            is SearchResultInternal.Loaded -> {
                if (result.results.isEmpty()) {
                    SearchUiState.NoResults(term = result.term)
                } else {
                    SearchUiState.Results(term = result.term, results = result.results)
                }
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SearchUiState.Entry(
            recents = recentsService.recentSearchItems,
            populars = recentsService.popularTerms,
        ),
    )

    fun search(term: String) {
        val parsed = SearchTerm(term) ?: return
        searchTerm.value = term
        searchResultState.value = SearchResultInternal.Loading(term)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = searcher.search(parsed, quran)
                searchResultState.value = SearchResultInternal.Loaded(term, results)
                recentsService.addToRecents(term)
            } catch (e: Exception) {
                Timber.e(e, "Search failed for: %s", term)
                searchResultState.value = SearchResultInternal.Loaded(term, emptyList())
            }
        }
    }

    fun selectRecent(term: String) {
        search(term)
    }

    fun clearRecents() {
        recentsService.reset()
    }

    fun clearSearch() {
        searchTerm.value = ""
        searchResultState.value = null
    }

    private sealed class SearchResultInternal {
        data class Loading(val term: String) : SearchResultInternal()
        data class Loaded(
            val term: String,
            val results: List<com.quranengine.model.qurantext.SearchResults>,
        ) : SearchResultInternal()
    }
}
