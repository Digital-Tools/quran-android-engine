package com.quranengine.features.translationverse

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quranengine.features.qurantranslation.ContentTranslationView
import com.quranengine.ui.components.DataUnavailableView
import com.quranengine.ui.components.LoadingView
import com.quranengine.ui.theme.QuranTheme
import com.quranengine.ui.theme.themedBackground

@Composable
fun TranslationVerseScreen(
    state: TranslationVerseState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .themedBackground(),
    ) {
        when {
            state.isLoading -> {
                LoadingView()
            }
            state.error != null -> {
                DataUnavailableView(
                    title = "Error Loading Translation",
                    message = state.error.localizedMessage ?: "An unknown error occurred",
                )
            }
            else -> {
                ContentTranslationView(
                    items = state.items,
                )
            }
        }
    }
}
