package com.quranengine.features.wordpointer

import android.graphics.PointF
import com.quranengine.domain.wordtextservice.WordTextService
import com.quranengine.model.qurangeometry.WordFrame
import com.quranengine.model.qurangeometry.WordFrameCollection
import com.quranengine.model.qurankit.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordPointerViewModel(
    private val wordTextService: WordTextService,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val _state = MutableStateFlow<WordPointerState>(WordPointerState.Idle)
    val state: StateFlow<WordPointerState> = _state.asStateFlow()

    private var textLookupJob: Job? = null
    private var currentWord: Word? = null

    fun onPanBegan() {
        // Reset state at the start of a new gesture
        currentWord = null
        _state.value = WordPointerState.Idle
    }

    fun onPanMoved(x: Float, y: Float, frames: WordFrameCollection) {
        val point = PointF(x, y)
        val word = frames.wordAtLocation(point)

        if (word == null) {
            if (currentWord != null) {
                currentWord = null
                _state.value = WordPointerState.Idle
            }
            return
        }

        // Same word as before — no update needed
        if (word == currentWord) return

        currentWord = word

        val frame = frames.wordFrameForWord(word) ?: return

        // Show highlight immediately without text
        _state.value = WordPointerState.Highlighting(
            word = word,
            frame = frame,
            text = null,
        )

        // Look up word text asynchronously
        textLookupJob?.cancel()
        textLookupJob = scope.launch {
            val text = wordTextService.textForWord(word)
            // Only update if this word is still the active one
            if (currentWord == word) {
                _state.value = WordPointerState.Highlighting(
                    word = word,
                    frame = frame,
                    text = text,
                )
            }
        }
    }

    fun onPanEnded() {
        textLookupJob?.cancel()
        textLookupJob = null
        currentWord = null
        _state.value = WordPointerState.Idle
    }

    fun destroy() {
        textLookupJob?.cancel()
        scope.cancel()
    }
}
