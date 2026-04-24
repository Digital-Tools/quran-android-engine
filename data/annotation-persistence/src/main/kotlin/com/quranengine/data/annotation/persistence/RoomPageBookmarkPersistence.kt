package com.quranengine.data.annotation.persistence

import com.quranengine.data.annotation.dao.PageBookmarkDao
import com.quranengine.data.annotation.entity.PageBookmarkEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPageBookmarkPersistence(
    private val dao: PageBookmarkDao,
) : PageBookmarkPersistence {

    override fun pageBookmarks(): Flow<List<PageBookmarkPersistenceModel>> =
        dao.observeBookmarks().map { entities ->
            entities.map { it.toModel() }
        }

    override suspend fun insertPageBookmark(page: Int) {
        val now = System.currentTimeMillis()
        dao.insert(PageBookmarkEntity(page = page, createdOn = now, modifiedOn = now))
    }

    override suspend fun removePageBookmark(page: Int) {
        dao.deleteByPage(page)
    }

    override suspend fun removeAllPageBookmarks() {
        dao.deleteAll()
    }

    private fun PageBookmarkEntity.toModel() =
        PageBookmarkPersistenceModel(page = page, creationDate = createdOn)
}
