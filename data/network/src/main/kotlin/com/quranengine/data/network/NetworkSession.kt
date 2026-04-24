package com.quranengine.data.network

import java.io.File
import java.net.URL

/**
 * Abstraction over a download session.
 * On Android this will be backed by OkHttp or Android DownloadManager.
 */
interface DownloadSession {
    suspend fun download(url: URL, destination: File)
    fun cancel(taskId: Int)
}

interface DownloadSessionDelegate {
    suspend fun onProgress(taskId: Int, bytesWritten: Long, totalBytes: Long, expectedTotalBytes: Long)
    suspend fun onComplete(taskId: Int, location: File)
    suspend fun onError(taskId: Int, error: Throwable?)
    suspend fun onAllEventsFinished()
}
