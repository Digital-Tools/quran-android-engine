package com.quranengine.data.audiotiming

import com.quranengine.data.sqlite.ReadOnlyDatabase
import com.quranengine.data.sqlite.firstOrNull
import com.quranengine.data.sqlite.getIntColumn
import com.quranengine.data.sqlite.getStringColumn
import com.quranengine.data.sqlite.map
import com.quranengine.model.quranaudio.AyahTiming
import com.quranengine.model.quranaudio.SuraTiming
import com.quranengine.model.quranaudio.Timing
import com.quranengine.model.qurankit.AyahNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads ayah timing data from a downloaded audio timing SQLite database.
 * Port of Swift's GRDBAyahTimingPersistence.
 */
class SqliteAyahTimingPersistence(
    private val db: ReadOnlyDatabase
) : AyahTimingPersistence {

    override suspend fun getVersion(): Int = withContext(Dispatchers.IO) {
        db.read { database ->
            val cursor = database.rawQuery(
                "SELECT value FROM properties WHERE property = ?",
                arrayOf("version")
            )
            val value = cursor.firstOrNull { it.getStringColumn("value") }
            value?.toIntOrNull() ?: 1
        }
    }

    override suspend fun getOrderedTimingForSura(startAyah: AyahNumber): SuraTiming =
        withContext(Dispatchers.IO) {
            db.read { database ->
                val cursor = database.rawQuery(
                    "SELECT sura, ayah, time FROM timings WHERE sura = ? AND ayah >= ? ORDER BY ayah",
                    arrayOf(startAyah.sura.suraNumber.toString(), startAyah.ayah.toString())
                )
                val timings = mutableListOf<AyahTiming>()
                var endTime: Timing? = null

                // Process rows without using map() since we need conditional logic per row
                cursor.use { c ->
                    while (c.moveToNext()) {
                        val sura = c.getIntColumn("sura")
                        val ayah = c.getIntColumn("ayah")
                        val time = Timing(c.getIntColumn("time"))

                        if (ayah == END_MARKER) {
                            endTime = time
                        } else {
                            AyahNumber(startAyah.quran, sura, ayah)?.let { verse ->
                                timings.add(AyahTiming(verse, time))
                            }
                        }
                    }
                }
                SuraTiming(timings, endTime)
            }
        }

    companion object {
        private const val END_MARKER = 999
    }
}
