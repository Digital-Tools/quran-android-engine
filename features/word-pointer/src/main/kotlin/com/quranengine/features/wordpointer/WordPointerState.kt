package com.quranengine.features.wordpointer

import com.quranengine.model.qurangeometry.WordFrame
import com.quranengine.model.qurankit.Word

sealed class WordPointerState {
    data object Idle : WordPointerState()

    data class Highlighting(
        val word: Word,
        val frame: WordFrame,
        val text: String?,
    ) : WordPointerState()
}
