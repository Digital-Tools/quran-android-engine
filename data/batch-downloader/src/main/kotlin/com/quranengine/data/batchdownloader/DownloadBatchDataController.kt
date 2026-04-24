package com.quranengine.data.batchdownloader

import com.quranengine.core.utilities.features.attemptSuspend
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Call
import timber.log.Timber

internal class DownloadBatchDataController(
    private val maxSimultaneousDownloads: Int,
    private val persistence: DownloadsPersistence,
) {
    private val mutex = Mutex()
    private val batches = mutableSetOf<DownloadBatchResponse>()
    private val initialized = CompletableDeferred<Unit>()
    private var downloadStarter: ((DownloadRequest) -> Pair<Int, Call>)? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun start(downloadStarter: (DownloadRequest) -> Pair<Int, Call>) {
        bootstrapPersistence()
        mutex.withLock {
            this.downloadStarter = downloadStarter
        }
        initialized.complete(Unit)
        startPendingTasksIfNeeded()
    }

    suspend fun getOnGoingDownloads(): List<DownloadBatchResponse> {
        initialized.await()
        return mutex.withLock { batches.toList() }
    }

    internal data class SingleTaskResponse(
        val request: DownloadRequest,
        val response: DownloadBatchResponse,
    )

    suspend fun downloadRequestResponse(forTaskId: Int): SingleTaskResponse? = mutex.withLock {
        for (batch in batches) {
            val request = batch.downloadRequest(forTaskId = forTaskId)
            if (request != null) {
                return@withLock SingleTaskResponse(request, batch)
            }
        }
        null
    }

    suspend fun download(batchRequest: DownloadBatchRequest): DownloadBatchResponse {
        Timber.i("Batching ${batchRequest.requests.size} to download.")
        val batch = persistence.insert(batch = batchRequest)
        Timber.i("Batch assigned Id = ${batch.id}.")
        val response = createResponse(batch)
        startPendingTasksIfNeeded()
        return response
    }

    suspend fun downloadCompleted(response: SingleTaskResponse) {
        response.response.complete(response.request, Result.success(Unit))
        updateDownloadPersistence(response)
        startPendingTasksIfNeeded()
    }

    suspend fun downloadFailed(response: SingleTaskResponse, error: Throwable) {
        response.response.complete(response.request, Result.failure(error))
        startPendingTasksIfNeeded()
    }

    suspend fun waitUntilBatchesRemoved(batchIds: Set<Long>) {
        if (batchIds.isEmpty()) return
        while (mutex.withLock { batches.any { it.batchId in batchIds } }) {
            delay(50)
        }
    }

    private suspend fun bootstrapPersistence() {
        try {
            attemptSuspend(times = 3) {
                loadBatchesFromPersistence()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve initial download batches from persistence.")
        }
    }

    private suspend fun loadBatchesFromPersistence() {
        val batches = persistence.retrieveAll()
        Timber.i("Loading ${batches.size} from persistence")
        for (batch in batches) {
            createResponse(batch)
        }
    }

    private suspend fun createResponse(batch: DownloadBatch): DownloadBatchResponse {
        val response = DownloadBatchResponse(batch)
        mutex.withLock { batches.add(response) }

        scope.launch {
            response.awaitCompletion()
            cleanUpForCompletedBatch(response)
        }

        return response
    }

    private suspend fun cleanUpForCompletedBatch(response: DownloadBatchResponse) {
        mutex.withLock { batches.remove(response) }
        try {
            persistence.delete(batchIds = listOf(response.batchId))
        } catch (e: Exception) {
            Timber.e(e, "DownloadPersistence.DeleteBatch")
        }
        startPendingTasksIfNeeded()
    }

    private suspend fun startPendingTasksIfNeeded() {
        if (!initialized.isCompleted) {
            Timber.w("startPendingTasksIfNeeded not initialized")
            return
        }

        val starter = mutex.withLock { downloadStarter } ?: run {
            Timber.w("startPendingTasksIfNeeded no download starter")
            return
        }

        val (runningTasks, batchesCopy) = mutex.withLock {
            val batchList = batches.sortedBy { it.batchId }.toList()
            0 to batchList // placeholder, compute outside lock
        }
        val totalRunning = batchesCopy.sumOf { it.runningTasks() }

        if (totalRunning >= maxSimultaneousDownloads) return
        if (batchesCopy.isEmpty()) return

        var remaining = maxSimultaneousDownloads - totalRunning
        val tasksToStart = mutableListOf<Call>()

        for (batch in batchesCopy) {
            while (remaining > 0) {
                val (request, call) = batch.startDownloadIfNeeded(starter) ?: break
                val singleResponse = SingleTaskResponse(request, batch)
                updateDownloadPersistence(singleResponse)
                tasksToStart.add(call)
                remaining--
            }
        }

        Timber.i("startDownloadTasks ${tasksToStart.size} to download on empty channels.")
    }

    private suspend fun updateDownloadPersistence(response: SingleTaskResponse) {
        try {
            persistence.update(listOf(response.response.download(response.request)))
        } catch (e: Exception) {
            Timber.e(e, "DownloadPersistence.UpdateDownload")
        }
    }
}
