package com.quranengine.domain.quranaudiokit

import com.quranengine.data.batchdownloader.DownloadBatchResponse
import com.quranengine.data.batchdownloader.DownloadRequest
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.audioFilesPath
import com.quranengine.model.quranaudio.localFolder

val DownloadRequest.isAudio: Boolean
    get() = Reciter.audioFilesPath.let { prefix ->
        destination.path.startsWith("$prefix/")
    }

val DownloadBatchResponse.isAudio: Boolean
    get() = requests.any { it.isAudio }

fun Set<DownloadBatchResponse>.firstMatches(reciter: Reciter): DownloadBatchResponse? {
    for (batch in this) {
        val download = batch.requests.firstOrNull() ?: continue
        if (reciter.matches(download)) return batch
    }
    return null
}

fun List<Reciter>.firstMatches(batch: DownloadBatchResponse): Reciter? {
    val download = batch.requests.firstOrNull() ?: return null
    return firstOrNull { it.matches(download) }
}

private fun Reciter.matches(request: DownloadRequest): Boolean =
    localFolder() == request.destination.deletingLastPathComponent().path
