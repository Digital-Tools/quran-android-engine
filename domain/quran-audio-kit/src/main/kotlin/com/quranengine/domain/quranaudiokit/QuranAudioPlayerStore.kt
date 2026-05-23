package com.quranengine.domain.quranaudiokit

import com.quranengine.model.qurankit.AyahNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared Quran audio player for host apps (e.g. Mizan dashboard + in-app banner).
 *
 * Port of iOS `QuranAudioPlayerStore`. Multiplexes [QuranAudioPlayerActions]
 * so dashboard headless playback and the in-app audio banner stay in sync.
 */
class QuranAudioPlayerStore(
    val player: QuranAudioPlayer,
) {
    private val handlers = mutableMapOf<Any, QuranAudioPlayerActions>()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    var currentAyah: AyahNumber? = null
        private set

    init {
        player.actions = QuranAudioPlayerActions(
            playbackEnded = {
                currentAyah = null
                updateState(isPlaying = false, isPaused = false)
                broadcast { playbackEnded() }
            },
            playbackPaused = {
                updateState(isPlaying = false, isPaused = true)
                broadcast { playbackPaused() }
            },
            playbackResumed = {
                updateState(isPlaying = true, isPaused = false)
                broadcast { playbackResumed() }
            },
            playing = { ayah, progress ->
                currentAyah = ayah
                updateState(isPlaying = true, isPaused = false)
                broadcast { playing(ayah, progress) }
            },
        )
    }

    fun register(owner: Any, actions: QuranAudioPlayerActions) {
        handlers[owner] = actions
    }

    fun unregister(owner: Any) {
        handlers.remove(owner)
    }

    private fun broadcast(block: QuranAudioPlayerActions.() -> Unit) {
        handlers.values.forEach { it.block() }
    }

    private fun updateState(isPlaying: Boolean, isPaused: Boolean) {
        _isPlaying.value = isPlaying
        _isPaused.value = isPaused
    }
}
