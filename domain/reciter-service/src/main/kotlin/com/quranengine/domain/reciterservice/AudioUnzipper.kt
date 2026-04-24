package com.quranengine.domain.reciterservice

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.Zipper
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.localDatabasePath
import com.quranengine.model.quranaudio.localFolder
import com.quranengine.model.quranaudio.localZipPath
import timber.log.Timber
import java.io.File

class AudioUnzipper(
    private val zipper: Zipper,
    private val fileSystem: FileSystem,
    private val baseDir: File,
) {

    suspend fun unzip(reciter: Reciter) {
        val dbPath = reciter.localDatabasePath ?: return
        val zipPath = reciter.localZipPath ?: return

        val dbFile = File(baseDir, dbPath)
        val zipFile = File(baseDir, zipPath)
        val destDir = File(baseDir, reciter.localFolder())

        if (dbFile.exists()) return

        Timber.i("Unzipping audio file. Reciter=%s file=%s.", reciter.nameKey, zipFile)
        try {
            zipper.unzipFile(zipFile, destDir, overwrite = true)
        } catch (e: Exception) {
            Timber.e(e, "Cannot unzip file '%s' to '%s'", zipFile, destDir)
            // Delete the zip and try to re-download next time
            try {
                fileSystem.removeItem(zipFile)
            } catch (_: Exception) {
                // Best effort
            }
            throw e
        }
    }
}
