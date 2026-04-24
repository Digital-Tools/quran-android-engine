package com.quranengine.data.batchdownloader.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DownloadBatchEntity::class, DownloadEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class DownloadsDatabase : RoomDatabase() {
    abstract fun downloadsDao(): DownloadsDao
}
