package com.quranengine.data.batchdownloader

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.createDirectory
import com.quranengine.core.system.moveItem
import com.quranengine.core.system.removeItem
import com.quranengine.core.utilities.features.RelativeFilePath
import com.quranengine.data.network.NetworkError
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

internal class DownloadCallbacks(
    private val dataController: DownloadBatchDataController,
    private val fileSystem: FileSystem,
    private val baseDir: File,
) {
    suspend fun onProgress(taskId: Int, bytesWritten: Long, totalBytes: Long) {
        val response = dataController.downloadRequestResponse(forTaskId = taskId) ?: run {
            Timber.w("onProgress: Cannot find download for taskId $taskId")
            return
        }
        val progress = DownloadProgress(
            total = totalBytes.toDouble(),
            completed = bytesWritten.toDouble(),
        )
        response.response.updateProgress(response.request, progress)
    }

    suspend fun onDownloadComplete(taskId: Int, tempFile: File) {
        val response = dataController.downloadRequestResponse(forTaskId = taskId) ?: run {
            Timber.w("onDownloadComplete: Cannot find download for taskId $taskId")
            return
        }

        val destinationPath = response.request.destination
        val resumePath = response.request.resumePath

        try {
            fileSystem.removeItem(resumePath, baseDir)
        } catch (_: Exception) {
            // Ignore – resume data may not exist
        }

        try {
            fileSystem.removeItem(destinationPath, baseDir)
        } catch (_: Exception) {
            // Ignore – destination may not exist yet
        }

        try {
            val directory = destinationPath.deletingLastPathComponent()
            fileSystem.createDirectory(directory, baseDir, withIntermediateDirectories = true)
            fileSystem.moveItem(tempFile, destinationPath, baseDir)
        } catch (e: Exception) {
            Timber.e(e, "Problem copying item to destination '$destinationPath'")
            dataController.downloadFailed(response, FileSystemError.from(e))
            return
        }

        dataController.downloadCompleted(response)
    }

    suspend fun onDownloadFailed(taskId: Int, error: Throwable) {
        Timber.d("Download failed taskId=$taskId error=$error")
        val response = dataController.downloadRequestResponse(forTaskId = taskId) ?: run {
            if (error !is CancellationException) {
                Timber.w("onDownloadFailed: Cannot find download for taskId $taskId")
            }
            return
        }

        val finalError = wrapError(error)
        dataController.downloadFailed(response, finalError)
    }

    private fun wrapError(error: Throwable): Throwable {
        if (error is CancellationException) return error

        Timber.e(error, "Download network error occurred")
        return when {
            error is IOException && error.message?.contains("No space left") == true ->
                FileSystemError.NoDiskSpace
            else -> NetworkError.from(error)
        }
    }
}
