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

    // A pause after a verse (VerseDelay / RepetitionDelay): the player is paused
    // and [afterDelay] runs when [pendingDelay] fires.
    private var pendingDelay: Runnable? = null
    private var afterDelay: (() -> Unit)? = null

    // True while waiting out a delay, so our own pause isn't reported as the
    // user pausing (port of the iOS `isDelaying`).
    private var isDelaying = false

    // ---- Public controls ----

    fun startPlaying() {
        play(fileIndex = 0, frameIndex = 0, forceSeek = true)
    }

    fun resume() {
        // Paused during a between-verse pause: skip what's left of it.
        afterDelay?.let { action ->
            afterDelay = null
            action()
            return
        }
        player?.play(rate) ?: return
        waitUntilFrameEnds()
        scheduleProgressUpdates()
    }

    fun pause() {
        // Keep [afterDelay] so resume() continues to the next verse.
        cancelDelayTimer()
        isDelaying = false
        cancelFrameTimer()
        cancelProgressUpdates()
        player?.pause()
    }

    fun stop() {
        cancelDelay()
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
        cancelDelay()
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
        cancelDelay()
        cancelFrameTimer()
        playing.resetFramePlays()

        val currentPlayer = player
        val currentPosition = currentPlayer?.currentTime ?: 0.0
        val frameStartTime = playing.frame.startTime

        // If played more than 2 seconds into current frame, restart current frame first
        if (currentPlayer != null && (currentPosition - frameStartTime) > 2.0) {
            currentPlayer.seek(frameStartTime, rate)
            waitUntilFrameEnds()
            return
        }

        val prev = playing.previousFrame()
        if (prev != null) {
            play(fileIndex = prev.first, frameIndex = prev.second, forceSeek = true)
        } else {
            // Already at start — re-seek to the beginning of the current frame.
            if (currentPlayer != null) {
                currentPlayer.seek(frameStartTime, rate)
                waitUntilFrameEnds()
            }
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
        cancelDelay()
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
                // Ignore the pause we trigger ourselves while waiting out a delay.
                if (!isDelaying) actions?.playbackRateChanged?.invoke(newRate)
            }
            newPlayer.onEnded = { playerEnded(newPlayer) }
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
     * Media-time remaining until the current frame's end, in seconds.
     *
     * Falls back to the player's total [Player.duration] when the frame has no
     * resolvable end-time (e.g. the boundary between two files), matching the iOS
     * `getDurationToFrameEnd` — so a timer is always scheduled and playback never
     * silently stalls waiting for a native "playback ended" event that this class
     * doesn't listen for.
     */
    private fun durationToFrameEnd(p: Player): Double {
        val endTime = playing.frameEndTime ?: p.duration
        return endTime - p.currentTime
    }

    /**
     * Schedule a delayed callback for when the current frame's end-time is reached.
     * The delay is computed in wall-clock time: `(mediaTimeRemaining) / playbackRate`.
     */
    private fun waitUntilFrameEnds() {
        cancelFrameTimer()

        val p = player ?: return
        val mediaRemaining = durationToFrameEnd(p)
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
     * Called when the current frame's end-time is believed to have been reached.
     * Decides whether to replay the frame, advance to the next, loop the request, or stop.
     */
    private fun onFrameEnded() {
        pendingFrameEnd = null

        // The Handler callback can fire a little early (dispatch jitter). If the frame
        // genuinely hasn't ended yet, reschedule instead of advancing/repeating prematurely —
        // port of the iOS `guard time < 0.2` check. A file that has played to its end
        // never gets closer to a frame time that lies beyond it, so it counts as ended.
        val p = player
        if (p != null && !p.hasEnded && durationToFrameEnd(p) >= 0.2) {
            waitUntilFrameEnds()
            return
        }

        advanceAfterFrame(fileEnded = p?.hasEnded == true)
    }

    /**
     * The file ran out of audio before the frame timer fired (a timing database can
     * run slightly past the real end of its audio). Without this, the timer kept
     * rescheduling itself against a finished player and playback stopped for good
     * at the end of the surah instead of going on to the next one.
     */
    private fun playerEnded(ended: Player) {
        if (ended !== player || isDelaying) return
        cancelFrameTimer()
        advanceAfterFrame(fileEnded = true)
    }

    private fun advanceAfterFrame(fileEnded: Boolean) {

        // Pause before whatever plays next, scaled by the verse that just ended.
        val delay = verseDelaySeconds()

        // 1. Repeat the same frame if frame-runs not exhausted.
        playing.incrementFramePlays()
        Timber.d(
            "onFrameEnded: file=%d frame=%d framePlays=%d/%d requestPlays=%d/%d",
            playing.fileIndex,
            playing.frameIndex,
            playing.framePlays,
            playing.request.frameRuns.maxRuns,
            playing.requestPlays,
            playing.request.requestRuns.maxRuns,
        )
        if (!playing.isLastPlayForCurrentFrame()) {
            playAfterDelay(delay) {
                val player = player ?: return@playAfterDelay
                player.seek(playing.frame.startTime, rate)
                waitUntilFrameEnds()
                scheduleProgressUpdates()
            }
            return
        }

        // Frame runs exhausted — move to next frame.
        playing.resetFramePlays()

        // Once the file has run out, the frames left in it start after its audio and
        // have nothing to play (the timings run past the file): go on with the next
        // file instead of waiting out each silent verse.
        val next = if (fileEnded) playing.firstFrameOfNextFile() else playing.nextFrame()
        if (next != null) {
            playAfterDelay(delay) {
                play(fileIndex = next.first, frameIndex = next.second, forceSeek = true)
            }
            return
        }

        // All frames done — check request-level repeats.
        playing.incrementRequestPlays()
        Timber.d(
            "onFrameEnded: request pass complete, requestPlays=%d/%d",
            playing.requestPlays,
            playing.request.requestRuns.maxRuns,
        )
        if (!playing.isLastRun()) {
            // The verse pause plus the fixed pause between repetitions.
            playAfterDelay(delay + playing.request.repetitionDelay.seconds) {
                play(fileIndex = 0, frameIndex = 0, forceSeek = true)
            }
            return
        }

        // Truly finished.
        stop()
        actions?.playbackEnded?.invoke()
    }

    /**
     * Wall-clock pause after the frame that just ended: its recited length
     * (media time / rate) times the [VerseDelay] multiplier. 0 when off.
     */
    private fun verseDelaySeconds(): Double {
        val multiplier = playing.request.verseDelay.multiplier
        if (multiplier <= 0) return 0.0
        val p = player ?: return 0.0
        val frameEnd = playing.frameEndTime ?: p.duration
        val recited = (frameEnd - playing.frame.startTime).coerceAtLeast(0.0)
        val effectiveRate = if (rate > 0f) rate else 1f
        return recited / effectiveRate * multiplier
    }

    /** Runs [action] after pausing [seconds]; immediately when there is no pause. */
    private fun playAfterDelay(seconds: Double, action: () -> Unit) {
        if (seconds <= 0) {
            action()
            return
        }
        cancelProgressUpdates()
        isDelaying = true
        player?.pause()
        afterDelay = action
        val runnable = Runnable {
            pendingDelay = null
            isDelaying = false
            val next = afterDelay
            afterDelay = null
            next?.invoke()
        }
        pendingDelay = runnable
        handler.postDelayed(runnable, (seconds * 1_000).toLong())
    }

    private fun cancelDelayTimer() {
        pendingDelay?.let { handler.removeCallbacks(it) }
        pendingDelay = null
    }

    private fun cancelDelay() {
        cancelDelayTimer()
        afterDelay = null
        isDelaying = false
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
