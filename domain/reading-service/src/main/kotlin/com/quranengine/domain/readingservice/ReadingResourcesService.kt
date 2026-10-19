package com.quranengine.domain.readingservice

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.Zipper
import com.quranengine.core.system.fileExists
import com.quranengine.core.system.removeItem
import com.quranengine.core.utilities.features.attempt
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages downloading reading resources.
 *
 * Port of the Swift `ReadingResourcesService` actor.
 */
class ReadingResourcesService(
    private val fileSystem: FileSystem,
    private val zipper: Zipper,
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
    private val started = AtomicBoolean(false)

    /**
     * Call from a [CoroutineScope] to begin observing the selected reading preference and
     * downloading resources as needed.
     */
    suspend fun startLoadingResources() = coroutineScope {
        // Started once per process: by the host app at launch (so the pages
        // download before the reader is first opened) and again by the
        // reader. A second loader would delete the first one's download.
        if (!started.compareAndSet(false, true)) return@coroutineScope
        // A reading chosen before it was marked unavailable (e.g. one on the
        // placeholder host) can never load; fall back to one that can.
        if (!ReadingAvailability.isAvailable(readingPreferences.reading, remoteResources)) {
            readingPreferences.reading = ReadingAvailability.firstAvailable(remoteResources)
        }
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

        clearForDownload(remoteResource)

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

        // progressFlow never completes: report it beside the wait, then stop.
        coroutineScope {
            val progress = launch {
                download.progressFlow.collect { progress ->
                    send(ResourceStatus.Downloading(progress.progress), reading)
                }
            }
            try {
                download.awaitCompletion()
            } finally {
                progress.cancel()
            }
        }
        val error = download.getError()
        if (error != null) throw error

        unzip(remoteResource)
    }

    private fun unzip(remoteResource: RemoteResource) {
        val zipFile = remoteResource.zipFile.file(baseDir)
        try {
            attempt(times = 3) {
                zipper.unzipFile(
                    zipFile = zipFile,
                    destination = remoteResource.extractDestination.file(baseDir),
                    overwrite = true,
                )
            }
        } finally {
            // Delete the zip either way — success: to save space, failure: to
            // force a fresh re-download instead of retrying a corrupt file.
            try {
                fileSystem.removeItem(zipFile)
            } catch (e: Exception) {
                Timber.w(e, "Failed to remove reading zip file: $zipFile")
            }
        }
        fileSystem.writeToFile(remoteResource.successFilePath.file(baseDir), "Downloaded")
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

    /**
     * Waits until [reading]'s pages are on the phone (they may still be
     * downloading); throws the download's error if it failed.
     */
    suspend fun awaitReady(reading: Reading) {
        val resource = remoteResources?.resource(reading) ?: return
        if (fileSystem.fileExists(resource.successFilePath, baseDir)) return
        when (val result = status.first { it is ResourceStatus.Ready || it is ResourceStatus.Error }) {
            is ResourceStatus.Error -> throw result.error
            else -> Unit
        }
    }

    /** Removes a half-unzipped reading, keeping a part-downloaded zip to resume. */
    private fun clearForDownload(resource: RemoteResource) {
        val directory = resource.downloadDestination.file(baseDir)
        directory.listFiles()?.forEach { child ->
            if (!child.name.endsWith(".part")) child.deleteRecursively()
        }
    }

    private fun send(status: ResourceStatus, reading: Reading) {
        if (readingPreferences.reading != reading) return
        _status.value = status
    }
}
