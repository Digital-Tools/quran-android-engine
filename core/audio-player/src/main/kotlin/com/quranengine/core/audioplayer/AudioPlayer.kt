package com.quranengine.core.audioplayer

import android.content.Context
import android.os.Handler
import android.os.Looper
import timber.log.Timber

/**
 * Internal orchestrator that drives frame-by-frame playback of an [AudioRequest].
 *
 * Port of the iOS `AudioPlayer` class. The frame-end timing that iOS implements
 * with a `Timer` is achieved here with [Handler.postDelayed] on the main looper,
 * converting media-time deltas to wall-clock time via the playback rate.
 *
 * **Must be created and used on the main thread.**
 *
 * @param context Application context (for creating the inner [Player]).
 * @param request The playback request describing files, frames, and repeat counts.
 * @param rate    Initial playback speed.
 */
internal class AudioPlayer(
    private val context: Context,
    request: AudioRequest,
    private var rate: Float,
) {
    /** Callbacks — mirrors the iOS `QueuePlayerActions`. */
    var actions: QueuePlayerActions? = null

    private val playing = AudioPlaying(request, fileIndex = 0, frameIndex = 0)
    private var player: Player? = null

    private val handler = Handler(Looper.getMainLooper())
    private var pendingFrameEnd: Runnable? = null
    private var pendingProgressUpdate: Runnable? = null

    // ---- Public controls ----

    fun startPlaying() {
        play(fileIndex = 0, frameIndex = 0, forceSeek = true)
    }

    fun resume() {
        player?.play(rate) ?: return
        waitUntilFrameEnds()
        scheduleProgressUpdates()
    }

    fun pause() {
        cancelFrameTimer()
        cancelProgressUpdates()
        player?.pause()
    }

    fun stop() {
        cancelFrameTimer()
        cancelProgressUpdates()
        player?.stop()
        player = null
    }

    fun setRate(newRate: Float) {
        rate = newRate
        player?.let { p ->
            if (p.isPlaying) {
                p.setRate(newRate)
                // Reschedule the frame-end timer with the new rate.
                cancelFrameTimer()
                waitUntilFrameEnds()
            }
        }
    }

    fun stepForward() {
        cancelFrameTimer()
        playing.resetFramePlays()

        val next = playing.nextFrame()
        if (next != null) {
            play(fileIndex = next.first, frameIndex = next.second, forceSeek = true)
        } else {
            // Wrap to the beginning.
            play(fileIndex = 0, frameIndex = 0, forceSeek = true)
        }
    }

    fun stepBackward() {
        cancelFrameTimer()
        playing.resetFramePlays()

        val prev = playing.previousFrame()
        if (prev != null) {
            play(fileIndex = prev.first, frameIndex = prev.second, forceSeek = true)
        } else {
            // Already at start — re-seek to the beginning of the current frame.
            val currentPlayer = player ?: return
            currentPlayer.seek(playing.frame.startTime, rate)
            waitUntilFrameEnds()
        }
    }

    // ---- Core playback logic ----

    /**
     * Begin playing the frame at ([fileIndex], [frameIndex]).
     *
     * If the file index changed compared to what is currently loaded, a new [Player] is created
     * for the new URI. Otherwise we seek within the existing player.
     */
    private fun play(fileIndex: Int, frameIndex: Int, forceSeek: Boolean) {
        cancelFrameTimer()
        cancelProgressUpdates()

        val previousFileIndex = playing.fileIndex
        playing.setPlaying(fileIndex, frameIndex)

        val file = playing.file
        val frame = playing.frame

        // Need a new Player if the file changed or this is the first play.
        if (player == null || fileIndex != previousFileIndex) {
            player?.stop()
            val newPlayer = Player(context, file.uri)
            newPlayer.onRateChanged = { newRate ->
                actions?.playbackRateChanged?.invoke(newRate)
            }
            player = newPlayer
            // Seek to the frame start-time and begin playback.
            newPlayer.seek(frame.startTime, rate)
        } else if (forceSeek) {
            player?.seek(frame.startTime, rate)
        }

        // Notify listener about the frame change.
        actions?.audioFrameChanged?.invoke(fileIndex, frameIndex, player!!.rawPlayer)

        waitUntilFrameEnds()
        scheduleProgressUpdates()
    }

    /**
     * Schedule a delayed callback for when the current frame's end-time is reached.
     * The delay is computed in wall-clock time: `(mediaTimeRemaining) / playbackRate`.
     */
    private fun waitUntilFrameEnds() {
        cancelFrameTimer()

        val endTime = playing.frameEndTime ?: return // no end-time → play until media ends naturally
        val p = player ?: return
        val currentTime = p.currentTime
        val mediaRemaining = endTime - currentTime
        if (mediaRemaining <= 0) {
            onFrameEnded()
            return
        }

        val effectiveRate = if (rate > 0f) rate else 1f
        val wallClockDelayMs = ((mediaRemaining / effectiveRate) * 1_000).toLong()

        Timber.d(
            "Frame timer: %.2fs media remaining, rate=%.1f → %dms wall-clock delay",
            mediaRemaining,
            effectiveRate,
            wallClockDelayMs,
        )

        val runnable = Runnable { onFrameEnded() }
        pendingFrameEnd = runnable
        handler.postDelayed(runnable, wallClockDelayMs)
    }

    /**
     * Called when the current frame's end-time has been reached.
     * Decides whether to replay the frame, advance to the next, loop the request, or stop.
     */
    private fun onFrameEnded() {
        pendingFrameEnd = null

        // 1. Repeat the same frame if frame-runs not exhausted.
        playing.incrementFramePlays()
        if (!playing.isLastPlayForCurrentFrame()) {
            val p = player ?: return
            p.seek(playing.frame.startTime, rate)
            waitUntilFrameEnds()
            return
        }

        // Frame runs exhausted — move to next frame.
        playing.resetFramePlays()

        val next = playing.nextFrame()
        if (next != null) {
            play(fileIndex = next.first, frameIndex = next.second, forceSeek = true)
            return
        }

        // All frames done — check request-level repeats.
        playing.incrementRequestPlays()
        if (!playing.isLastRun()) {
            play(fileIndex = 0, frameIndex = 0, forceSeek = true)
            return
        }

        // Truly finished.
        stop()
        actions?.playbackEnded?.invoke()
    }

    private fun cancelFrameTimer() {
        pendingFrameEnd?.let { handler.removeCallbacks(it) }
        pendingFrameEnd = null
    }

    private fun scheduleProgressUpdates() {
        val runnable = object : Runnable {
            override fun run() {
                val currentPlayer = player ?: return
                actions?.audioFrameChanged?.invoke(playing.fileIndex, playing.frameIndex, currentPlayer.rawPlayer)
                pendingProgressUpdate = this
                handler.postDelayed(this, PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
        pendingProgressUpdate = runnable
        handler.postDelayed(runnable, PROGRESS_UPDATE_INTERVAL_MS)
    }

    private fun cancelProgressUpdates() {
        pendingProgressUpdate?.let { handler.removeCallbacks(it) }
        pendingProgressUpdate = null
    }

    private companion object {
        const val PROGRESS_UPDATE_INTERVAL_MS = 150L
    }
}
