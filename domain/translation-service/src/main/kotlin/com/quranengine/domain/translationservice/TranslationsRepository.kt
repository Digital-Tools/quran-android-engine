package com.quranengine.domain.translationservice

import com.quranengine.data.translation.ActiveTranslationsPersistence
import com.quranengine.model.qurantext.Translation
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TranslationsRepository(
    private val networkManager: TranslationNetworkManager,
    private val persistence: ActiveTranslationsPersistence,
) {
    suspend fun downloadAndSyncTranslations() {
        val (local, remote) = coroutineScope {
            val localDeferred = async { persistence.retrieveAll() }
            val remoteDeferred = async { networkManager.getTranslations() }
            localDeferred.await() to remoteDeferred.await()
        }

        val (translations, localMap) = combine(local, remote)
        saveCombined(translations, localMap)
    }

    private fun combine(
        local: List<Translation>,
        remote: List<Translation>,
    ): Pair<List<Translation>, Map<String, Translation>> {
        val localMap = local.associateBy { it.fileName }
        val remainingLocal = localMap.toMutableMap()

        val combined = mutableListOf<Translation>()
        for (r in remote) {
            val existingLocal = remainingLocal.remove(r.fileName)
            if (existingLocal != null) {
                combined.add(r.copy(installedVersion = existingLocal.installedVersion))
            } else {
                combined.add(r)
            }
        }
        // Append any local-only translations that were not in the remote list
        combined.addAll(remainingLocal.values)

        return combined to localMap
    }

    private suspend fun saveCombined(
        translations: List<Translation>,
        localMap: Map<String, Translation>,
    ) {
        for (translation in translations) {
            val old = localMap[translation.fileName]
            if (old != null) {
                persistence.remove(old)
            }
            persistence.insert(translation)
        }
    }
}
