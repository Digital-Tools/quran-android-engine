package com.quranengine.domain.quranaudiokit

import android.net.Uri
import com.quranengine.core.audioplayer.AudioFile
import com.quranengine.core.audioplayer.AudioFrame
import com.quranengine.core.audioplayer.AudioRequest
import com.quranengine.core.audioplayer.PlayerItemInfo
import com.quranengine.core.audioplayer.Runs
import com.quranengine.core.localization.Localizer
import com.quranengine.core.localization.Table
import com.quranengine.domain.reciterservice.localizedName
import com.quranengine.model.quranaudio.AudioType
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.localURL
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Sura
import com.quranengine.model.qurankit.arrayTo
import java.io.File

internal class GappedAudioRequest(
    private val request: AudioRequest,
    private val ayahs: List<AyahNumber>,
    private val reciter: Reciter,
    private val localizer: Localizer,
) : QuranAudioRequest {

    override fun getRequest(): AudioRequest = request

    override fun getAyahNumberFrom(fileIndex: Int, frameIndex: Int): AyahNumber =
        ayahs[fileIndex]

    override fun getPlayerInfo(fileIndex: Int): PlayerItemInfo {
        val ayah = ayahs[fileIndex]
        return PlayerItemInfo(
            title = localizer.l("sura_${ayah.sura.suraNumber}", table = Table.SURAS),
            artist = reciter.localizedName(localizer),
        )
    }
}

class GappedAudioRequestBuilder(
    private val localizer: Localizer,
    private val baseDir: File,
) : QuranAudioRequestBuilder {

    override suspend fun buildRequest(
        reciter: Reciter,
        from: AyahNumber,
        to: AyahNumber,
        frameRuns: Runs,
        requestRuns: Runs,
    ): QuranAudioRequest {
        val (urls, ayahs) = urlsToPlay(reciter, from, to, requestRuns)
        val files = urls.map { path ->
            AudioFile(
                uri = Uri.fromFile(File(baseDir, path)),
                frames = listOf(AudioFrame(startTime = 0.0, endTime = null)),
            )
        }
        val request = AudioRequest(
            files = files,
            endTime = null,
            frameRuns = frameRuns,
            requestRuns = requestRuns,
        )
        return GappedAudioRequest(request, ayahs, reciter, localizer)
    }

    private fun urlsToPlay(
        reciter: Reciter,
        from: AyahNumber,
        to: AyahNumber,
        requestRuns: Runs,
    ): Pair<List<String>, List<AyahNumber>> {
        require(reciter.audioType is AudioType.Gapped) {
            "Unsupported reciter type gapless. Only gapped reciters can be downloaded here."
        }

        val urls = mutableListOf<String>()
        val ayahs = mutableListOf<AyahNumber>()
        val verses = from.arrayTo(to)
        val surasDictionary = verses.groupBy { it.sura }

        for (sura in surasDictionary.keys.sorted()) {
            val suraVerses = surasDictionary[sura] ?: emptyList()

            // Add besmAllah for all except Al-Fatihah and At-Tawbah
            if ((requestRuns == Runs.ONE || ayahs.isNotEmpty()) &&
                sura.startsWithBesmAllah &&
                suraVerses[0] == sura.firstVerse
            ) {
                urls.add(reciter.localURL(from.quran.firstVerse))
                ayahs.add(suraVerses[0])
            }
            for (verse in suraVerses) {
                urls.add(reciter.localURL(verse))
                ayahs.add(verse)
            }
        }
        return urls to ayahs
    }
}
