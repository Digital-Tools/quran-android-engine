package com.quranengine.data.batchdownloader

import com.quranengine.core.utilities.features.RelativeFilePath

data class DownloadRequest(
    val url: String,
    val destination: RelativeFilePath,
) {
    val resumePath: RelativeFilePath
        get() = destination.appendingPathExtension("resume")
}

data class DownloadBatchRequest(
    val requests: List<DownloadRequest>,
)
