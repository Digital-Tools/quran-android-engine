package com.quranengine.domain.annotationservice

import com.quranengine.model.qurankit.Page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class LastPageUpdater(
    private val service: LastPageService,
    private val scope: CoroutineScope,
) {
    var lastPage: Page? = null
        private set

    fun configure(initialPage: Page, lastPage: Page?) {
        this.lastPage = lastPage

        if (lastPage != null) {
            updateTo(page = initialPage, lastPage = lastPage)
        } else {
            create(page = initialPage)
        }
    }

    fun updateTo(pages: List<Page>) {
        val page = pages.minOrNull() ?: return
        val currentLastPage = lastPage ?: return
        // Don't update if it's the same page
        if (page == currentLastPage) return

        updateTo(page = page, lastPage = currentLastPage)
    }

    private fun updateTo(page: Page, lastPage: Page) {
        this.lastPage = page
        scope.launch {
            try {
                service.update(page = lastPage, toPage = page)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update last page")
            }
        }
    }

    private fun create(page: Page) {
        lastPage = page
        scope.launch {
            try {
                service.add(page = page)
            } catch (e: Exception) {
                Timber.e(e, "Failed to create a last page")
            }
        }
    }
}
