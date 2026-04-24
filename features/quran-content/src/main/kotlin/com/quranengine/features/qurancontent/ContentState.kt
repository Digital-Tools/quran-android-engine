package com.quranengine.features.qurancontent

sealed class ContentState<out T> {
    data object Loading : ContentState<Nothing>()
    data class Loaded<T>(val data: T) : ContentState<T>()
    data class Error(val error: Throwable) : ContentState<Nothing>()
}
