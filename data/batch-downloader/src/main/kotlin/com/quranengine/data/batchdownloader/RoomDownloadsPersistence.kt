package com.quranengine.data.batchdownloader

import com.quranengine.core.utilities.features.RelativeFilePath
import com.quranengine.data.batchdownloader.db.DownloadBatchEntity
import com.quranengine.data.batchdownloader.db.DownloadEntity
import com.quranengine.data.batchdownloader.db.DownloadsDao

internal class RoomDownloadsPersistence(
    private val dao: DownloadsDao,
) : DownloadsPersistence {

    override suspend fun retrieveAll(): List<DownloadBatch> {
        val downloads = dao.getAllDownloads()
        return downloads.groupBy { it.batchId }.map { (batchId, entities) ->
            DownloadBatch(
                id = batchId,
                downloads = entities.map { it.toDownload() },
            )
        }
    }

    override suspend fun insert(batch: DownloadBatchRequest): DownloadBatch {
        val batchId = dao.insertBatch(DownloadBatchEntity())
        val entities = batch.requests.map { request ->
            DownloadEntity(
                batchId = batchId,
                url = request.url,
                destination = request.destination.path,
                status = Download.Status.DOWNLOADING.value,
                taskId = null,
            )
        }
        dao.insertDownloads(entities)
        val downloads = batch.requests.map { request ->
            Download(request = request, batchId = batchId)
        }
        return DownloadBatch(id = batchId, downloads = downloads)
    }

    override suspend fun update(downloads: List<Download>) {
        for (download in downloads) {
            dao.updateByUrl(
                url = download.request.url,
                status = download.status.value,
                taskId = download.taskId,
            )
        }
    }

    override suspend fun delete(batchIds: List<Long>) {
        dao.deleteBatches(batchIds)
    }

    private fun DownloadEntity.toDownload(): Download = Download(
        taskId = taskId,
        request = DownloadRequest(
            url = url,
            destination = RelativeFilePath(destination, isDirectory = false),
        ),
        status = Download.Status.fromValue(status),
        batchId = batchId,
    )
}
