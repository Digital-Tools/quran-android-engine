package com.quranengine.domain.translationservice

import com.quranengine.core.system.FileSystem
import com.quranengine.core.system.SystemBundle
import com.quranengine.data.translation.ActiveTranslationsPersistence
import com.quranengine.model.qurantext.Translation
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class TranslationAssetsInstaller(
    private val systemBundle: SystemBundle,
    private val fileSystem: FileSystem,
    private val persistence: ActiveTranslationsPersistence,
    private val unzipper: TranslationUnzipper,
    private val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val baseDir: File,
) {

    suspend fun ensureInstalled() = withContext(Dispatchers.IO) {
        val assetPath = "translations/bundled_translations.json"
        val jsonStream = systemBundle.openAsset(assetPath) ?: run {
            Timber.i("Translation assets: no bundled_translations.json found")
            return@withContext
        }

        val jsonBytes = jsonStream.use { it.readBytes() }
        val parser = JSONTranslationsParser()
        val translations = try {
            parser.parse(jsonBytes)
        } catch (e: Exception) {
            Timber.e(e, "Translation assets: failed to parse bundled_translations.json")
            return@withContext
        }

        val translationsDir = File(baseDir, "translations")
        if (!translationsDir.exists()) {
            fileSystem.createDirectory(translationsDir, withIntermediateDirectories = true)
        }

        val currentSelected = selectedTranslationsPreferences.selectedTranslationIds.toMutableList()
        var selectedChanged = false

        translations.forEach { translation ->
            val dbFile = File(baseDir, translation.localPath)
            
            // Check if DB file is already installed
            if (!dbFile.isFile) {
                val assetZipPath = "translations/${translation.fileName.substringBeforeLast('.')}.zip"
                val zipInputStream = systemBundle.openAsset(assetZipPath)
                if (zipInputStream != null) {
                    val unprocessedName = translation.unprocessedLocalPath.substringAfterLast('/')
                    val tempZipFile = File(translationsDir, unprocessedName)
                    try {
                        // Copy ZIP from assets to baseDir/translations/
                        zipInputStream.use { input ->
                            tempZipFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        // Unzip the file
                        unzipper.unzipIfNeeded(translation, baseDir)
                        Timber.i("Translation assets: installed bundled translation %s", translation.fileName)
                    } catch (e: Exception) {
                        Timber.e(e, "Translation assets: failed to install translation %s", translation.fileName)
                    } finally {
                        if (tempZipFile.exists()) {
                            tempZipFile.delete()
                        }
                    }
                } else {
                    Timber.w("Translation assets: zipped translation asset %s not found in assets", assetZipPath)
                }
            }

            // Verify db file exists, and if so, register in database
            if (dbFile.isFile) {
                val installedTranslation = translation.copy(installedVersion = translation.version)
                persistence.insert(installedTranslation)

                // Default-select English (98) if nothing is selected yet
                if (selectedTranslationsPreferences.selectedTranslationIds.isEmpty()) {
                    if (translation.id == 98) {
                        if (!currentSelected.contains(translation.id)) {
                            currentSelected.add(translation.id)
                            selectedChanged = true
                        }
                    }
                }
            }
        }

        if (selectedChanged) {
            selectedTranslationsPreferences.selectedTranslationIds = currentSelected
        }
    }
}
