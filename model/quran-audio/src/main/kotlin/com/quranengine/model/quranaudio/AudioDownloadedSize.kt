package com.quranengine.model.quranaudio

import com.quranengine.model.qurankit.Quran

data class AudioDownloadedSize(
    val downloadedSizeInBytes: Long,
    val downloadedSuraCount: Int,
    val surasCount: Int
) {
    val isDownloaded: Boolean get() = downloadedSuraCount == surasCount

    companion object {
        fun zero(quran: Quran): AudioDownloadedSize =
            AudioDownloadedSize(
                downloadedSizeInBytes = 0,
                downloadedSuraCount = 0,
                surasCount = quran.suras.size
            )
    }
}
