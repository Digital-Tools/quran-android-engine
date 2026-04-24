package com.quranengine.domain.annotationservice

import com.quranengine.data.annotation.persistence.PageBookmarkPersistence
import com.quranengine.data.annotation.persistence.PageBookmarkPersistenceModel
import com.quranengine.model.quranannotations.PageBookmark
import com.quranengine.model.qurankit.Page
import com.quranengine.model.qurankit.Quran
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class PageBookmarkService(
    private val persistence: PageBookmarkPersistence,
) {
    fun pageBookmarks(quran: Quran): Flow<List<PageBookmark>> =
        persistence.pageBookmarks().map { bookmarks ->
            bookmarks.mapNotNull { it.toPageBookmark(quran) }
        }

    suspend fun insertPageBookmark(page: Page) {
        persistence.insertPageBookmark(page.pageNumber)
    }

    suspend fun removePageBookmark(page: Page) {
        persistence.removePageBookmark(page.pageNumber)
    }

    suspend fun removeAllPageBookmarks() {
        persistence.removeAllPageBookmarks()
    }
}

private fun PageBookmarkPersistenceModel.toPageBookmark(quran: Quran): PageBookmark? {
    val p = quran.pages.firstOrNull { it.pageNumber == page } ?: return null
    return PageBookmark(
        page = p,
        creationDate = Instant.ofEpochMilli(creationDate),
    )
}
