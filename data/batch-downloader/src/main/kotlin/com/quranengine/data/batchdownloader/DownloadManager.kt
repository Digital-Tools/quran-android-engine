package com.quranengine.data.batchdownloader

import com.quranengine.core.system.FileSystem
import com.quranengine.data.batchdownloader.db.DownloadsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class DownloadManager(
    private val maxSimultaneousDownloads: Int,
    private val okHttpClient: OkHttpClient,
    private val persistence: DownloadsPersistence,
    private val fileSystem: FileSystem,
    private val baseDir: File,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) {
    private val dataController = DownloadBatchDataController(maxSimultaneousDownloads, persistence)
    private val callbacks = DownloadCallbacks(dataController, fileSystem, baseDir)
    private val taskIdCounter = AtomicInteger(0)

    constructor(
        maxSimultaneousDownloads: Int,
        okHttpClient: OkHttpClient,
        database: DownloadsDatabase,
        fileSystem: FileSystem,
        baseDir: File,
    ) : this(
        maxSimultaneousDownloads = maxSimultaneousDownloads,
        okHttpClient = okHttpClient,
        persistence = RoomDownloadsPersistence(database.downloadsDao()),
        fileSystem = fileSystem,
        baseDir = baseDir,
    )

    suspend fun start() {
        Timber.i("Starting download manager")
        dataController.start { request -> startOkHttpDownload(request) }
        Timber.i("Download manager started")
    }

    suspend fun getOnGoingDownloads(): List<DownloadBatchResponse> {
        Timber.i("getOnGoingDownloads requested")
        val downloads = dataController.getOnGoingDownloads()
        Timber.d("Found ${downloads.size} ongoing downloads")
        return downloads
    }

    suspend fun download(batch: DownloadBatchRequest): DownloadBatchResponse {
        Timber.d("Requested to download ${batch.requests.map { it.url }}")
        return dataController.download(batch)
    }

    suspend fun cancel(downloads: List<DownloadBatchResponse>) {
        if (downloads.isEmpty()) return
        coroutineScope {
            for (download in downloads) {
                launch {
                    download.cancel()
                    download.awaitCompletion()
                }
            }
        }
        val batchIds = downloads.map { it.batchId }.toSet()
        dataController.waitUntilBatchesRemoved(batchIds)
    }

    private fun startOkHttpDownload(request: DownloadRequest): Pair<Int, Call> {
        val taskId = taskIdCounter.incrementAndGet()
        val httpRequest = Request.Builder()
            .url(request.url)
            .build()

        val call = okHttpClient.newCall(httpRequest)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                scope.launch { callbacks.onDownloadFailed(taskId, e) }
            }

            override fun onResponse(call: Call, response: Response) {
                scope.launch {
                    response.use { resp ->
                        if (!resp.isSuccessful) {
                            callbacks.onDownloadFailed(
                                taskId,
                                IOException("Unacceptable status code: ${resp.code}"),
                            )
                            return@launch
                        }

                        val tempFile = File(baseDir, ".download_tmp_$taskId")
                        try {
                            val body = resp.body ?: throw IOException("Empty response body")
                            val totalBytes = body.contentLength()
                            var bytesWritten = 0L

                            body.byteStream().use { input ->
                                tempFile.outputStream().use { output ->
                                    val buffer = ByteArray(8192)
                                    var read: Int
                                    while (input.read(buffer).also { read = it } != -1) {
                                        output.write(buffer, 0, read)
                                        bytesWritten += read
                                        callbacks.onProgress(taskId, bytesWritten, totalBytes)
                                    }
                                }
                            }

                            callbacks.onDownloadComplete(taskId, tempFile)
                        } catch (e: Exception) {
                            tempFile.delete()
                            callbacks.onDownloadFailed(taskId, e)
                        }
                    }
                }
            }
        })

        return taskId to call
    }
}
