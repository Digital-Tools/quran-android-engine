package com.quranengine.core.audioplayer

import timber.log.Timber

/**
 * Thin handle around a native SoundTouch time-stretcher (see `src/main/cpp`).
 * Samples are interleaved 16-bit PCM. Not thread-safe; owned by one
 * [SoundTouchAudioProcessor] on ExoPlayer's playback thread.
 */
internal class SoundTouch(sampleRate: Int, private val channels: Int, tempo: Float) {
    private var handle: Long = nativeCreate(sampleRate, channels, tempo)

    init {
        check(handle != 0L) { "SoundTouch rejected $sampleRate Hz / $channels channels" }
    }

    /** Queues [frames] frames from [samples] (interleaved, `frames * channels` shorts). */
    fun putSamples(samples: ShortArray, frames: Int) = nativePutSamples(handle, samples, frames)

    /** Copies up to [maxFrames] processed frames into [output]; returns the frame count. */
    fun receiveSamples(output: ShortArray, maxFrames: Int): Int =
        nativeReceiveSamples(handle, output, maxFrames)

    /** Processed frames ready to [receiveSamples]. */
    val availableFrames: Int get() = nativeAvailableFrames(handle)

    /** Input frames queued but not yet stretched. */
    val unprocessedFrames: Int get() = nativeUnprocessedFrames(handle)

    /** Pushes any buffered input through (end of stream). */
    fun flush() = nativeFlush(handle)

    /** Drops all buffered input and output (seek). */
    fun clear() = nativeClear(handle)

    fun release() {
        if (handle != 0L) {
            nativeRelease(handle)
            handle = 0L
        }
    }

    companion object {
        /** False when the native library could not be loaded; callers fall back to Sonic. */
        val isAvailable: Boolean by lazy {
            try {
                System.loadLibrary("quran_soundtouch")
                true
            } catch (e: UnsatisfiedLinkError) {
                Timber.e(e, "SoundTouch native library unavailable; using Media3 Sonic")
                false
            }
        }

        @JvmStatic private external fun nativeCreate(sampleRate: Int, channels: Int, tempo: Float): Long
        @JvmStatic private external fun nativeRelease(handle: Long)
        @JvmStatic private external fun nativePutSamples(handle: Long, samples: ShortArray, frames: Int)
        @JvmStatic private external fun nativeReceiveSamples(handle: Long, output: ShortArray, maxFrames: Int): Int
        @JvmStatic private external fun nativeAvailableFrames(handle: Long): Int
        @JvmStatic private external fun nativeUnprocessedFrames(handle: Long): Int
        @JvmStatic private external fun nativeFlush(handle: Long)
        @JvmStatic private external fun nativeClear(handle: Long)
    }
}
