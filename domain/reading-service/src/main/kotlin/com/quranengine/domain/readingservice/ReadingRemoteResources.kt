package com.quranengine.domain.readingservice

import com.quranengine.core.utilities.features.RelativeFilePath
import com.quranengine.data.batchdownloader.DownloadBatchResponse
import com.quranengine.data.batchdownloader.DownloadRequest
import com.quranengine.model.qurankit.Reading

/**
 * Provides the remote resource metadata for a given [Reading].
 *
 * Port of the Swift `ReadingRemoteResources` protocol.
 */
interface ReadingRemoteResources {
    fun resource(reading: Reading): RemoteResource?
}

/**
 * Describes a single remote resource that must be downloaded for a reading.
 */
data class RemoteResource(
    val url: String,
    val reading: Reading,
    val version: Int,
) {
    val downloadDestination: RelativeFilePath =
        READINGS_PATH.appendingPathComponent(reading.localPath, isDirectory = true)

    val zipFile: RelativeFilePath
        get() {
            val fileName = url.substringAfterLast('/')
            return downloadDestination.appendingPathComponent(fileName)
        }

    val successFilePath: RelativeFilePath
        get() = downloadDestination.appendingPathComponent("success-v$version.txt")

    fun matches(batch: DownloadBatchResponse): Boolean {
        val request = batch.requests.singleOrNull() ?: return false
        return matches(request)
    }

    fun matches(request: DownloadRequest): Boolean =
        request.destination == downloadDestination

    companion object {
        private val READINGS_PATH = RelativeFilePath("readings", isDirectory = true)
    }
}

/** Local directory name for each [Reading]. */
val Reading.localPath: String
    get() = when (this) {
        Reading.HAFS_1405 -> "hafs_1405"
        Reading.HAFS_1440 -> "hafs_1440"
        Reading.HAFS_1421 -> "hafs_1421"
        Reading.TAJWEED -> "tajweed"
    }

data class ReadingImageResources(
    val databasePath: String,
    val imagesPath: String,
)

val Reading.imageResources: ReadingImageResources
    get() = when (this) {
        Reading.HAFS_1405 -> ReadingImageResources(
            databasePath = "images_1920/databases/ayahinfo_1920.db",
            imagesPath = "images_1920/width_1920",
        )
        Reading.HAFS_1421 -> ReadingImageResources(
            databasePath = "images_1120/databases/ayahinfo_1120.db",
            imagesPath = "images_1120/width_1120",
        )
        Reading.HAFS_1440 -> ReadingImageResources(
            databasePath = "images_1352/databases/ayahinfo_1352.db",
            imagesPath = "images_1352/width_1352",
        )
        Reading.TAJWEED -> ReadingImageResources(
            databasePath = "images_1280/databases/ayahinfo_1280.db",
            imagesPath = "images_1280/width_1280",
        )
    }

val Reading.bundledAssetsPath: String
    get() = "readings/$localPath"

/** Returns `true` if the given path is beneath the readings directory. */
fun Reading.Companion.isDownloadDestinationPath(path: RelativeFilePath): Boolean {
    val readingsPath = RelativeFilePath("readings", isDirectory = true)
    return readingsPath.isParent(path)
}
