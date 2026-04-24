package com.quranengine.core.caching

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Interface for types that represent a page with a page number.
 * Port of Swift's Pageable protocol.
 */
interface Pageable {
    val pageNumber: Int
}

/**
 * A cacheable service that loads pages and preloads neighboring pages.
 *
 * Port of Swift's PagesCacheableService.
 *
 * @param cache The cache to store page results.
 * @param previousPagesCount Number of pages before the current page to preload.
 * @param nextPagesCount Number of pages after the current page to preload.
 * @param pages The full ordered list of pages.
 * @param operation The async operation to load a page.
 * @param scope Coroutine scope for background preloading.
 */
class PagesCacheableService<Input : Pageable, Output>(
    cache: Cache<Input, Output>,
    private val previousPagesCount: Int,
    private val nextPagesCount: Int,
    private val pages: List<Input>,
    operation: CacheableOperation<Input, Output>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val service = OperationCacheableService(cache, operation, scope)

    fun invalidate() {
        service.invalidate()
    }

    suspend fun get(page: Input): Output {
        try {
            return service.get(page)
        } finally {
            cachePagesCloserToPage(page)
        }
    }

    fun getCached(input: Input): Output? {
        return service.getCached(input)
    }

    private fun cachePagesCloserToPage(page: Input) {
        fun cacheCloser(pageNumber: Int) {
            val target = pages.firstOrNull { it.pageNumber == pageNumber } ?: return
            scope.launch {
                runCatching { service.get(target) }
            }
        }

        // Load next pages
        for (index in 0 until nextPagesCount) {
            cacheCloser(page.pageNumber + 1 + index)
        }

        // Load previous pages
        for (index in 0 until previousPagesCount) {
            cacheCloser(page.pageNumber - 1 - index)
        }
    }
}
