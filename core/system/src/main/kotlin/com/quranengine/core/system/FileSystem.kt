package com.quranengine.core.system

import com.quranengine.core.utilities.features.RelativeFilePath
import java.io.File
import java.io.IOException

/**
 * Abstraction over file-system operations.
 * Port of Swift's `FileSystem` protocol.
 */
interface FileSystem {
    fun fileExists(file: File): Boolean
    fun createDirectory(file: File, withIntermediateDirectories: Boolean)
    fun copyItem(src: File, dst: File)
    fun removeItem(file: File)
    fun moveItem(src: File, dst: File)
    fun contentsOfDirectory(directory: File): List<File>
    fun fileSize(file: File): Long?
    fun writeToFile(file: File, content: String)
}

/** Convenience extensions for [RelativeFilePath]. */
fun FileSystem.fileExists(path: RelativeFilePath, baseDir: File): Boolean =
    fileExists(path.file(baseDir))

fun FileSystem.removeItem(path: RelativeFilePath, baseDir: File) {
    removeItem(path.file(baseDir))
}

fun FileSystem.createDirectory(path: RelativeFilePath, baseDir: File, withIntermediateDirectories: Boolean) {
    createDirectory(path.file(baseDir), withIntermediateDirectories)
}

fun FileSystem.moveItem(src: File, dst: RelativeFilePath, baseDir: File) {
    moveItem(src, dst.file(baseDir))
}

fun FileSystem.contentsOfDirectory(path: RelativeFilePath, baseDir: File): List<File> =
    contentsOfDirectory(path.file(baseDir))

/**
 * Default implementation backed by [java.io.File] APIs.
 */
class DefaultFileSystem : FileSystem {

    override fun fileExists(file: File): Boolean = file.exists()

    override fun createDirectory(file: File, withIntermediateDirectories: Boolean) {
        val success = if (withIntermediateDirectories) file.mkdirs() else file.mkdir()
        if (!success && !file.isDirectory) {
            throw IOException("Failed to create directory: $file")
        }
    }

    override fun copyItem(src: File, dst: File) {
        if (src.isDirectory) {
            src.copyRecursively(dst, overwrite = false)
        } else {
            src.copyTo(dst, overwrite = false)
        }
    }

    override fun removeItem(file: File) {
        if (file.isDirectory) {
            if (!file.deleteRecursively()) {
                throw IOException("Failed to delete directory: $file")
            }
        } else {
            if (!file.delete() && file.exists()) {
                throw IOException("Failed to delete file: $file")
            }
        }
    }

    override fun moveItem(src: File, dst: File) {
        if (!src.renameTo(dst)) {
            // Fallback: copy then delete (handles cross-filesystem moves).
            copyItem(src, dst)
            removeItem(src)
        }
    }

    override fun contentsOfDirectory(directory: File): List<File> =
        directory.listFiles()?.toList()
            ?: throw IOException("Failed to list contents of directory: $directory")

    override fun fileSize(file: File): Long? =
        if (file.isFile) file.length() else null

    override fun writeToFile(file: File, content: String) {
        file.writeText(content, Charsets.UTF_8)
    }
}
