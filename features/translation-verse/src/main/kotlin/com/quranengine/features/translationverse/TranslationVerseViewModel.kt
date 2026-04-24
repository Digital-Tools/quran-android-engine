package com.quranengine.features.translationverse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.quranengine.features.qurantranslation.TranslationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TranslationVerseViewModel(
    private val verse: Int,
    private val scope: CoroutineScope,
) {
    var state by mutableStateOf(TranslationVerseState(verse = verse))
        private set

    fun loadTranslations(
        loader: suspend (Int) -> List<TranslationItem>,
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val items = loader(verse)
                state = state.copy(
                    items = items,
                    isLoading = false,
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e,
                )
            }
        }
    }
}
