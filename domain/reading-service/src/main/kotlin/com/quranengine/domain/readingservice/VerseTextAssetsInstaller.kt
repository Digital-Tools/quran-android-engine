package com.quranengine.domain.readingservice

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.SystemBundle
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Installs the bundled Arabic Uthmani verse-text database used by translation /
 * tafseer mode (`quran.ar.uthmani.v2.db`). Without this file, inline translation
 * pages cannot load Arabic ayah text.
 */
class VerseTextAssetsInstaller(
    private val systemBundle: SystemBundle,
    private val fileSystem: FileSystem,
    private val databasesDir: File,
) {

    suspend fun ensureInstalled() = withContext(Dispatchers.IO) {
        if (!databasesDir.exists()) {
            fileSystem.createDirectory(databasesDir, withIntermediateDirectories = true)
        }

        val destination = File(databasesDir, DB_FILE_NAME)
        if (destination.isFile && destination.length() > MIN_VALID_BYTES) {
            return@withContext
        }

        val assetPath = "databases/$DB_FILE_NAME"
        val input = systemBundle.openAsset(assetPath) ?: run {
            Timber.e("VerseTextAssets: bundled Arabic DB missing at %s", assetPath)
            return@withContext
        }

        val temp = File(databasesDir, ".$DB_FILE_NAME.installing")
        if (temp.exists()) {
            fileSystem.removeItem(temp)
        }

        input.use { source ->
            temp.outputStream().use { target ->
                source.copyTo(target)
            }
        }

        if (destination.exists()) {
            fileSystem.removeItem(destination)
        }
        fileSystem.moveItem(temp, destination)
        Timber.i("VerseTextAssets: installed %s (%d bytes)", destination.name, destination.length())
    }

    companion object {
        private const val DB_FILE_NAME = "quran.ar.uthmani.v2.db"
        private const val MIN_VALID_BYTES = 1_000_000L
    }
}
