package com.quranengine.model.qurantext

data class Translation(
    val id: Int,
    val displayName: String,
    val translator: String?,
    val translatorForeign: String?,
    val fileURL: String,
    val fileName: String,
    val languageCode: String,
    val version: Int,
    val installedVersion: Int? = null
) : Comparable<Translation> {

    val isDownloaded: Boolean get() = installedVersion != null

    val needsUpgrade: Boolean get() = installedVersion != version

    val translatorDisplayName: String?
        get() = translatorForeign ?: translator

    val translationName: String
        get() = translatorDisplayName ?: displayName

    override fun compareTo(other: Translation): Int {
        val nameCompare = displayName.compareTo(other.displayName, ignoreCase = true)
        if (nameCompare != 0) return nameCompare
        return translationName.compareTo(other.translationName, ignoreCase = true)
    }

    companion object {
        private const val COMPRESSED_FILE_EXTENSION = "zip"
        private const val TRANSLATIONS_PATH = "translations"
    }

    val localPath: String
        get() = "$TRANSLATIONS_PATH/$fileName"

    val localFiles: List<String>
        get() {
            val unprocessed = unprocessedFileName
            return if (unprocessed != fileName) {
                listOf("$TRANSLATIONS_PATH/$fileName", "$TRANSLATIONS_PATH/$unprocessed")
            } else {
                listOf("$TRANSLATIONS_PATH/$fileName")
            }
        }

    val unprocessedLocalPath: String
        get() = "$TRANSLATIONS_PATH/$unprocessedFileName"

    val isUnprocessedFileZip: Boolean
        get() = unprocessedFileName.endsWith(COMPRESSED_FILE_EXTENSION)

    internal val unprocessedFileName: String
        get() {
            if (fileURL.endsWith(COMPRESSED_FILE_EXTENSION)) {
                val baseName = fileName.substringBeforeLast('.')
                return "$baseName.$COMPRESSED_FILE_EXTENSION"
            }
            return fileName
        }
}

fun isLocalTranslationPath(path: String): Boolean =
    path.startsWith("translations/")
