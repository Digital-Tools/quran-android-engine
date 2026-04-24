package com.quranengine.core.system

import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream

/**
 * Abstraction for unzipping archives.
 * Port of Swift's `Zipper` protocol.
 */
interface Zipper {
    fun unzipFile(zipFile: File, destination: File, overwrite: Boolean, password: String? = null)
}

/**
 * Default implementation using [ZipInputStream].
 *
 * Note: [ZipInputStream] does not support password-protected archives.
 * If password support is needed, consider a library such as Zip4j.
 */
class DefaultZipper : Zipper {

    override fun unzipFile(zipFile: File, destination: File, overwrite: Boolean, password: String?) {
        if (password != null) {
            throw UnsupportedOperationException(
                "Password-protected zips are not supported by the default implementation. Use Zip4j or similar."
            )
        }

        zipFile.inputStream().buffered().use { fis ->
            ZipInputStream(fis).use { zis ->
                generateSequence { zis.nextEntry }.forEach { entry ->
                    val outFile = File(destination, entry.name)

                    // Zip-slip guard
                    val destCanonical = destination.canonicalPath
                    val outCanonical = outFile.canonicalPath
                    if (!outCanonical.startsWith(destCanonical + File.separator) &&
                        outCanonical != destCanonical
                    ) {
                        throw IOException("Zip entry is outside of the target dir: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        if (!overwrite && outFile.exists()) {
                            zis.closeEntry()
                            return@forEach
                        }
                        outFile.outputStream().use { out -> zis.copyTo(out) }
                    }
                    zis.closeEntry()
                }
            }
        }
    }
}
