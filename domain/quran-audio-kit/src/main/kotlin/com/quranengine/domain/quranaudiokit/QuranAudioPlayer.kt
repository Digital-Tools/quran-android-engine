package com.quranengine.domain.quranaudiokit

import com.quranengine.core.audioplayer.NowPlayingUpdater
import com.quranengine.core.audioplayer.QueuePlayerActions
import com.quranengine.core.audioplayer.Runs
import com.quranengine.domain.reciterservice.AudioUnzipper
import com.quranengine.model.quranaudio.AudioType
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.qurankit.AyahNumber
import timber.log.Timber

/**
 * Callbacks emitted by [QuranAudioPlayer] during playback.
 */
data class QuranAudioPlayerActions(
    val playbackEnded: () -> Unit,
    val playbackPaused: () -> Unit,
    val playbackResumed: () -> Unit,
    val playing: (AyahNumber) -> Unit,
)

/**
 * Main audio playback controller for Quran audio.
 *
 * Port of the iOS `QuranAudioPlayer`. Manages play/pause/stop/seek,
 * listens to player events, and tracks the current ayah.
 */
class QuranAudioPlayer(
    private val player: QueuingPlayer,
    private val unzipper: AudioUnzipper,
    private val nowPlaying: NowPlayingUpdater,
    private val gaplessBuilder: QuranAudioRequestBuilder,
    private val gappedBuilder: QuranAudioRequestBuilder,
) {
    var actions: QuranAudioPlayerActions? = null

    private var audioRequest: QuranAudioRequest? = null

    // ---- Playback Controls ----

    fun pauseAudio() {
        player.pause()
    }

    fun resumeAudio() {
        player.resume()
    }

    fun stopAudio() {
        player.stop()
    }

    fun stepForward() {
        player.stepForward()
    }

    fun stepBackward() {
        player.stepBackward()
    }

    fun setRate(rate: Float) {
        player.setRate(rate)
    }

    // ---- Play ----

    suspend fun play(
        reciter: Reciter,
        rate: Float,
        from: AyahNumber,
        to: AyahNumber,
        verseRuns: Runs,
        listRuns: Runs,
    ) {
        Timber.i(
            "Playing startAyah=%s, to=%s, reciter=%s, verseRuns=%s, listRuns=%s",
            from, to, reciter, verseRuns, listRuns,
        )
        unzipper.unzip(reciter)

        val builder = getAudioRequestBuilder(reciter)
        val audioRequest = builder.buildRequest(reciter, from, to, frameRuns = verseRuns, requestRuns = listRuns)
        val request = audioRequest.getRequest()
        willPlay(request)
        this.audioRequest = audioRequest
        player.actions = newPlayerActions()
        player.play(request, rate)
    }

    // ---- Internal ----

    private fun playbackEnded() {
        nowPlaying.clear()
        actions?.playbackEnded?.invoke()
        player.actions = null
        audioRequest = null
    }

    private fun playbackRateChanged(rate: Float) {
        nowPlaying.updateRate(rate)
        if (rate > 0.1f) {
            actions?.playbackResumed?.invoke()
        } else {
            actions?.playbackPaused?.invoke()
        }
    }

    private fun audioFrameChanged(fileIndex: Int, frameIndex: Int, exoPlayer: androidx.media3.exoplayer.ExoPlayer) {
        val audioRequest = audioRequest ?: return

        val info = audioRequest.getPlayerInfo(fileIndex)
        nowPlaying.updateInfo(info)
        nowPlaying.updatePlayingIndex(fileIndex)

        val durationMs = exoPlayer.duration
        if (durationMs != androidx.media3.common.C.TIME_UNSET) {
            nowPlaying.updateDuration(durationMs / 1_000.0)
        }
        nowPlaying.updateElapsedTime(exoPlayer.currentPosition / 1_000.0, exoPlayer.playbackParameters.speed)

        val ayah = audioRequest.getAyahNumberFrom(fileIndex, frameIndex)
        actions?.playing?.invoke(ayah)
    }

    private fun willPlay(request: com.quranengine.core.audioplayer.AudioRequest) {
        nowPlaying.clear()
        nowPlaying.updateCount(request.files.size)
    }

    private fun getAudioRequestBuilder(reciter: Reciter): QuranAudioRequestBuilder =
        when (reciter.audioType) {
            is AudioType.Gapless -> gaplessBuilder
            is AudioType.Gapped -> gappedBuilder
        }

    private fun newPlayerActions(): QueuePlayerActions =
        QueuePlayerActions(
            playbackEnded = { playbackEnded() },
            playbackRateChanged = { rate -> playbackRateChanged(rate) },
            audioFrameChanged = { fileIndex, frameIndex, exoPlayer ->
                audioFrameChanged(fileIndex, frameIndex, exoPlayer)
            },
        )
}
