package com.quranengine.features.audiobanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.core.audioplayer.Runs
import com.quranengine.core.localization.Localizer
import com.quranengine.domain.quranaudiokit.AudioPreferences
import com.quranengine.domain.quranaudiokit.QuranAudioDownloader
import com.quranengine.domain.quranaudiokit.QuranAudioPlayerActions
import com.quranengine.domain.quranaudiokit.QuranAudioPlayerStore
import com.quranengine.domain.reciterservice.ReciterDataRetriever
import com.quranengine.domain.reciterservice.ReciterPreferences
import com.quranengine.domain.reciterservice.localizedName
import com.quranengine.model.qurankit.AyahNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.quranengine.ui.audiobanner.AudioBannerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class AyahPlaybackProgress(
    val ayah: AyahNumber,
    val progress: Float?,
)

@HiltViewModel
class AudioBannerViewModel @Inject constructor(
    private val audioPlayerStore: QuranAudioPlayerStore,
    private val audioPreferences: AudioPreferences,
    private val reciterPreferences: ReciterPreferences,
    private val audioDownloader: QuranAudioDownloader,
    private val reciterDataRetriever: ReciterDataRetriever,
    private val localizer: Localizer,
) : ViewModel() {

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Stopped)

    private val _audioBannerState = MutableStateFlow(AudioBannerState())
    val audioBannerState: StateFlow<AudioBannerState> = _audioBannerState.asStateFlow()

    private val _playbackRate = MutableStateFlow(audioPreferences.playbackRate)
    val playbackRate: StateFlow<Float> = _playbackRate.asStateFlow()

    private val _currentAyah = MutableStateFlow<AyahNumber?>(null)
    val currentAyah: StateFlow<AyahNumber?> = _currentAyah.asStateFlow()

    private val _currentAyahProgress = MutableStateFlow<AyahPlaybackProgress?>(null)
    val currentAyahProgress: StateFlow<AyahPlaybackProgress?> = _currentAyahProgress.asStateFlow()

    private val _playbackRange = MutableStateFlow<Pair<AyahNumber, AyahNumber>?>(null)
    val playbackRange: StateFlow<Pair<AyahNumber, AyahNumber>?> = _playbackRange.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    private var currentReciterName: String? = null

    private val audioPlayer
        get() = audioPlayerStore.player

    init {
        audioPlayerStore.register(
            this,
            QuranAudioPlayerActions(
                playbackEnded = { onPlaybackEnded() },
                playbackPaused = { onPlaybackPaused() },
                playbackResumed = { onPlaybackResumed() },
                playing = { ayah, progress -> onPlayingAyah(ayah, progress) },
            ),
        )
        syncPlayingStateFromSharedPlayer()
        viewModelScope.launch {
            loadReciterName()
        }
    }

    fun play(
        from: AyahNumber,
        to: AyahNumber,
        verseRuns: Runs = Runs.ONE,
        listRuns: Runs = Runs.ONE,
    ) {
        _playbackRange.value = from to to
        viewModelScope.launch {
            try {
                val reciterId = reciterPreferences.lastSelectedReciterId
                val reciter = resolveReciterById(reciterId) ?: return@launch
                currentReciterName = reciter.localizedName(localizer)

                val alreadyDownloaded = audioDownloader.downloaded(reciter, from, to)
                if (!alreadyDownloaded) {
                    updatePlaybackState(PlaybackState.Downloading(0f))
                    val response = audioDownloader.download(from, to, reciter)
                    viewModelScope.launch {
                        response.progressFlow.collect { progress ->
                            updatePlaybackState(PlaybackState.Downloading(progress.progress.toFloat()))
                        }
                    }
                    response.awaitCompletion()
                    val downloadError = response.getError()
                    if (downloadError != null) {
                        _error.value = downloadError
                        updatePlaybackState(PlaybackState.Stopped)
                        return@launch
                    }
                }

                val rate = _playbackRate.value
                updatePlaybackState(PlaybackState.Playing)
                audioPlayer.play(reciter, rate, from, to, verseRuns, listRuns)
            } catch (e: Exception) {
                Timber.e(e, "Error starting playback")
                _error.value = e
                updatePlaybackState(PlaybackState.Stopped)
            }
        }
    }

    fun pause() {
        audioPlayer.pauseAudio()
        updatePlaybackState(PlaybackState.Paused)
    }

    fun resume() {
        audioPlayer.resumeAudio()
        updatePlaybackState(PlaybackState.Playing)
    }

    fun stop() {
        audioPlayer.stopAudio()
        updatePlaybackState(PlaybackState.Stopped)
    }

    fun togglePlayPause(from: AyahNumber, to: AyahNumber) {
        when (_playbackState.value) {
            is PlaybackState.Playing -> pause()
            is PlaybackState.Paused -> resume()
            is PlaybackState.Downloading -> Unit
            is PlaybackState.Stopped -> play(from, to)
        }
    }

    fun stepForward() {
        audioPlayer.stepForward()
    }

    fun stepBackward() {
        audioPlayer.stepBackward()
    }

    fun setPlaybackRate(rate: Float) {
        audioPreferences.playbackRate = rate
        _playbackRate.value = rate
        _audioBannerState.update { it.copy(playbackRate = rate) }
        audioPlayer.setRate(rate)
    }

    fun dismissError() {
        _error.value = null
    }

    // -- Player callbacks --

    private fun onPlaybackEnded() {
        updatePlaybackState(PlaybackState.Stopped)
    }

    private fun onPlaybackPaused() {
        updatePlaybackState(PlaybackState.Paused)
    }

    private fun onPlaybackResumed() {
        updatePlaybackState(PlaybackState.Playing)
    }

    private fun onPlayingAyah(ayah: AyahNumber, progress: Float?) {
        _currentAyah.value = ayah
        _currentAyahProgress.value = AyahPlaybackProgress(ayah, progress)
        _audioBannerState.update { state ->
            state.copy(
                title = "Ayah ${ayah.ayah}",
                subtitle = currentReciterName.orEmpty(),
            )
        }
    }

    // -- State mapping --

    private fun updatePlaybackState(state: PlaybackState) {
        _playbackState.value = state
        _audioBannerState.update { current ->
            when (state) {
                is PlaybackState.Stopped -> current.copy(
                    isVisible = false,
                    isPlaying = false,
                    isDownloading = false,
                    playbackRate = _playbackRate.value,
                    progress = 0f,
                    title = currentReciterName.orEmpty(),
                    subtitle = "",
                )
                is PlaybackState.Playing -> current.copy(
                    isVisible = true,
                    isPlaying = true,
                    isDownloading = false,
                    playbackRate = _playbackRate.value,
                )
                is PlaybackState.Paused -> current.copy(
                    isVisible = true,
                    isPlaying = false,
                    isDownloading = false,
                    playbackRate = _playbackRate.value,
                )
                is PlaybackState.Downloading -> current.copy(
                    isVisible = true,
                    isPlaying = false,
                    isDownloading = true,
                    playbackRate = _playbackRate.value,
                    progress = state.progress,
                )
            }
        }
        if (state is PlaybackState.Stopped) {
            _currentAyah.value = null
            _currentAyahProgress.value = null
            _playbackRange.value = null
        }
    }

    /**
     * Placeholder reciter resolution. In a full implementation this would
     * query a reciter repository. Returns `null` when the reciter cannot
     * be found so callers can bail out gracefully.
     */
    private suspend fun loadReciterName() {
        val reciterId = reciterPreferences.lastSelectedReciterId
        val reciter = resolveReciterById(reciterId)
        currentReciterName = reciter?.localizedName(localizer)
        _audioBannerState.update { current ->
            if (_playbackState.value is PlaybackState.Stopped) {
                current.copy(
                    title = currentReciterName.orEmpty(),
                    subtitle = ""
                )
            } else {
                current
            }
        }
    }

    private suspend fun resolveReciterById(id: Int): com.quranengine.model.quranaudio.Reciter? =
        reciterDataRetriever.getReciters().firstOrNull { it.id == id }

    private fun syncPlayingStateFromSharedPlayer() {
        when {
            audioPlayerStore.isPlaying.value -> updatePlaybackState(PlaybackState.Playing)
            audioPlayerStore.isPaused.value -> updatePlaybackState(PlaybackState.Paused)
            else -> updatePlaybackState(PlaybackState.Stopped)
        }
        audioPlayerStore.currentAyah?.let { ayah ->
            onPlayingAyah(ayah, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerStore.unregister(this)
    }
}
