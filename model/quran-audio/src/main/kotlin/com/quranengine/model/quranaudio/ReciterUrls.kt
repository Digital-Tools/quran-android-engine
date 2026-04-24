package com.quranengine.model.quranaudio

import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Sura

private const val AUDIO_EXTENSION = "mp3"
private const val DATABASE_REMOTE_EXTENSION = "zip"
private const val DATABASE_LOCAL_EXTENSION = "db"
private const val AUDIO_FILES_PATH = "audio_files"
private const val AUDIO_REMOTE_PATH = "hafs/databases/audio/"

fun Int.as3DigitString(): String {
    val v3 = this / 100
    val m2 = this - v3 * 100
    val v2 = m2 / 10
    val m1 = m2 - v2 * 10
    return "$v3$v2$m1"
}

val Reciter.Companion.audioFilesPath: String
    get() = AUDIO_FILES_PATH

fun Reciter.localFolder(): String =
    "$AUDIO_FILES_PATH/$directory"

fun Reciter.oldLocalFolder(): String =
    directory

val Reciter.localDatabasePath: String?
    get() {
        val gapless = audioType as? AudioType.Gapless ?: return null
        return "${localFolder()}/${gapless.databaseName}.$DATABASE_LOCAL_EXTENSION"
    }

val Reciter.localZipPath: String?
    get() {
        val gapless = audioType as? AudioType.Gapless ?: return null
        return "${localFolder()}/${gapless.databaseName}.$DATABASE_REMOTE_EXTENSION"
    }

fun Reciter.databaseRemoteURL(baseURL: String): String? {
    val gapless = audioType as? AudioType.Gapless ?: return null
    val base = baseURL.trimEnd('/')
    return "$base/$AUDIO_REMOTE_PATH${gapless.databaseName}.$DATABASE_REMOTE_EXTENSION"
}

fun Reciter.remoteURL(sura: Sura): String {
    val fileName = sura.suraNumber.as3DigitString()
    return "${audioURL.trimEnd('/')}/$fileName.$AUDIO_EXTENSION"
}

fun Reciter.localURL(sura: Sura): String {
    val fileName = sura.suraNumber.as3DigitString()
    return "${localFolder()}/$fileName.$AUDIO_EXTENSION"
}

fun Reciter.remoteURL(ayah: AyahNumber): String {
    val fileName = ayah.sura.suraNumber.as3DigitString() + ayah.ayah.as3DigitString()
    return "${audioURL.trimEnd('/')}/$fileName.$AUDIO_EXTENSION"
}

fun Reciter.localURL(ayah: AyahNumber): String {
    val fileName = ayah.sura.suraNumber.as3DigitString() + ayah.ayah.as3DigitString()
    return "${localFolder()}/$fileName.$AUDIO_EXTENSION"
}

fun Reciter.isReciterDirectory(directoryName: String): Boolean =
    directory == directoryName
