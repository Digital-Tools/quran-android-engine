package com.quranengine.core.audioplayer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.media3.common.C
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
        PlaybackService.start(context)
        PlaybackService.Controls.apply {
            onPlay = { resume() }
            onPause = { pause() }
            onNext = { stepForward() }
            onPrevious = { stepBackward() }
            onStop = { stopFromControls() }
        }

        speed = rate
        val player = AudioPlayer(context, request, rate)
        player.actions = actions?.let { withServiceUpdates(it) }
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
        stopService()
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

    // ---- Foreground service ----

    private var paused = false
    private var speed = 1f

    /** The player of the file now playing, for the lock screen's progress bar. */
    private var currentFile: ExoPlayer? = null

    /**
     * Stop pressed on the lock screen, in the notification or on a headset. [stop] does
     * not report the end (the app's own Stop button updates its screen itself), so the
     * app is told here, or its player would still show a recitation that has stopped.
     */
    private fun stopFromControls() {
        val listener = actions
        stop()
        listener?.playbackEnded?.invoke()
    }

    private fun stopService() {
        PlaybackService.Controls.clear()
        currentFile = null
        PlaybackService.stop(context)
    }

    private fun publishPlayback() {
        val file = currentFile
        PlaybackService.updatePlayback(
            isPaused = paused,
            newPositionMs = file?.currentPosition ?: -1,
            newDurationMs = file?.duration?.takeIf { it != C.TIME_UNSET } ?: -1,
            newSpeed = speed,
        )
    }

    /**
     * Keeps the foreground service in step with playback: the lock screen follows
     * pause and resume, each new verse updates its position, and it goes away when
     * the request finishes by itself.
     */
    private fun withServiceUpdates(callbacks: QueuePlayerActions): QueuePlayerActions =
        callbacks.copy(
            playbackEnded = {
                stopService()
                callbacks.playbackEnded()
            },
            playbackRateChanged = { rate ->
                paused = rate <= 0f
                if (rate > 0f) speed = rate
                publishPlayback()
                callbacks.playbackRateChanged(rate)
            },
            audioFrameChanged = { fileIndex, frameIndex, exoPlayer ->
                currentFile = exoPlayer
                publishPlayback()
                callbacks.audioFrameChanged(fileIndex, frameIndex, exoPlayer)
            },
        )

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
