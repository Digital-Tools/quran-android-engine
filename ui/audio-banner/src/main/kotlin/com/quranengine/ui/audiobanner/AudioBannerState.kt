package com.quranengine.ui.audiobanner

data class AudioBannerState(
    val isVisible: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String = "",
    val subtitle: String = "",
    val progress: Float = 0f,
    val canGoForward: Boolean = true,
    val canGoBackward: Boolean = true,
)
