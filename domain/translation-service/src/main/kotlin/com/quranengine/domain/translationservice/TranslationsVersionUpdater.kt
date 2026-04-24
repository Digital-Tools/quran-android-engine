package com.quranengine.domain.translationservice

import com.quranengine.core.system.FileSystem
import com.quranengine.data.batchdownloader.DownloadBatchResponse
import com.quranengine.data.batchdownloader.DownloadRequest
import com.quranengine.data.translation.ActiveTranslationsPersistence
import com.quranengine.data.versetext.DatabaseVersionPersistence
import com.quranengine.model.qurantext.Translation
import com.quranengine.model.qurantext.isLocalTranslationPath
import timber.log.Timber
import java.io.File

typealias VersionPersistenceFactory = (Translation) -> DatabaseVersionPersistence

class TranslationsVersionUpdater(
    private val persistence: ActiveTranslationsPersistence,
    private val versionPersistenceFactory: VersionPersistenceFactory,
    private val unzipper: TranslationUnzipper,
    private val fileSystem: FileSystem,
    private val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val baseDir: File,
) {
    suspend fun updateInstalledVersion(translation: Translation): Translation {
        unzipper.unzipIfNeeded(translation, baseDir)
        return updateVersion(translation)
    }

    private suspend fun updateVersion(translation: Translation): Translation {
        var updated = translation
        val localFile = File(baseDir, translation.localPath)
        val isReachable = fileSystem.fileExists(localFile)
        val previousInstalledVersion = updated.installedVersion

        // Installed on the latest version & the db file exists
        if (updated.version != updated.installedVersion && isReachable) {
            try {
                val versionPersistence = versionPersistenceFactory(updated)
                val version = versionPersistence.getTextVersion()
                updated = updated.copy(installedVersion = version)
            } catch (e: Exception) {
                // DB file is corrupted
                Timber.w(e, "Error reading version for ${translation.fileName}")
                updated = updated.copy(installedVersion = null)
            }
        } else if (updated.installedVersion != null && !isReachable) {
            updated = updated.copy(installedVersion = null)
        }

        if (previousInstalledVersion != updated.installedVersion) {
            persistence.update(updated)

            // Remove from selected translations if no longer installed
            if (updated.installedVersion == null) {
                val selected = selectedTranslationsPreferences.selectedTranslationIds.toMutableList()
                if (selected.remove(updated.id)) {
                    selectedTranslationsPreferences.selectedTranslationIds = selected
                }
            }
        }
        return updated
    }
}

val DownloadRequest.isTranslation: Boolean
    get() = isLocalTranslationPath(destination.path)

val DownloadBatchResponse.isTranslation: Boolean
    get() = requests.size == 1 && requests.any { it.isTranslation }
