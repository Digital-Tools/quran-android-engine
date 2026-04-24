package com.quranengine.data.batchdownloader.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "downloads",
    foreignKeys = [ForeignKey(
        entity = DownloadBatchEntity::class,
        parentColumns = ["id"],
        childColumns = ["batchId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("batchId"), Index("url")],
)
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long,
    val url: String,
    val destination: String,
    val status: Int,
    val taskId: Int?,
)

@Entity(tableName = "download_batches")
data class DownloadBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)
