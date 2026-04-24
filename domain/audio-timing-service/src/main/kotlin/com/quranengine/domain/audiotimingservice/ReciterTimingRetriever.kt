package com.quranengine.domain.audiotimingservice

import com.quranengine.data.audiotiming.AyahTimingPersistence
import com.quranengine.model.quranaudio.RangeTiming
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.SuraTiming
import com.quranengine.model.quranaudio.Timing
import com.quranengine.model.quranaudio.localDatabasePath
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Sura
import com.quranengine.model.qurankit.arrayTo
import timber.log.Timber

class ReciterTimingRetriever(
    private val persistenceFactory: (String) -> AyahTimingPersistence,
) {
    suspend fun timing(
        reciter: Reciter,
        from: AyahNumber,
        to: AyahNumber,
    ): RangeTiming {
        val suras = from.sura.arrayTo(to.sura)
        val timings = retrieveTiming(reciter, suras)

        val endTime = getEndTime(timings, from, to)
        val filteredTimings = filteredTimings(timings, from, to)

        return RangeTiming(timings = filteredTimings, endTime = endTime)
    }

    private suspend fun retrieveTiming(
        reciter: Reciter,
        suras: List<Sura>,
    ): Map<Sura, SuraTiming> {
        val filePath = reciter.localDatabasePath
            ?: error("Gapped reciters are not supported.")
        val persistence = persistenceFactory(filePath)

        val result = mutableMapOf<Sura, SuraTiming>()
        for (sura in suras) {
            val timings = persistence.getOrderedTimingForSura(startAyah = sura.firstVerse)
            result[sura] = timings
        }
        return result
    }

    private fun getEndTime(
        timings: Map<Sura, SuraTiming>,
        from: AyahNumber,
        to: AyahNumber,
    ): Timing? {
        val lastSuraTimings = timings[to.sura]!!
        // end is the last verse in the sura
        if (lastSuraTimings.verses.lastOrNull()?.ayah == to) {
            return lastSuraTimings.endTime
        }
        val endIndex = lastSuraTimings.verses.indexOfFirst { it.ayah == to }
        if (endIndex >= 0 && endIndex + 1 < lastSuraTimings.verses.size) {
            return lastSuraTimings.verses[endIndex + 1].time
        }
        Timber.e("lastSuraTimings doesn't have the end verse")
        return null
    }

    private fun filteredTimings(
        timings: Map<Sura, SuraTiming>,
        from: AyahNumber,
        to: AyahNumber,
    ): Map<Sura, SuraTiming> {
        val ayahSet = from.arrayTo(to).toSet()
        val result = mutableMapOf<Sura, SuraTiming>()
        for ((sura, suraTimings) in timings) {
            var endTime: Timing? = null
            if (suraTimings.endTime != null) {
                val lastVerse = suraTimings.verses.lastOrNull()?.ayah
                if (lastVerse != null && lastVerse in ayahSet) {
                    endTime = suraTimings.endTime
                }
            }
            result[sura] = SuraTiming(
                verses = suraTimings.verses.filter { it.ayah in ayahSet },
                endTime = endTime,
            )
        }
        return result
    }
}
