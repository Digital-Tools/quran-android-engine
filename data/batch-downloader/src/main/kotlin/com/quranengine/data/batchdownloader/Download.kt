package com.quranengine.data.batchdownloader

data class Download(
    val taskId: Int? = null,
    val request: DownloadRequest,
    val status: Status = Status.DOWNLOADING,
    val batchId: Long,
) {
    enum class Status(val value: Int) {
        DOWNLOADING(0),
        COMPLETED(1);

        companion object {
            fun fromValue(value: Int) = entries.first { it.value == value }
        }
    }
}

data class DownloadBatch(
    val id: Long,
    val downloads: List<Download>,
)
