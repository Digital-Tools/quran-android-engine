package com.quranengine.core.utilities.features

import java.io.File

/**
 * Represents a file path relative to a base directory (e.g., app internal storage).
 * Port of Swift's RelativeFilePath.
 */
data class RelativeFilePath(
    val path: String,
    val isDirectory: Boolean = false,
) {
    /** Resolves this relative path against a [baseDir]. */
    fun file(baseDir: File): File = File(baseDir, path)

    /** Checks whether the resolved file exists and is reachable. */
    fun isReachable(baseDir: File): Boolean {
        val f = file(baseDir)
        return if (isDirectory) f.isDirectory else f.exists()
    }

    /** Returns true if [other]'s path starts with this path (i.e., this is a parent). */
    fun isParent(other: RelativeFilePath): Boolean =
        other.path.startsWith(if (path.endsWith("/")) path else "$path/")

    val lastPathComponent: String
        get() = File(path).name

    fun appendingPathComponent(component: String): RelativeFilePath =
        RelativeFilePath(File(path, component).path, isDirectory = false)

    fun appendingPathComponent(component: String, isDirectory: Boolean): RelativeFilePath =
        RelativeFilePath(File(path, component).path, isDirectory = isDirectory)

    fun appendingPathExtension(extension: String): RelativeFilePath =
        RelativeFilePath("$path.$extension", isDirectory = false)

    fun deletingLastPathComponent(): RelativeFilePath {
        val parent = File(path).parent ?: ""
        return RelativeFilePath(parent, isDirectory = true)
    }

    override fun toString(): String = path
}
