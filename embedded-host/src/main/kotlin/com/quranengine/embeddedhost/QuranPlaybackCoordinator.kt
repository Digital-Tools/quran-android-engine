package com.quranengine.embeddedhost

import com.quranengine.core.audioplayer.Runs
import com.quranengine.data.annotation.persistence.LastPagePersistence
import com.quranengine.domain.quranaudiokit.AudioPreferences
import com.quranengine.domain.quranaudiokit.PreferencesLastAyahFinder
import com.quranengine.domain.quranaudiokit.QuranAudioDownloader
import com.quranengine.domain.quranaudiokit.QuranAudioPlayerActions
import com.quranengine.domain.quranaudiokit.QuranAudioPlayerStore
import com.quranengine.domain.reciterservice.ReciterDataRetriever
import com.quranengine.domain.reciterservice.ReciterPreferences
import com.quranengine.model.qurankit.Quran
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Headless Quran audio playback for Mizan Flutter dashboard.
 * Uses the shared [QuranAudioPlayerStore] so dashboard controls stay in sync
 * with the in-app audio banner and mushaf ayah highlight.
 */
@Singleton
class QuranPlaybackCoordinator @Inject constructor(
    private val store: QuranAudioPlayerStore,
    private val audioDownloader: QuranAudioDownloader,
    private val reciterDataRetriever: ReciterDataRetriever,
    private val reciterPreferences: ReciterPreferences,
    private val audioPreferences: AudioPreferences,
    private val lastAyahFinder: PreferencesLastAyahFinder,
    private val lastPagePersistence: LastPagePersistence,
    private val quran: Quran,
) {
    private val coordinatorKey = Any()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var playbackJob: Job? = null
    private var onStateChanged: ((Boolean) -> Unit)? = null

    init {
        store.register(
            coordinatorKey,
            QuranAudioPlayerActions(
                playbackEnded = { notifyState() },
                playbackPaused = { notifyState() },
                playbackResumed = { notifyState() },
                playing = { _, _ -> notifyState() },
            ),
        )
        scope.launch {
            store.isPlaying.collect { playing ->
                onStateChanged?.invoke(playing)
            }
        }
    }

    val isPlaying: Boolean
        get() = store.isPlaying.value

    fun setStateHandler(handler: (Boolean) -> Unit) {
        onStateChanged = handler
        handler(store.isPlaying.value)
    }

    fun togglePlayback(fallbackPageNumber: Int?) {
        if (store.isPlaying.value) {
            store.player.pauseAudio()
            return
        }
        if (store.isPaused.value) {
            store.player.resumeAudio()
            return
        }
        playFromLastPage(fallbackPageNumber)
    }

    private fun playFromLastPage(fallbackPageNumber: Int?) {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            startPlayback(fallbackPageNumber)
        }
    }

    private suspend fun startPlayback(fallbackPageNumber: Int?) {
        val pageNumber = resolvePageNumber(fallbackPageNumber)
        val page = quran.pages.getOrNull(pageNumber - 1) ?: run {
            Timber.w("Mizan: invalid Quran page %s", pageNumber)
            return
        }

        val startAyah = page.firstVerse
        val endAyah = lastAyahFinder.findLastAyah(startAyah)
        val reciters = reciterDataRetriever.getReciters()
        val reciter = reciters.firstOrNull { it.id == reciterPreferences.lastSelectedReciterId }
            ?: reciters.firstOrNull()
            ?: run {
                Timber.w("Mizan: no reciter available for Quran playback")
                return
            }

        store.player.stopAudio()

        try {
            val downloaded = audioDownloader.downloaded(reciter, startAyah, endAyah)
            if (!downloaded) {
                val response = audioDownloader.download(startAyah, endAyah, reciter)
                response.awaitCompletion()
                response.getError()?.let { error ->
                    Timber.e(error, "Mizan: Quran audio download failed")
                    notifyState()
                    return
                }
            }

            store.player.play(
                reciter = reciter,
                rate = audioPreferences.playbackRate,
                from = startAyah,
                to = endAyah,
                verseRuns = Runs.ONE,
                listRuns = Runs.ONE,
            )
            notifyState()
        } catch (e: Exception) {
            Timber.e(e, "Mizan: Quran playback failed")
            notifyState()
        }
    }

    private suspend fun resolvePageNumber(fallback: Int?): Int {
        if (fallback != null && fallback > 0) return fallback
        return lastPagePersistence.retrieveAll()
            .maxByOrNull { it.modifiedOn }
            ?.page
            ?: 1
    }

    private fun notifyState() {
        onStateChanged?.invoke(store.isPlaying.value)
    }
}
