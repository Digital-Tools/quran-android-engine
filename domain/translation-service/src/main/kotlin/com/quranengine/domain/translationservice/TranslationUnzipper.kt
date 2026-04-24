package com.quranengine.domain.translationservice

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.Zipper
import com.quranengine.core.utilities.features.attempt
import com.quranengine.model.qurantext.Translation
import timber.log.Timber
import java.io.File

interface TranslationUnzipper {
    fun unzipIfNeeded(translation: Translation, baseDir: File)
}

class DefaultTranslationUnzipper(
    private val zipper: Zipper,
    private val fileSystem: FileSystem,
) : TranslationUnzipper {

    override fun unzipIfNeeded(translation: Translation, baseDir: File) {
        // Already on the latest version — nothing to do.
        if (translation.version == translation.installedVersion) return

        if (translation.isUnprocessedFileZip) {
            val zipFile = File(baseDir, translation.unprocessedLocalPath)
            if (zipFile.exists()) {
                try {
                    attempt(times = 3) {
                        zipper.unzipFile(
                            zipFile = zipFile,
                            destination = zipFile.parentFile!!,
                            overwrite = true,
                        )
                    }
                } finally {
                    // Delete the zip in both cases (success or failure)
                    // success: to save space
                    // failure: to redownload it again
                    try {
                        fileSystem.removeItem(zipFile)
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to remove zip file: $zipFile")
                    }
                }
            }
        }
    }
}
