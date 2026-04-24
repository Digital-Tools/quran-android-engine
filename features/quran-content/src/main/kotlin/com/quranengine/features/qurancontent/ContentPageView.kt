package com.quranengine.features.qurancontent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quranengine.ui.components.DataUnavailableView
import com.quranengine.ui.components.LoadingView

@Composable
fun <T> ContentPageView(
    state: ContentState<T>,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            is ContentState.Loading -> {
                LoadingView()
            }
            is ContentState.Loaded -> {
                content(state.data)
            }
            is ContentState.Error -> {
                DataUnavailableView(
                    title = "Error Loading Content",
                    message = state.error.localizedMessage ?: "An unknown error occurred",
                )
            }
        }
    }
}
