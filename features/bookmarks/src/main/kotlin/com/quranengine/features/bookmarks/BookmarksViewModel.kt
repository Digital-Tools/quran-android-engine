package com.quranengine.features.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.domain.annotationservice.PageBookmarkService
import com.quranengine.model.quranannotations.PageBookmark
import com.quranengine.model.qurankit.Quran
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val quran: Quran,
    private val bookmarkService: PageBookmarkService,
) : ViewModel() {

    val bookmarks: StateFlow<List<PageBookmark>> = bookmarkService.pageBookmarks(quran)
        .map { it.sortedByDescending { b -> b.creationDate } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    fun deleteBookmark(bookmark: PageBookmark) {
        viewModelScope.launch {
            try {
                bookmarkService.removePageBookmark(bookmark.page)
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

    fun deleteAllBookmarks() {
        viewModelScope.launch {
            try {
                bookmarkService.removeAllPageBookmarks()
            } catch (e: Exception) {
                _error.value = e
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
