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
 *
 * When bundled ZIPs are absent (common in embedded Mizan builds), English is
 * downloaded from android.quran.com so translation mode works out of the box.
 */
class TranslationAssetsInstaller(
    private val systemBundle: SystemBundle,
    private val fileSystem: FileSystem,
    private val persistence: ActiveTranslationsPersistence,
    private val unzipper: TranslationUnzipper,
    private val translationsDownloader: TranslationsDownloader,
    private val versionUpdater: TranslationsVersionUpdater,
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
            installIfNeeded(
                translation = translation,
                alreadyRegistered = alreadyRegistered,
                translationsDir = translationsDir,
                allowNetworkDownload = true,
            )
        }

        ensureDefaultEnglishSelected(bundledTranslations, alreadyRegistered, translationsDir)
    }

    private suspend fun ensureDefaultEnglishSelected(
        bundledTranslations: List<Translation>,
        alreadyRegistered: Map<String, Translation>,
        translationsDir: File,
    ) {
        if (selectedTranslationsPreferences.selectedTranslationIds.isNotEmpty()) return

        val english = bundledTranslations.firstOrNull { it.id == ENGLISH_TRANSLATION_ID } ?: return
        val dbFile = File(baseDir, english.localPath)
        if (!isValidTranslationDatabase(dbFile)) {
            removeInvalidTranslationArtifacts(english)
            installIfNeeded(
                translation = english,
                alreadyRegistered = alreadyRegistered,
                translationsDir = translationsDir,
                allowNetworkDownload = true,
            )
        }

        if (isValidTranslationDatabase(File(baseDir, english.localPath))) {
            selectedTranslationsPreferences.select(ENGLISH_TRANSLATION_ID)
            Timber.i("TranslationAssets: auto-selected English (id=%d)", ENGLISH_TRANSLATION_ID)
        }
    }

    private suspend fun installIfNeeded(
        translation: Translation,
        alreadyRegistered: Map<String, Translation>,
        translationsDir: File,
        allowNetworkDownload: Boolean,
    ) {
        val dbFile = File(baseDir, translation.localPath)

        if (!isValidTranslationDatabase(dbFile)) {
            removeInvalidTranslationArtifacts(translation)
        }

        // If already registered AND the DB file is valid — nothing to do.
        if (alreadyRegistered.containsKey(translation.fileName) && isValidTranslationDatabase(dbFile)) {
            Timber.d("TranslationAssets: already installed %s, skipping", translation.fileName)
            return
        }

        // DB is missing or invalid — try to extract the bundled ZIP.
        if (!isValidTranslationDatabase(dbFile)) {
            extractBundledZip(translation, translationsDir)
        }

        if (!isValidTranslationDatabase(dbFile) && allowNetworkDownload) {
            downloadTranslation(translation)
        }

        // Register in persistence if the DB file is now present on disk.
        if (isValidTranslationDatabase(dbFile)) {
            val existing = alreadyRegistered[translation.fileName]
            val registered = if (existing != null) {
                versionUpdater.updateInstalledVersion(existing)
            } else {
                val installed = versionUpdater.updateInstalledVersion(translation)
                persistence.insert(installed)
                installed
            }
            Timber.i(
                "TranslationAssets: registered %s (v%d)",
                registered.fileName,
                registered.installedVersion ?: translation.version,
            )
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

    private suspend fun downloadTranslation(translation: Translation) {
        val downloadTarget = translation.withZipDownloadUrl()
        try {
            Timber.i("TranslationAssets: downloading %s", downloadTarget.fileName)
            val response = translationsDownloader.download(downloadTarget)
            response.awaitCompletion()
            val error = response.getError()
            if (error != null) {
                Timber.w(error, "TranslationAssets: download failed for %s", translation.fileName)
                return
            }
            unzipper.unzipIfNeeded(downloadTarget.copy(installedVersion = null), baseDir)
        } catch (e: Exception) {
            Timber.e(e, "TranslationAssets: download failed for %s", translation.fileName)
        }
    }

    private fun removeInvalidTranslationArtifacts(translation: Translation) {
        translation.localFiles.forEach { relativePath ->
            val file = File(baseDir, relativePath)
            if (file.exists()) {
                try {
                    fileSystem.removeItem(file)
                } catch (error: Exception) {
                    Timber.w(error, "TranslationAssets: failed to remove invalid file %s", file.path)
                }
            }
        }
    }

    private fun isValidTranslationDatabase(file: File): Boolean {
        if (!file.isFile || file.length() < MIN_VALID_DB_BYTES) return false
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(SQLITE_HEADER.size)
                if (input.read(header) != header.size) return false
                header.contentEquals(SQLITE_HEADER)
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun Translation.withZipDownloadUrl(): Translation {
        if (fileURL.endsWith(".zip", ignoreCase = true) || fileURL.contains("ext=zip")) {
            return this
        }
        val separator = if (fileURL.contains("?")) "&" else "?"
        return copy(fileURL = "$fileURL${separator}ext=zip")
    }

    companion object {
        private const val ENGLISH_TRANSLATION_ID = 98
        private const val MIN_VALID_DB_BYTES = 10_000L
        private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray()
    }
}
