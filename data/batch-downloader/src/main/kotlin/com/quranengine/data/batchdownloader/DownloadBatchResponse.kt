package com.quranengine.data.batchdownloader

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Call
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

class DownloadBatchResponse internal constructor(batch: DownloadBatch) {
    val batchId: Long = batch.id
    val requests: List<DownloadRequest> = batch.downloads.map { it.request }

    private val mutex = Mutex()
    private val _progressFlow = MutableSharedFlow<DownloadProgress>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val progressFlow: SharedFlow<DownloadProgress> = _progressFlow.asSharedFlow()

    private val _completion = CompletableDeferred<Unit>()

    private data class ResponseData(
        val request: DownloadRequest,
        var state: State = State.InProgress,
        var taskId: Int? = null,
        var call: Call? = null,
    ) {
        sealed class State {
            data object InProgress : State()
            data object Finished : State()
            data class Failed(val error: Throwable) : State()
        }

        val isInProgress: Boolean get() = state is State.InProgress
        val isFinished: Boolean get() = state is State.Finished
        val isFailed: Boolean get() = state is State.Failed

        fun toDownload(batchId: Long): Download = Download(
            taskId = taskId,
            request = request,
            status = if (isInProgress) Download.Status.DOWNLOADING else Download.Status.COMPLETED,
            batchId = batchId,
        )
    }

    private val responses = mutableMapOf<DownloadRequest, ResponseData>()

    init {
        _progressFlow.tryEmit(DownloadProgress(total = 1.0))
        for (download in batch.downloads) {
            val state = when (download.status) {
                Download.Status.COMPLETED -> ResponseData.State.Finished
                Download.Status.DOWNLOADING -> ResponseData.State.InProgress
            }
            responses[download.request] = ResponseData(
                request = download.request,
                state = state,
                taskId = download.taskId,
            )
        }
        for (download in batch.downloads) {
            if (download.status == Download.Status.COMPLETED) {
                completeInternal(download.request, Result.success(Unit))
            }
        }
    }

    suspend fun awaitCompletion() {
        _completion.await()
    }

    suspend fun getError(): Throwable? = mutex.withLock {
        responses.values.firstNotNullOfOrNull { (it.state as? ResponseData.State.Failed)?.error }
    }

    suspend fun runningTasks(): Int = mutex.withLock {
        responses.values.count { it.isInProgress && it.taskId != null }
    }

    suspend fun cancel() {
        mutex.withLock {
            for (request in requests) {
                cancelInternal(request)
            }
        }
    }

    internal suspend fun associateTasks(tasks: Map<Int, Call>) {
        mutex.withLock {
            for ((_, response) in responses) {
                val savedTaskId = response.taskId ?: continue
                val call = tasks[savedTaskId]
                if (call != null) {
                    response.call = call
                } else {
                    response.taskId = null
                    if (!response.isFinished) {
                        Timber.e("Couldn't find task with id $savedTaskId")
                    }
                }
            }
        }
    }

    internal suspend fun downloadRequest(forTaskId: Int): DownloadRequest? = mutex.withLock {
        responses.values.firstOrNull { it.taskId == forTaskId }?.request
    }

    internal suspend fun updateProgress(request: DownloadRequest, progress: DownloadProgress) {
        mutex.withLock {
            if (isCompleted()) return
            val response = responses[request] ?: return
            response.state = ResponseData.State.InProgress
            val accumulated = responses.values.sumOf {
                when (it.state) {
                    is ResponseData.State.Finished -> 1.0
                    is ResponseData.State.Failed -> 0.0
                    is ResponseData.State.InProgress -> {
                        if (it.request == request) progress.progress else 0.0
                    }
                }
            }
            val overallProgress = DownloadProgress(
                total = 1.0,
                completed = accumulated / responses.size,
            )
            _progressFlow.tryEmit(overallProgress)
        }
    }

    internal suspend fun complete(request: DownloadRequest, result: Result<Unit>) {
        mutex.withLock {
            completeInternal(request, result)
        }
    }

    internal suspend fun download(request: DownloadRequest): Download = mutex.withLock {
        responses[request]?.toDownload(batchId) ?: error("No response for $request")
    }

    internal suspend fun startDownloadIfNeeded(
        downloadStarter: (DownloadRequest) -> Pair<Int, Call>,
    ): Pair<DownloadRequest, Call>? = mutex.withLock {
        val notStarted = responses.values.firstOrNull {
            it.isInProgress && it.taskId == null
        } ?: return null
        val (taskId, call) = downloadStarter(notStarted.request)
        notStarted.taskId = taskId
        notStarted.call = call
        notStarted.request to call
    }

    // Must be called under lock
    private fun completeInternal(request: DownloadRequest, result: Result<Unit>) {
        val wasCompleted = isCompleted()
        val response = responses[request] ?: return
        response.state = result.fold(
            onSuccess = { ResponseData.State.Finished },
            onFailure = { ResponseData.State.Failed(it) },
        )

        if (!isCompleted() || wasCompleted) return

        val error = responses.values.firstNotNullOfOrNull {
            (it.state as? ResponseData.State.Failed)?.error
        }
        if (error != null) {
            for (req in requests) {
                cancelInternal(req)
            }
            _progressFlow.tryEmit(DownloadProgress(total = 1.0, completed = 0.0))
            _completion.complete(Unit)
        } else {
            _progressFlow.tryEmit(DownloadProgress(total = 1.0, completed = 1.0))
            _completion.complete(Unit)
        }
    }

    // Must be called under lock
    private fun cancelInternal(request: DownloadRequest) {
        val response = responses[request] ?: return
        response.call?.cancel()
        completeInternal(request, Result.failure(CancellationException("Download cancelled")))
    }

    private fun isCompleted(): Boolean {
        if (responses.values.any { it.isFailed }) return true
        return responses.values.all { it.isFinished }
    }

    override fun equals(other: Any?): Boolean =
        other is DownloadBatchResponse && batchId == other.batchId

    override fun hashCode(): Int = batchId.hashCode()
}
