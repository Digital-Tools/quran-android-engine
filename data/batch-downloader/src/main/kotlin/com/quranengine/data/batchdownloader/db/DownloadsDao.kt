package com.quranengine.data.batchdownloader.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DownloadsDao {
    @Query("SELECT * FROM downloads")
    suspend fun getAllDownloads(): List<DownloadEntity>

    @Insert
    suspend fun insertBatch(batch: DownloadBatchEntity): Long

    @Insert
    suspend fun insertDownloads(downloads: List<DownloadEntity>)

    @Query("UPDATE downloads SET status = :status, taskId = :taskId WHERE url = :url")
    suspend fun updateByUrl(url: String, status: Int, taskId: Int?)

    @Query("DELETE FROM download_batches WHERE id IN (:batchIds)")
    suspend fun deleteBatches(batchIds: List<Long>)
}
