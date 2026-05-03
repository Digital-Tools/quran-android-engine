package com.quranengine.ui.audiobanner

data class AudioBannerState(
    val isVisible: Boolean = false,
    val isPlaying: Boolean = false,
    val playbackRate: Float = 1f,
    val title: String = "",
    val subtitle: String = "",
    val actionHint: String = "Tap for reciter & audio options",
    val progress: Float = 0f,
    val canGoForward: Boolean = true,
    val canGoBackward: Boolean = true,
)
