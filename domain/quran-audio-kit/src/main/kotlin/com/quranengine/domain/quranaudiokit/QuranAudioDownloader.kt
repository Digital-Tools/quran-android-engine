package com.quranengine.domain.quranaudiokit

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.fileExists
import com.quranengine.core.utilities.features.RelativeFilePath
import com.quranengine.data.batchdownloader.DownloadBatchRequest
import com.quranengine.data.batchdownloader.DownloadBatchResponse
import com.quranengine.data.batchdownloader.DownloadManager
import com.quranengine.data.batchdownloader.DownloadRequest
import com.quranengine.domain.reciterservice.audioFiles
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.qurankit.AyahNumber
import java.io.File

class QuranAudioDownloader(
    private val baseURL: String,
    private val downloader: DownloadManager,
    private val fileSystem: FileSystem,
    private val baseDir: File,
) {

    suspend fun downloaded(reciter: Reciter, from: AyahNumber, to: AyahNumber): Boolean {
        val files = filesForReciter(reciter, from, to)
        return files.all { fileSystem.fileExists(it.destination, baseDir) }
    }

    suspend fun download(from: AyahNumber, to: AyahNumber, reciter: Reciter): DownloadBatchResponse {
        val files = reciter
            .audioFiles(baseURL, from, to)
            .filter { !File(baseDir, it.local).exists() }
            .map { DownloadRequest(url = it.remote, destination = RelativeFilePath(it.local)) }
        val request = DownloadBatchRequest(requests = files)
        return downloader.download(request)
    }

    suspend fun cancelAllAudioDownloads() {
        val downloads = runningAudioDownloads()
        downloader.cancel(downloads)
    }

    suspend fun runningAudioDownloads(): List<DownloadBatchResponse> {
        val batches = downloader.getOnGoingDownloads()
        return batches.filter { it.isAudio }
    }

    private fun filesForReciter(reciter: Reciter, from: AyahNumber, to: AyahNumber): List<DownloadRequest> =
        reciter.audioFiles(baseURL, from, to)
            .map { DownloadRequest(url = it.remote, destination = RelativeFilePath(it.local)) }
}
