package com.quranengine.core.audioplayer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.media3.exoplayer.ExoPlayer
import timber.log.Timber

/**
 * Callbacks emitted by [QueuePlayer] during playback.
 *
 * Port of the iOS `QueuePlayerActions` struct.
 *
 * @param playbackEnded       Invoked when the entire request has finished playing.
 * @param playbackRateChanged Invoked whenever the effective playback speed changes (including pause → 0).
 * @param audioFrameChanged   Invoked when the active frame changes. Receives (fileIndex, frameIndex, exoPlayer).
 */
data class QueuePlayerActions(
    val playbackEnded: () -> Unit,
    val playbackRateChanged: (Float) -> Unit,
    val audioFrameChanged: (Int, Int, ExoPlayer) -> Unit,
)

/**
 * Public façade for frame-aware, repeat-capable audio playback.
 *
 * Port of the iOS `QueuePlayer` that wraps `AVPlayer` / `AVAudioSession`.
 * On Android the audio session concept is replaced by [AudioManager] audio-focus,
 * and playback is driven by Media3 [ExoPlayer] via the internal [AudioPlayer].
 *
 * **Must be created and used on the main thread.**
 *
 * @param context Application context (kept for player creation and audio-focus).
 */
class QueuePlayer(private val context: Context) {

    /** Set callbacks before calling [play]. */
    var actions: QueuePlayerActions? = null

    private var audioPlayer: AudioPlayer? = null
        set(value) {
            // Detach callbacks from previous player.
            field?.actions = null
            field = value
        }

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val interruptionMonitor = AudioInterruptionMonitor()
    private var audioFocusRequest: AudioFocusRequest? = null

    init {
        interruptionMonitor.onAudioInterruption = { type ->
            when (type) {
                AudioInterruptionType.BEGAN -> pause()
                AudioInterruptionType.ENDED_SHOULD_RESUME -> resume()
                AudioInterruptionType.ENDED_SHOULD_NOT_RESUME -> {
                    // Stay paused — user must press play.
                }
            }
        }
    }

    // ---- Public API ----

    /**
     * Start playing the given [request] at the specified playback [rate].
     * Any currently active playback is stopped first.
     */
    fun play(request: AudioRequest, rate: Float = 1f) {
        stop()
        requestAudioFocus()

        val player = AudioPlayer(context, request, rate)
        player.actions = actions
        audioPlayer = player
        player.startPlaying()
    }

    /** Pause playback (keeps position). */
    fun pause() {
        audioPlayer?.pause()
    }

    /** Resume from a paused state. */
    fun resume() {
        audioPlayer?.resume()
    }

    /** Stop playback and release resources. */
    fun stop() {
        audioPlayer?.stop()
        audioPlayer = null
        abandonAudioFocus()
    }

    /** Change the playback speed. */
    fun setRate(rate: Float) {
        audioPlayer?.setRate(rate)
    }

    /** Skip to the next frame (or wrap to the start). */
    fun stepForward() {
        audioPlayer?.stepForward()
    }

    /** Skip to the previous frame (or re-seek to the current frame's start). */
    fun stepBackward() {
        audioPlayer?.stepBackward()
    }

    // ---- Audio focus ----

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener(interruptionMonitor)
                .build()

            audioFocusRequest = request
            val result = audioManager.requestAudioFocus(request)
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Timber.w("Audio focus request not granted: %d", result)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                interruptionMonitor,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN,
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(interruptionMonitor)
        }
    }
}
