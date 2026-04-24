package com.quranengine.data.annotation.persistence

import com.quranengine.data.annotation.dao.LastPageDao
import com.quranengine.data.annotation.entity.LastPageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLastPagePersistence(
    private val dao: LastPageDao,
) : LastPagePersistence {

    override fun lastPages(): Flow<List<LastPagePersistenceModel>> =
        dao.observeLastPages().map { entities ->
            entities.map { it.toModel() }
        }

    override suspend fun retrieveAll(): List<LastPagePersistenceModel> =
        dao.getAll().map { it.toModel() }

    override suspend fun add(page: Int): LastPagePersistenceModel {
        val now = System.currentTimeMillis()
        val entity = LastPageEntity(page = page, createdOn = now, modifiedOn = now)
        dao.insertWithOverflow(entity)
        return LastPagePersistenceModel(page = page, createdOn = now, modifiedOn = now)
    }

    override suspend fun update(page: Int, toPage: Int): LastPagePersistenceModel {
        val now = System.currentTimeMillis()
        val updated = dao.updatePage(oldPage = page, newPage = toPage, modifiedOn = now)
        if (updated == 0) {
            return add(toPage)
        }
        return LastPagePersistenceModel(page = toPage, createdOn = now, modifiedOn = now)
    }

    private fun LastPageEntity.toModel() =
        LastPagePersistenceModel(page = page, createdOn = createdOn, modifiedOn = modifiedOn)
}
