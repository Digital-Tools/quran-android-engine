package com.quranengine.domain.translationservice

import com.quranengine.data.network.NetworkManager
import com.quranengine.model.qurantext.Translation

class TranslationNetworkManager(
    private val networkManager: NetworkManager,
    private val parser: TranslationsParser = JSONTranslationsParser(),
) {
    suspend fun getTranslations(): List<Translation> {
        val data = networkManager.request(PATH, listOf("v" to "5"))
        return parser.parse(data)
    }

    companion object {
        const val PATH = "/data/translations.php"
    }
}
