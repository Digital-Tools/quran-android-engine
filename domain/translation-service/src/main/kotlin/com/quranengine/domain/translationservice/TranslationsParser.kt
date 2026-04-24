package com.quranengine.domain.translationservice

import com.quranengine.model.qurantext.Translation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface TranslationsParser {
    fun parse(data: ByteArray): List<Translation>
}

class JSONTranslationsParser : TranslationsParser {

    private val json = Json { ignoreUnknownKeys = true }

    override fun parse(data: ByteArray): List<Translation> {
        val response = json.decodeFromString<TranslationsResponse>(data.decodeToString())
        return response.data.map { it.toTranslation() }
    }
}

@Serializable
internal data class TranslationsResponse(
    val data: List<TranslationResponse>,
)

@Serializable
internal data class TranslationResponse(
    val id: Int,
    @SerialName("displayName") val displayName: String,
    val translator: String? = null,
    val translatorForeign: String? = null,
    val fileUrl: String,
    val fileName: String,
    val languageCode: String,
    val currentVersion: Int,
) {
    fun toTranslation(): Translation = Translation(
        id = id,
        displayName = displayName,
        translator = translator,
        translatorForeign = translatorForeign,
        fileURL = fileUrl,
        fileName = fileName,
        languageCode = languageCode,
        version = currentVersion,
        installedVersion = null,
    )
}
