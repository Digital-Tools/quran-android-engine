package com.quranengine.data.annotation.persistence

import kotlinx.coroutines.flow.Flow

interface LastPagePersistence {
    fun lastPages(): Flow<List<LastPagePersistenceModel>>
    suspend fun retrieveAll(): List<LastPagePersistenceModel>
    suspend fun add(page: Int): LastPagePersistenceModel
    suspend fun update(page: Int, toPage: Int): LastPagePersistenceModel
}
