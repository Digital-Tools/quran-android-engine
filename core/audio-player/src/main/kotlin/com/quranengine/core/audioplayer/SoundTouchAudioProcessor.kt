package com.quranengine.core.audioplayer

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.AudioProcessorChain
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

/**
 * Pitch-preserving speed change backed by SoundTouch, replacing Media3's Sonic.
 *
 * Sonic is tuned for speech and warbles on melodic, reverberant recitation at
 * non-1x speeds; iOS avoids that with AVFoundation's `.spectral` algorithm.
 * Mirrors `SonicAudioProcessor`'s lifecycle: a tempo change takes effect on the
 * next [flush], which `DefaultAudioSink` issues whenever playback parameters change.
 */
@OptIn(UnstableApi::class)
internal class SoundTouchAudioProcessor : AudioProcessor {
    private var tempo = 1f
    private var pendingInputFormat = AudioFormat.NOT_SET
    private var inputFormat = AudioFormat.NOT_SET
    private var pendingRecreate = false
    private var soundTouch: SoundTouch? = null

    private var inputSamples = ShortArray(0)
    private var outputSamples = ShortArray(0)
    private var buffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var inputBytes = 0L
    private var outputBytes = 0L
    private var inputEnded = false

    fun setTempo(value: Float) {
        if (tempo != value) {
            tempo = value
            pendingRecreate = true
        }
    }

    /** Media time covered by [playoutDuration] of output, measured from actual throughput. */
    fun getMediaDuration(playoutDuration: Long): Long {
        val st = soundTouch
        if (st != null && outputBytes >= MIN_BYTES_FOR_DURATION_SCALING) {
            val bytesPerFrame = inputFormat.bytesPerFrame.toLong()
            val processedInput = inputBytes - st.unprocessedFrames * bytesPerFrame
            val producedOutput = outputBytes + st.availableFrames * bytesPerFrame
            if (processedInput > 0 && producedOutput > 0) {
                return Util.scaleLargeTimestamp(playoutDuration, processedInput, producedOutput)
            }
        }
        return (tempo.toDouble() * playoutDuration).toLong()
    }

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT ||
            inputAudioFormat.channelCount !in 1..MAX_CHANNELS
        ) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        pendingInputFormat = inputAudioFormat
        pendingRecreate = true
        return inputAudioFormat
    }

    override fun isActive(): Boolean =
        pendingInputFormat.sampleRate != AudioFormat.NOT_SET.sampleRate &&
            abs(tempo - 1f) >= TEMPO_TOLERANCE

    override fun queueInput(inputBuffer: ByteBuffer) {
        val st = soundTouch ?: return
        if (!inputBuffer.hasRemaining()) return
        val bytes = inputBuffer.remaining()
        val shorts = bytes / 2
        if (inputSamples.size < shorts) inputSamples = ShortArray(shorts)
        inputBuffer.asShortBuffer().get(inputSamples, 0, shorts)
        inputBuffer.position(inputBuffer.position() + bytes)
        inputBytes += bytes
        st.putSamples(inputSamples, shorts / inputFormat.channelCount)
    }

    override fun queueEndOfStream() {
        soundTouch?.flush()
        inputEnded = true
    }

    override fun getOutput(): ByteBuffer {
        val st = soundTouch
        if (st != null) {
            val frames = st.availableFrames
            if (frames > 0) {
                val channels = inputFormat.channelCount
                if (outputSamples.size < frames * channels) outputSamples = ShortArray(frames * channels)
                val received = st.receiveSamples(outputSamples, frames)
                val size = received * channels * 2
                if (buffer.capacity() < size) {
                    buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
                } else {
                    buffer.clear()
                }
                buffer.asShortBuffer().put(outputSamples, 0, received * channels)
                buffer.limit(size)
                outputBytes += size
                outputBuffer = buffer
            }
        }
        val output = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return output
    }

    override fun isEnded(): Boolean =
        inputEnded && (soundTouch == null || soundTouch!!.availableFrames == 0)

    override fun flush() {
        if (isActive()) {
            inputFormat = pendingInputFormat
            if (pendingRecreate) {
                soundTouch?.release()
                soundTouch = SoundTouch(inputFormat.sampleRate, inputFormat.channelCount, tempo)
                pendingRecreate = false
            } else {
                soundTouch?.clear()
            }
        }
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputBytes = 0
        outputBytes = 0
        inputEnded = false
    }

    override fun reset() {
        tempo = 1f
        pendingInputFormat = AudioFormat.NOT_SET
        inputFormat = AudioFormat.NOT_SET
        pendingRecreate = false
        soundTouch?.release()
        soundTouch = null
        inputSamples = ShortArray(0)
        outputSamples = ShortArray(0)
        buffer = AudioProcessor.EMPTY_BUFFER
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputBytes = 0
        outputBytes = 0
        inputEnded = false
    }

    private companion object {
        const val TEMPO_TOLERANCE = 0.0001f
        const val MAX_CHANNELS = 16
        const val MIN_BYTES_FOR_DURATION_SCALING = 1024L
    }
}

/** Audio chain for [DefaultAudioSink] that time-stretches with SoundTouch instead of Sonic. */
@OptIn(UnstableApi::class)
internal class SoundTouchAudioProcessorChain : AudioProcessorChain {
    private val processor = SoundTouchAudioProcessor()

    override fun getAudioProcessors(): Array<AudioProcessor> = arrayOf(processor)

    override fun applyPlaybackParameters(playbackParameters: PlaybackParameters): PlaybackParameters {
        processor.setTempo(playbackParameters.speed)
        // Speed only; recitation pitch is never shifted.
        return PlaybackParameters(playbackParameters.speed)
    }

    override fun applySkipSilenceEnabled(skipSilenceEnabled: Boolean): Boolean = false

    override fun getMediaDuration(playoutDuration: Long): Long = processor.getMediaDuration(playoutDuration)

    override fun getSkippedOutputFrameCount(): Long = 0
}
