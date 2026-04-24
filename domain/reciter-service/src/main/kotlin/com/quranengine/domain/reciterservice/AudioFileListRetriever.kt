package com.quranengine.domain.reciterservice

import com.quranengine.model.quranaudio.AudioType
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.databaseRemoteURL
import com.quranengine.model.quranaudio.localURL
import com.quranengine.model.quranaudio.localZipPath
import com.quranengine.model.quranaudio.remoteURL
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Sura
import com.quranengine.model.qurankit.arrayTo

data class ReciterAudioFile(
    val remote: String,
    val local: String,
    val sura: Sura? = null,
)

private interface AudioFileListRetriever {
    fun get(reciter: Reciter, from: AyahNumber, to: AyahNumber): List<ReciterAudioFile>
}

private class GaplessAudioFileListRetriever(private val baseURL: String) : AudioFileListRetriever {

    override fun get(reciter: Reciter, from: AyahNumber, to: AyahNumber): List<ReciterAudioFile> {
        val databaseRemoteURL = reciter.databaseRemoteURL(baseURL)
            ?: error("Unsupported reciter type gapped. Only gapless reciters can be used here.")
        val localZipPath = reciter.localZipPath
            ?: error("Unsupported reciter type gapped. Only gapless reciters can be used here.")

        val dbFile = ReciterAudioFile(remote = databaseRemoteURL, local = localZipPath)

        val files = linkedSetOf<ReciterAudioFile>()
        for (sura in from.sura.arrayTo(to.sura)) {
            files.add(
                ReciterAudioFile(
                    remote = reciter.remoteURL(sura),
                    local = reciter.localURL(sura),
                    sura = sura,
                )
            )
        }
        return files.toList() + dbFile
    }
}

private class GappedAudioFileListRetriever : AudioFileListRetriever {

    override fun get(reciter: Reciter, from: AyahNumber, to: AyahNumber): List<ReciterAudioFile> {
        require(reciter.audioType is AudioType.Gapped) {
            "Unsupported reciter type gapless. Only gapped reciters can be used here."
        }

        val files = linkedSetOf<ReciterAudioFile>()

        // Add bismillah for all gapped audio
        val firstVerse = from.quran.firstVerse
        files.add(createRequestInfo(reciter, firstVerse))

        for (ayah in from.arrayTo(to)) {
            files.add(createRequestInfo(reciter, ayah))
        }
        return files.toList()
    }

    private fun createRequestInfo(reciter: Reciter, ayah: AyahNumber): ReciterAudioFile =
        ReciterAudioFile(
            remote = reciter.remoteURL(ayah),
            local = reciter.localURL(ayah),
            sura = ayah.sura,
        )
}

fun Reciter.audioFiles(baseURL: String, from: AyahNumber, to: AyahNumber): List<ReciterAudioFile> {
    val retriever: AudioFileListRetriever = when (audioType) {
        is AudioType.Gapped -> GappedAudioFileListRetriever()
        is AudioType.Gapless -> GaplessAudioFileListRetriever(baseURL)
    }
    return retriever.get(this, from, to)
}
