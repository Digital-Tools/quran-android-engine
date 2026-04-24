package com.quranengine.domain.translationservice

import com.quranengine.core.utilities.features.RelativeFilePath
import com.quranengine.data.batchdownloader.DownloadBatchRequest
import com.quranengine.data.batchdownloader.DownloadBatchResponse
import com.quranengine.data.batchdownloader.DownloadManager
import com.quranengine.data.batchdownloader.DownloadRequest
import com.quranengine.model.qurantext.Translation

class TranslationsDownloader(
    private val downloader: DownloadManager,
) {
    suspend fun download(translation: Translation): DownloadBatchResponse {
        val request = DownloadRequest(
            url = translation.fileURL,
            destination = RelativeFilePath(translation.unprocessedLocalPath),
        )
        return downloader.download(DownloadBatchRequest(requests = listOf(request)))
    }

    suspend fun runningTranslationDownloads(): List<DownloadBatchResponse> {
        return downloader.getOnGoingDownloads().filter { it.isTranslation }
    }
}

fun Set<DownloadBatchResponse>.firstMatches(translation: Translation): DownloadBatchResponse? {
    for (batch in this) {
        val request = batch.requests.firstOrNull() ?: continue
        if (translation.matches(request)) return batch
    }
    return null
}

fun List<Translation>.firstMatches(batch: DownloadBatchResponse): Translation? {
    val request = batch.requests.firstOrNull() ?: return null
    return firstOrNull { it.matches(request) }
}

private fun Translation.matches(request: DownloadRequest): Boolean =
    request.destination.path == unprocessedLocalPath
