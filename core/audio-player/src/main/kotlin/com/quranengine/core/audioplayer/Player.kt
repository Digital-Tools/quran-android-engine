package com.quranengine.core.audioplayer

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import timber.log.Timber

/**
 * Thin wrapper around a Media3 [ExoPlayer] for a single audio URI.
 *
 * Port of the iOS `Player` class that wraps `AVPlayer`.
 *
 * **Must be used exclusively from the main (UI) thread** — ExoPlayer enforces
 * single-thread access from its creating looper.
 *
 * @param context Application or activity context (used to build the player).
 * @param uri     The audio [Uri] to prepare.
 */
internal class Player(
    context: Context,
    uri: Uri,
) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            false,
        )
        volume = 1f
    }

    /**
     * Invoked whenever the effective playback speed changes
     * (play / pause / rate adjustment).
     */
    var onRateChanged: ((Float) -> Unit)? = null

    /**
     * Invoked when the underlying player encounters an error.
     */
    var onError: ((Exception) -> Unit)? = null

    init {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                onRateChanged?.invoke(playbackParameters.speed)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Map play/pause to rate 0 / current speed, mirroring iOS KVO on AVPlayer.rate.
                val effectiveRate = if (isPlaying) exoPlayer.playbackParameters.speed else 0f
                onRateChanged?.invoke(effectiveRate)
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Timber.e(error, "ExoPlayer error")
                onError?.invoke(error)
            }
        })
    }

    /** Current playback position in **seconds**. */
    val currentTime: Double
        get() = exoPlayer.currentPosition / 1_000.0

    /** Total duration of the loaded media in **seconds**. Returns 0 if unknown. */
    val duration: Double
        get() {
            val durationMs = exoPlayer.duration
            return if (durationMs == androidx.media3.common.C.TIME_UNSET) 0.0 else durationMs / 1_000.0
        }

    /** Whether the player is currently playing (rate > 0). */
    val isPlaying: Boolean
        get() = exoPlayer.isPlaying

    /** The current playback speed (1.0 = normal). 0 when paused. */
    val rate: Float
        get() = if (exoPlayer.isPlaying) exoPlayer.playbackParameters.speed else 0f

    /**
     * Start playback immediately at the given [rate].
     * Equivalent to iOS `AVPlayer.playImmediately(atRate:)`.
     */
    fun play(rate: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(rate)
        exoPlayer.playWhenReady = true
    }

    /** Pause without releasing resources. */
    fun pause() {
        exoPlayer.playWhenReady = false
    }

    /** Stop and release the underlying ExoPlayer. After calling this the instance must not be reused. */
    fun stop() {
        exoPlayer.stop()
        exoPlayer.release()
    }

    /**
     * Change the playback speed without pausing.
     * Equivalent to setting `AVPlayer.rate` in iOS.
     */
    fun setRate(rate: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(rate)
    }

    /**
     * Seek to [timeInSeconds] and resume playback at [rate].
     * Equivalent to iOS `player.seek(to:) + play(rate:)`.
     */
    fun seek(timeInSeconds: Double, rate: Float) {
        exoPlayer.seekTo((timeInSeconds * 1_000).toLong())
        play(rate)
    }

    /** Expose the raw ExoPlayer for callers that need direct access (e.g. frame-change callbacks). */
    val rawPlayer: ExoPlayer get() = exoPlayer
}
