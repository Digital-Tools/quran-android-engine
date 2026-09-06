package com.quranengine.features.audiodownloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.core.localization.Localizer
import com.quranengine.data.batchdownloader.DownloadsObserver
import com.quranengine.domain.quranaudiokit.QuranAudioDownloader
import com.quranengine.domain.quranaudiokit.firstMatches
import com.quranengine.domain.readingservice.ReadingPreferences
import com.quranengine.domain.reciterservice.ReciterAudioDeleter
import com.quranengine.domain.reciterservice.ReciterDataRetriever
import com.quranengine.domain.reciterservice.ReciterSizeInfoRetriever
import com.quranengine.domain.reciterservice.localizedName
import com.quranengine.model.quranaudio.AudioDownloadedSize
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.qurankit.Quran
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

data class AudioDownloadsUiState(
    val downloaded: List<AudioDownloadItem> = emptyList(),
    val available: List<AudioDownloadItem> = emptyList(),
)

@HiltViewModel
class AudioDownloadsViewModel @Inject constructor(
    private val reciterDataRetriever: ReciterDataRetriever,
    private val sizeInfoRetriever: ReciterSizeInfoRetriever,
    private val audioDownloader: QuranAudioDownloader,
    private val deleter: ReciterAudioDeleter,
    private val readingPreferences: ReadingPreferences,
    private val localizer: Localizer,
) : ViewModel() {

    private val reciters = MutableStateFlow<List<Reciter>>(emptyList())
    private val sizes = MutableStateFlow<Map<Int, AudioDownloadedSize>>(emptyMap())
    private val downloadsObserver = DownloadsObserver<Int>(
        extractKey = { batch -> reciters.value.firstMatches(batch)?.id },
        showError = { Timber.e(it, "Audio download error") },
    )

    val uiState: StateFlow<AudioDownloadsUiState> = combine(
        reciters,
        sizes,
        downloadsObserver.progressFlow,
    ) { all, sizeMap, progress ->
        val items = all.map { reciter ->
            AudioDownloadItem(
                reciter = reciter,
                displayName = reciter.localizedName(localizer),
                size = sizeMap[reciter.id],
                progress = progress[reciter.id],
            )
        }.sortedWith(
            compareByDescending<AudioDownloadItem> { it.size?.downloadedSizeInBytes ?: 0L }
                .thenBy { it.displayName },
        )
        AudioDownloadsUiState(
            downloaded = items.filter { it.canDelete },
            available = items.filter { !it.canDelete },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AudioDownloadsUiState())

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch { observeRunningDownloads() }
        viewModelScope.launch {
            var previousKeys = emptySet<Int>()
            downloadsObserver.progressFlow.collect { progress ->
                val finished = previousKeys - progress.keys
                for (id in finished) reloadSize(id)
                previousKeys = progress.keys
            }
        }
    }

    fun download(reciter: Reciter) {
        viewModelScope.launch {
            try {
                val quran = currentQuran()
                val response = audioDownloader.download(quran.firstVerse, quran.lastVerse, reciter)
                downloadsObserver.observe(setOf(response))
                response.awaitCompletion()
                reloadSize(reciter.id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to download audio for reciter ${reciter.id}")
            }
        }
    }

    fun cancel(reciter: Reciter) {
        viewModelScope.launch {
            try {
                downloadsObserver.runningDownloads.firstMatches(reciter)?.cancel()
            } catch (e: Exception) {
                Timber.e(e, "Failed to cancel audio download for reciter ${reciter.id}")
            }
        }
    }

    fun delete(reciter: Reciter) {
        viewModelScope.launch {
            try {
                downloadsObserver.runningDownloads.firstMatches(reciter)?.cancel()
                deleter.deleteAudioFiles(reciter)
                reloadSize(reciter.id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete audio for reciter ${reciter.id}")
            }
        }
    }

    private suspend fun load() {
        val all = reciterDataRetriever.getReciters()
        reciters.value = all
        sizes.value = sizeInfoRetriever.getDownloadedSizes(all, currentQuran())
            .mapKeys { it.key.id }
    }

    private suspend fun observeRunningDownloads() {
        try {
            val running = audioDownloader.runningAudioDownloads()
            if (running.isNotEmpty()) downloadsObserver.observe(running.toSet())
        } catch (e: Exception) {
            Timber.e(e, "Failed to observe running audio downloads")
        }
    }

    private suspend fun reloadSize(reciterId: Int) {
        val reciter = reciters.value.firstOrNull { it.id == reciterId } ?: return
        val size = sizeInfoRetriever.getDownloadedSize(reciter, currentQuran())
        sizes.value = sizes.value + (reciterId to size)
    }

    private fun currentQuran(): Quran = readingPreferences.reading.quran
}
