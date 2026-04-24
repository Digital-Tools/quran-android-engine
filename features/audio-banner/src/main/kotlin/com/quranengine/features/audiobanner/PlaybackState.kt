package com.quranengine.features.audiobanner

sealed class PlaybackState {
    data object Stopped : PlaybackState()
    data object Playing : PlaybackState()
    data object Paused : PlaybackState()
    data class Downloading(val progress: Float) : PlaybackState()
}
