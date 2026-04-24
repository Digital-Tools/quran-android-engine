package com.quranengine.data.annotation.persistence

import kotlinx.coroutines.flow.Flow

interface PageBookmarkPersistence {
    fun pageBookmarks(): Flow<List<PageBookmarkPersistenceModel>>
    suspend fun insertPageBookmark(page: Int)
    suspend fun removePageBookmark(page: Int)
    suspend fun removeAllPageBookmarks()
}
