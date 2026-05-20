package com.quranengine.domain.translationservice

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.SystemBundle
import com.quranengine.data.translation.ActiveTranslationsPersistence
import com.quranengine.model.qurantext.Translation
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Installs translations that are bundled as assets into the application's
 * local storage on first run. This is a one-time setup operation — subsequent
 * calls skip translations that are already registered and whose DB file exists.
 *
 * The bundled manifest is at `assets/translations/bundled_translations.json`.
 * Each translation's ZIP is at `assets/translations/<baseName>.zip`.
 */
class TranslationAssetsInstaller(
    private val systemBundle: SystemBundle,
    private val fileSystem: FileSystem,
    private val persistence: ActiveTranslationsPersistence,
    private val unzipper: TranslationUnzipper,
    private val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val baseDir: File,
) {

    suspend fun ensureInstalled() = withContext(Dispatchers.IO) {
        val manifestPath = "translations/bundled_translations.json"
        val jsonStream = systemBundle.openAsset(manifestPath) ?: run {
            Timber.i("TranslationAssets: manifest not found at %s", manifestPath)
            return@withContext
        }

        val bundledTranslations = try {
            JSONTranslationsParser().parse(jsonStream.use { it.readBytes() })
        } catch (e: Exception) {
            Timber.e(e, "TranslationAssets: failed to parse manifest")
            return@withContext
        }

        // Load existing registrations once — avoid redundant DB round trips per loop iteration.
        val alreadyRegistered = persistence.retrieveAll().associateBy { it.fileName }

        val translationsDir = File(baseDir, "translations")
        if (!translationsDir.exists()) {
            fileSystem.createDirectory(translationsDir, withIntermediateDirectories = true)
        }

        for (translation in bundledTranslations) {
            installIfNeeded(translation, alreadyRegistered, translationsDir)
        }

        // Auto-select English (Sahih International, id=98) as the default if the user
        // has never picked any translation yet.
        if (selectedTranslationsPreferences.selectedTranslationIds.isEmpty()) {
            val englishId = bundledTranslations.firstOrNull { it.id == 98 }?.id
            if (englishId != null) {
                val dbFile = File(baseDir, bundledTranslations.first { it.id == englishId }.localPath)
                if (dbFile.isFile) {
                    selectedTranslationsPreferences.select(englishId)
                    Timber.i("TranslationAssets: auto-selected English (id=%d)", englishId)
                }
            }
        }
    }

    private suspend fun installIfNeeded(
        translation: Translation,
        alreadyRegistered: Map<String, Translation>,
        translationsDir: File,
    ) {
        val dbFile = File(baseDir, translation.localPath)

        // If already registered AND the DB file is on disk — nothing to do.
        if (alreadyRegistered.containsKey(translation.fileName) && dbFile.isFile) {
            Timber.d("TranslationAssets: already installed %s, skipping", translation.fileName)
            return
        }

        // DB is missing — try to extract the bundled ZIP.
        if (!dbFile.isFile) {
            extractBundledZip(translation, translationsDir)
        }

        // Register in persistence if the DB file is now present on disk.
        if (dbFile.isFile) {
            val registered = translation.copy(installedVersion = translation.version)
            persistence.insert(registered)
            Timber.i("TranslationAssets: registered %s (v%d)", translation.fileName, translation.version)
        } else {
            Timber.w("TranslationAssets: DB still missing after extraction for %s", translation.fileName)
        }
    }

    /**
     * Copies the bundled ZIP asset to [translationsDir], lets [TranslationUnzipper]
     * extract it (which also deletes the ZIP afterwards per its contract), so that
     * the final `.db` file ends up at `baseDir/translations/<fileName>`.
     */
    private fun extractBundledZip(translation: Translation, translationsDir: File) {
        // The bundled asset ZIP name matches the un-zipped file's base name + ".zip".
        // e.g. `quran.ensi.zip` for `quran.ensi.db`.
        val zipBaseName = translation.fileName.substringBeforeLast('.') + ".zip"
        val assetZipPath = "translations/$zipBaseName"

        val zipInputStream = systemBundle.openAsset(assetZipPath)
        if (zipInputStream == null) {
            Timber.w("TranslationAssets: bundled ZIP not found at %s", assetZipPath)
            return
        }

        // The destination path must match what TranslationUnzipper expects:
        // File(baseDir, translation.unprocessedLocalPath)
        // = File(baseDir, "translations/<unprocessedFileName>")
        val destZipFile = File(baseDir, translation.unprocessedLocalPath)

        try {
            zipInputStream.use { input ->
                destZipFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Timber.d("TranslationAssets: copied ZIP to %s", destZipFile.path)

            // unzipIfNeeded will extract the ZIP and then delete it.
            // We pass installedVersion=null so its early-return guard (version == installedVersion) is skipped.
            unzipper.unzipIfNeeded(translation.copy(installedVersion = null), baseDir)
        } catch (e: Exception) {
            Timber.e(e, "TranslationAssets: failed to extract %s", translation.fileName)
            // Clean up a partially-written ZIP so it doesn't confuse future runs.
            if (destZipFile.exists()) {
                try { destZipFile.delete() } catch (_: Exception) { }
            }
        }
    }
}
