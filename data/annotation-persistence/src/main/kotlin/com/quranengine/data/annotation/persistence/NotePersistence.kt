package com.quranengine.data.annotation.persistence

import kotlinx.coroutines.flow.Flow

interface NotePersistence {
    fun notes(): Flow<List<NotePersistenceModel>>
    suspend fun setNote(
        note: String?,
        verses: List<VersePersistenceModel>,
        color: Int,
    ): NotePersistenceModel

    suspend fun removeNotes(
        with: List<VersePersistenceModel>,
    ): List<NotePersistenceModel>
}
