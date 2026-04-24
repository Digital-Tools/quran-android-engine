package com.quranengine.features.translations

import com.quranengine.model.qurantext.Translation

data class TranslationItemState(
    val translation: Translation,
    val downloadProgress: DownloadProgress,
) {
    sealed class DownloadProgress {
        data object NotDownloaded : DownloadProgress()
        data class Downloading(val progress: Float) : DownloadProgress()
        data object Downloaded : DownloadProgress()
        data object NeedsUpgrade : DownloadProgress()
    }
}
