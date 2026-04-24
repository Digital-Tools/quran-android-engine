package com.quranengine.domain.reciterservice

import com.quranengine.core.localization.Localizer
import com.quranengine.core.system.FileSystem
import com.quranengine.model.quranaudio.AudioType
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.quranaudio.localFolder
import java.io.File
import java.io.InputStream

/**
 * Provides the bundled reciters JSON data as an [InputStream].
 *
 * On Android, the typical implementation opens an asset file via `Context.assets.open(...)`.
 * For testing, a simple in-memory implementation can be used.
 */
fun interface ReciterDataSource {
    fun open(): InputStream
}

class ReciterDataRetriever(
    private val dataSource: ReciterDataSource,
    private val fileSystem: FileSystem,
    private val baseDir: File,
    private val localizer: Localizer,
) {

    suspend fun getReciters(): List<Reciter> {
        val json = dataSource.open().bufferedReader().use { it.readText() }
        val reciters = parseReciters(json)

        return reciters
            .filter { reciter ->
                !reciter.hasGaplessAlternative ||
                    fileSystem.fileExists(File(baseDir, reciter.localFolder()))
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.localizedName(localizer) })
    }

    companion object {
        internal fun parseReciters(json: String): List<Reciter> {
            // Minimal JSON array parser using org.json which is available on Android.
            val array = org.json.JSONArray(json)
            return (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                Reciter(
                    id = obj.getInt("id"),
                    nameKey = obj.getString("name"),
                    directory = obj.getString("path"),
                    audioURL = obj.getString("url"),
                    audioType = audioType(obj.optString("databaseName", "")),
                    hasGaplessAlternative = obj.getBoolean("hasGaplessAlternative"),
                    category = Reciter.Category.fromValue(obj.getString("category")),
                )
            }
        }

        private fun audioType(db: String): AudioType =
            if (db.isEmpty()) AudioType.Gapped else AudioType.Gapless(databaseName = db)
    }
}
