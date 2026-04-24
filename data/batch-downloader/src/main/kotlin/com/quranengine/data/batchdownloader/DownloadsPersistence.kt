package com.quranengine.data.batchdownloader

interface DownloadsPersistence {
    suspend fun retrieveAll(): List<DownloadBatch>
    suspend fun insert(batch: DownloadBatchRequest): DownloadBatch
    suspend fun update(downloads: List<Download>)
    suspend fun delete(batchIds: List<Long>)
}
