package com.quranengine.domain.readingservice

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.fileExists
import com.quranengine.core.system.removeItem
import com.quranengine.data.batchdownloader.DownloadBatchRequest
import com.quranengine.data.batchdownloader.DownloadBatchResponse
import com.quranengine.data.batchdownloader.DownloadManager
import com.quranengine.data.batchdownloader.DownloadRequest
import com.quranengine.model.qurankit.Reading
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * Manages downloading reading resources.
 *
 * Port of the Swift `ReadingResourcesService` actor.
 */
class ReadingResourcesService(
    private val fileSystem: FileSystem,
    private val downloader: DownloadManager,
    private val remoteResources: ReadingRemoteResources?,
    private val readingPreferences: ReadingPreferences,
    private val baseDir: File,
) {
    sealed class ResourceStatus {
        data class Downloading(val progress: Double) : ResourceStatus()
        data object Ready : ResourceStatus()
        data class Error(val error: Throwable) : ResourceStatus()
    }

    private val _status = MutableStateFlow<ResourceStatus?>(null)
    val status: StateFlow<ResourceStatus?> = _status.asStateFlow()

    private var readingJob: Job? = null

    /**
     * Call from a [CoroutineScope] to begin observing the selected reading preference and
     * downloading resources as needed.
     */
    suspend fun startLoadingResources() = coroutineScope {
        readingPreferences.readingFlow.collectLatest { reading ->
            readingJob?.cancel()
            readingJob = launch { loadResource(reading) }
        }
    }

    /** Retry downloading resources for the currently selected reading. */
    suspend fun retry() {
        loadResource(readingPreferences.reading)
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private suspend fun loadResource(reading: Reading) {
        removePreviouslyDownloadedResources(exclude = reading)
        cancelDownloads(exclude = reading)

        Timber.i("Resources: Start loading reading resources of: %s", reading)
        val remoteResource = remoteResources?.resource(reading)
        if (remoteResource == null) {
            Timber.i("Resources: Reading %s is bundled with the app.", reading)
            send(ResourceStatus.Ready, reading)
            return
        }

        if (fileSystem.fileExists(remoteResource.successFilePath, baseDir)) {
            Timber.i("Resources: Reading %s has been downloaded and saved locally before", reading)
            send(ResourceStatus.Ready, reading)
            return
        }

        removeDownloadedResource(reading)

        try {
            download(reading, remoteResource)
            send(ResourceStatus.Ready, reading)
        } catch (e: Exception) {
            Timber.e(e, "Failed to download %s", reading)
            send(ResourceStatus.Error(e), reading)
        }
    }

    private suspend fun download(reading: Reading, remoteResource: RemoteResource) {
        val runningReadings = runningReadingDownloads()
        val existing = runningReadings.firstOrNull { remoteResource.matches(it) }

        val download: DownloadBatchResponse = if (existing != null) {
            existing
        } else {
            val request = DownloadRequest(
                url = remoteResource.url,
                destination = remoteResource.zipFile,
            )
            downloader.download(DownloadBatchRequest(requests = listOf(request)))
        }

        download.progressFlow.collect { progress ->
            send(ResourceStatus.Downloading(progress.progress), reading)
        }

        download.awaitCompletion()
        val error = download.getError()
        if (error != null) throw error
    }

    private suspend fun cancelDownloads(exclude: Reading) {
        val excludedResource = remoteResources?.resource(exclude)
        val running = runningReadingDownloads()
        val toCancel = running.filter { batch ->
            excludedResource == null || !excludedResource.matches(batch)
        }
        if (toCancel.isNotEmpty()) {
            downloader.cancel(toCancel)
        }
    }

    private suspend fun runningReadingDownloads(): List<DownloadBatchResponse> {
        return downloader.getOnGoingDownloads().filter { isReadingDownload(it) }
    }

    private fun isReadingDownload(response: DownloadBatchResponse): Boolean {
        return response.requests.size == 1 &&
            response.requests.any { Reading.isDownloadDestinationPath(it.destination) }
    }

    private fun removePreviouslyDownloadedResources(exclude: Reading) {
        Reading.sortedReadings.filter { it != exclude }.forEach { removeDownloadedResource(it) }
    }

    private fun removeDownloadedResource(reading: Reading) {
        val resource = remoteResources?.resource(reading) ?: return
        try {
            fileSystem.removeItem(resource.downloadDestination, baseDir)
        } catch (_: Exception) {
            // Ignore – the directory may not exist.
        }
    }

    private fun send(status: ResourceStatus, reading: Reading) {
        if (readingPreferences.reading != reading) return
        _status.value = status
    }
}
