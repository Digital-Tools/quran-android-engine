package com.quranengine.data.batchdownloader

data class DownloadProgress(
    val total: Double,
    val completed: Double = 0.0,
) {
    val progress: Double
        get() = if (total > 0) completed / total else 0.0
}
