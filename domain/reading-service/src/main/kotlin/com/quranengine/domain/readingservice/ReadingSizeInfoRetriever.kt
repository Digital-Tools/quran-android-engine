package com.quranengine.domain.readingservice

import com.quranengine.core.system.FileSystem
import com.quranengine.model.qurankit.Reading
import java.io.File

/**
 * Information about the disk space occupied by a downloaded reading.
 */
data class ReadingDownloadedSize(
    val reading: Reading,
    val sizeInBytes: Long,
)

/**
 * Calculates how much disk space each reading's downloaded files occupy.
 *
 * Port of the Swift `ReadingSizeInfoRetriever`.
 */
class ReadingSizeInfoRetriever(
    private val fileSystem: FileSystem,
    private val baseDir: File,
) {
    /**
     * Returns the total size on disk for [reading]'s downloaded resources,
     * or `null` if no files are present.
     */
    fun sizeInfo(reading: Reading): ReadingDownloadedSize? {
        val remoteResource = RemoteResource(url = "", reading = reading, version = 0)
        val dir = remoteResource.downloadDestination.file(baseDir)
        if (!fileSystem.fileExists(dir)) return null

        val totalBytes = directorySize(dir)
        if (totalBytes == 0L) return null
        return ReadingDownloadedSize(reading, totalBytes)
    }

    /** Returns the sizes for all readings that have downloaded content. */
    fun allSizeInfo(): List<ReadingDownloadedSize> {
        return Reading.sortedReadings.mapNotNull { sizeInfo(it) }
    }

    private fun directorySize(dir: File): Long {
        if (!dir.exists()) return 0L
        if (dir.isFile) return fileSystem.fileSize(dir) ?: 0L
        return try {
            fileSystem.contentsOfDirectory(dir).sumOf { directorySize(it) }
        } catch (_: Exception) {
            0L
        }
    }
}
