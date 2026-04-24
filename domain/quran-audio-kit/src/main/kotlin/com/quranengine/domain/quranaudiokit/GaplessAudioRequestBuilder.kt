package com.quranengine.domain.quranaudiokit

import android.net.Uri
import com.quranengine.core.audioplayer.AudioFile
import com.quranengine.core.audioplayer.AudioFrame
import com.quranengine.core.audioplayer.AudioRequest
import com.quranengine.core.audioplayer.PlayerItemInfo
import com.quranengine.core.audioplayer.Runs
import com.quranengine.core.localization.Localizer
import com.quranengine.core.localization.Table
import com.quranengine.domain.audiotimingservice.ReciterTimingRetriever
import com.quranengine.domain.reciterservice.localizedName
import com.quranengine.model.quranaudio.AudioType
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.localURL
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Sura
import java.io.File

internal class GaplessAudioRequest(
    private val request: AudioRequest,
    private val ayahs: List<List<AyahNumber>>,
    private val reciter: Reciter,
    private val localizer: Localizer,
) : QuranAudioRequest {

    override fun getRequest(): AudioRequest = request

    override fun getAyahNumberFrom(fileIndex: Int, frameIndex: Int): AyahNumber =
        ayahs[fileIndex][frameIndex]

    override fun getPlayerInfo(fileIndex: Int): PlayerItemInfo {
        val sura = ayahs[fileIndex][0].sura
        return PlayerItemInfo(
            title = localizer.l("sura_${sura.suraNumber}", table = Table.SURAS),
            artist = reciter.localizedName(localizer),
        )
    }
}

class GaplessAudioRequestBuilder(
    private val timingRetriever: ReciterTimingRetriever,
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
        val range = timingRetriever.timing(reciter, from, to)
        val surasPaths = urlsToPlay(reciter, range.timings.keys)

        val files = mutableListOf<AudioFile>()
        val ayahs = mutableListOf<List<AyahNumber>>()

        for ((path, sura) in surasPaths) {
            val suraTimings = range.timings[sura]!!

            val frames = mutableListOf<AudioFrame>()
            val fileAyahs = mutableListOf<AyahNumber>()

            for ((offset, verse) in suraTimings.verses.withIndex()) {
                val endTime = if (offset == suraTimings.verses.size - 1) {
                    suraTimings.endTime
                } else {
                    null
                }

                var startTimeSeconds = verse.time.seconds

                // Do not include the basmalah when the first verse is repeated
                if (offset == 0 && verse.ayah.ayah == 1 && (requestRuns == Runs.ONE || ayahs.isNotEmpty())) {
                    startTimeSeconds = 0.0
                }

                val frame = AudioFrame(
                    startTime = startTimeSeconds,
                    endTime = endTime?.seconds,
                )
                frames.add(frame)
                fileAyahs.add(verse.ayah)
            }
            files.add(AudioFile(uri = Uri.fromFile(File(baseDir, path)), frames = frames))
            ayahs.add(fileAyahs)
        }

        val request = AudioRequest(
            files = files,
            endTime = range.endTime?.seconds,
            frameRuns = frameRuns,
            requestRuns = requestRuns,
        )
        return GaplessAudioRequest(request, ayahs, reciter, localizer)
    }

    private fun urlsToPlay(
        reciter: Reciter,
        suras: Collection<Sura>,
    ): List<Pair<String, Sura>> {
        require(reciter.audioType is AudioType.Gapless) {
            "Unsupported reciter type gapped. Only gapless reciters can be played here."
        }

        return suras.sorted().map { sura ->
            reciter.localURL(sura) to sura
        }
    }
}
