package com.quranengine.core.audioplayer

/**
 * Internal mutable state tracker for the current playback position within an [AudioRequest].
 * Port of the Swift AudioPlaying / FilePlaying / FramePlaying structs.
 *
 * All public methods are **not** thread-safe — callers must ensure access from a single thread
 * (the main / player looper thread).
 */
internal class AudioPlaying(
    val request: AudioRequest,
    fileIndex: Int,
    frameIndex: Int,
) {
    var fileIndex: Int = fileIndex
        private set

    var frameIndex: Int = frameIndex
        private set

    var framePlays: Int = 0
        private set

    var requestPlays: Int = 0

    // ---- Convenience accessors ----

    val file: AudioFile get() = request.files[fileIndex]
    val frame: AudioFrame get() = file.frames[frameIndex]

    /**
     * Resolved end-time (in seconds) for the current frame.
     *
     * Priority:
     * 1. Explicit [AudioFrame.endTime] on the current frame.
     * 2. Start-time of the next frame **in the same file**.
     * 3. [AudioRequest.endTime] if this is the very last frame across all files.
     * 4. `null` (play to end of the media item).
     */
    val frameEndTime: Double?
        get() {
            // 1. Explicit end-time on the frame itself.
            request.files[fileIndex].frames[frameIndex].endTime?.let { return it }

            // 2 / 3. Derive from next frame or request-level endTime.
            val next = nextFrame()
                ?: return request.endTime // last frame overall → use request endTime

            return if (next.first == fileIndex) {
                // Next frame is in the same file → its start time is our end time.
                request.files[next.first].frames[next.second].startTime
            } else {
                // Next frame is in a different file → no intra-file boundary; null.
                null
            }
        }

    // ---- Mutation helpers ----

    fun setPlaying(fileIndex: Int, frameIndex: Int) {
        this.fileIndex = fileIndex
        this.frameIndex = frameIndex
    }

    fun isLastPlayForCurrentFrame(): Boolean = framePlays + 1 >= request.frameRuns.maxRuns

    fun isLastRun(): Boolean = requestPlays + 1 >= request.requestRuns.maxRuns

    fun incrementRequestPlays() {
        if (request.requestRuns != Runs.INDEFINITE) {
            requestPlays++
        }
    }

    fun incrementFramePlays() {
        if (request.frameRuns != Runs.INDEFINITE) {
            framePlays++
        }
    }

    fun resetFramePlays() {
        framePlays = 0
    }

    // ---- Navigation ----

    /**
     * Returns the (fileIndex, frameIndex) of the previous frame, or `null` if already at the start.
     */
    fun previousFrame(): Pair<Int, Int>? {
        if (frameIndex > 0) return fileIndex to (frameIndex - 1)
        if (fileIndex > 0) {
            val prev = fileIndex - 1
            return prev to (request.files[prev].frames.size - 1)
        }
        return null
    }

    /**
     * Returns the (fileIndex, frameIndex) of the next frame, or `null` if already at the end.
     */
    fun nextFrame(): Pair<Int, Int>? {
        if (frameIndex < file.frames.size - 1) return fileIndex to (frameIndex + 1)
        if (fileIndex < request.files.size - 1) return (fileIndex + 1) to 0
        return null
    }
}
