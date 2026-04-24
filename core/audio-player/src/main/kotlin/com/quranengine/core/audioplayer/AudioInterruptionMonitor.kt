package com.quranengine.core.audioplayer

import android.media.AudioManager

/**
 * Categorises audio-focus changes into simple interruption events
 * analogous to the iOS `AVAudioSession.interruptionNotification`.
 */
enum class AudioInterruptionType {
    /** Another app took audio focus — pause playback. */
    BEGAN,

    /** Focus returned and playback should resume automatically. */
    ENDED_SHOULD_RESUME,

    /** Focus returned but playback should stay paused (user must press play). */
    ENDED_SHOULD_NOT_RESUME,
}

/**
 * Monitors Android audio-focus changes and maps them to [AudioInterruptionType] events.
 *
 * Create an instance, set [onAudioInterruption], then register with
 * [AudioManager.requestAudioFocus].
 *
 * Port of the iOS `AudioInterruptionMonitor` that listens to
 * `AVAudioSession.interruptionNotification`.
 */
internal class AudioInterruptionMonitor : AudioManager.OnAudioFocusChangeListener {

    /**
     * Callback invoked on the main thread when an audio interruption occurs.
     * Set this before requesting audio focus.
     */
    var onAudioInterruption: ((AudioInterruptionType) -> Unit)? = null

    override fun onAudioFocusChange(focusChange: Int) {
        val type = when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            -> AudioInterruptionType.BEGAN

            AudioManager.AUDIOFOCUS_GAIN -> AudioInterruptionType.ENDED_SHOULD_RESUME

            // Transient-can-duck: pause playback to match iOS behaviour.
            // Volume ducking could be added later if desired.
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> AudioInterruptionType.BEGAN

            else -> return
        }
        onAudioInterruption?.invoke(type)
    }
}
