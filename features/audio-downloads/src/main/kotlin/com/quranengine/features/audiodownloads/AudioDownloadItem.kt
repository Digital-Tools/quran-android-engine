package com.quranengine.features.audiodownloads

import com.quranengine.model.quranaudio.AudioDownloadedSize
import com.quranengine.model.quranaudio.Reciter
import java.util.Locale

data class AudioDownloadItem(
    val reciter: Reciter,
    val displayName: String,
    val size: AudioDownloadedSize?,
    val progress: Double?,
) {
    val isDownloading: Boolean get() = progress != null
    val isDownloaded: Boolean get() = size?.isDownloaded == true
    val canDelete: Boolean get() = (size?.downloadedSizeInBytes ?: 0L) > 0L

    fun sizeLabel(): String {
        val downloaded = size ?: return ""
        val suras = if (downloaded.downloadedSuraCount == 1) {
            "1 sura"
        } else {
            "${downloaded.downloadedSuraCount} suras"
        }
        if (downloaded.downloadedSizeInBytes == 0L) return suras
        return "${formatBytes(downloaded.downloadedSizeInBytes)} · $suras"
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
    return String.format(Locale.US, "%.2f GB", mb / 1024.0)
}
