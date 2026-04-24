package com.quranengine.core.audioplayer

import android.net.Uri

/**
 * A single time-bounded frame within an audio file.
 *
 * @param startTime Start time in seconds.
 * @param endTime   End time in seconds, or null if the frame extends to the next frame / end of file.
 */
data class AudioFrame(
    val startTime: Double,
    val endTime: Double?,
)

/**
 * An audio file together with its frame segmentation.
 *
 * @param uri    Content or file [Uri] pointing to the audio.
 * @param frames Ordered list of frames within the file.
 */
data class AudioFile(
    val uri: Uri,
    val frames: List<AudioFrame>,
)

/**
 * A complete playback request describing what to play and how many times.
 *
 * @param files       Ordered list of audio files with their frames.
 * @param endTime     Optional global end-time (seconds) for the last frame of the last file.
 * @param frameRuns   How many times each individual frame should repeat.
 * @param requestRuns How many times the entire request should repeat.
 */
data class AudioRequest(
    val files: List<AudioFile>,
    val endTime: Double?,
    val frameRuns: Runs,
    val requestRuns: Runs,
)
