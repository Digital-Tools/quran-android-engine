package com.quranengine.domain.translationservice

import com.quranengine.core.system.FileSystem
import com.quranengine.data.translation.ActiveTranslationsPersistence
import com.quranengine.model.qurantext.Translation
import java.io.File

class TranslationDeleter(
    private val persistence: ActiveTranslationsPersistence,
    private val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val fileSystem: FileSystem,
    private val baseDir: File,
) {
    suspend fun deleteTranslation(translation: Translation): Translation {
        // Update selected translations
        selectedTranslationsPreferences.remove(translation.id)

        // Delete from disk
        for (path in translation.localFiles) {
            try {
                fileSystem.removeItem(File(baseDir, path))
            } catch (_: Exception) {
                // Ignore errors for individual file deletions
            }
        }

        val updated = translation.copy(installedVersion = null)
        persistence.update(updated)
        return updated
    }
}
