package com.quranengine.domain.quranaudiokit

import com.quranengine.core.audioplayer.AudioRequest
import com.quranengine.core.audioplayer.QueuePlayerActions

/**
 * Abstraction for audio queue playback.
 * On Android, [com.quranengine.core.audioplayer.QueuePlayer] implements this directly.
 */
interface QueuingPlayer {
    var actions: QueuePlayerActions?

    fun play(request: AudioRequest, rate: Float)
    fun pause()
    fun resume()
    fun stop()
    fun stepForward()
    fun stepBackward()
    fun setRate(rate: Float)
}
