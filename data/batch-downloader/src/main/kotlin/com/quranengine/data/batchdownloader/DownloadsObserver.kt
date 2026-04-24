package com.quranengine.data.batchdownloader

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

import java.util.Collections

class DownloadsObserver<Key : Any>(
    private val extractKey: (DownloadBatchResponse) -> Key?,
    private val showError: (Throwable) -> Unit,
) {
    private val _runningDownloads: MutableSet<DownloadBatchResponse> =
        Collections.synchronizedSet(mutableSetOf())
    val runningDownloads: Set<DownloadBatchResponse> get() = synchronized(_runningDownloads) { _runningDownloads.toSet() }

    private val _progressFlow = MutableStateFlow<Map<Key, Double>>(emptyMap())
    val progressFlow: StateFlow<Map<Key, Double>> = _progressFlow.asStateFlow()

    private val observerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun observe(downloads: Set<DownloadBatchResponse>) {
        _runningDownloads.addAll(downloads)
        for (download in downloads) {
            observerScope.launch {
                try {
                    download.progressFlow.collect { progress ->
                        progressUpdated(download, progress.progress)
                    }
                } catch (e: Exception) {
                    if (e !is CancellationException) showError(e)
                }
                finish(download)
            }
        }
    }

    private fun finish(download: DownloadBatchResponse) {
        _runningDownloads.remove(download)
        val key = extractKey(download) ?: return
        _progressFlow.update { it - key }
    }

    private fun progressUpdated(batch: DownloadBatchResponse, newProgress: Double) {
        if (batch !in _runningDownloads) return
        val key = extractKey(batch) ?: run {
            Timber.d("Cannot find key for download $batch")
            return
        }
        _progressFlow.update { it + (key to newProgress) }
    }
}
