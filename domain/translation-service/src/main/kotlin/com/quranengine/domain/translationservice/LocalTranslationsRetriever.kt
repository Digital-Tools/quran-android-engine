package com.quranengine.domain.translationservice

import com.quranengine.core.system.FileSystem
import com.quranengine.data.translation.ActiveTranslationsPersistence
import com.quranengine.model.qurantext.Translation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class LocalTranslationsRetriever(
    private val persistence: ActiveTranslationsPersistence,
    private val versionUpdater: TranslationsVersionUpdater,
) {
    suspend fun getLocalTranslations(): List<Translation> {
        val translations = persistence.retrieveAll()
        val originalOrder = translations.map { it.id }

        val updated = coroutineScope {
            translations.map { translation ->
                async { versionUpdater.updateInstalledVersion(translation) }
            }.awaitAll()
        }

        // Preserve original ordering
        val byId = updated.associateBy { it.id }
        return originalOrder.mapNotNull { byId[it] }
    }
}
