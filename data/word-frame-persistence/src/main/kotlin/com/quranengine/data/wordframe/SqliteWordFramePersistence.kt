package com.quranengine.data.wordframe

import com.quranengine.data.sqlite.ReadOnlyDatabase
import com.quranengine.data.sqlite.getIntColumn
import com.quranengine.data.sqlite.map
import com.quranengine.model.qurangeometry.AyahNumberLocation
import com.quranengine.model.qurangeometry.SuraHeaderLocation
import com.quranengine.model.qurangeometry.WordFrame
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Page
import com.quranengine.model.qurankit.Sura
import com.quranengine.model.qurankit.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads word frame / glyph geometry from a pre-bundled SQLite database.
 * Port of Swift's GRDBWordFramePersistence.
 */
class SqliteWordFramePersistence(
    private val db: ReadOnlyDatabase
) : WordFramePersistence {

    override suspend fun wordFrameCollectionForPage(page: Page): List<WordFrame> =
        withContext(Dispatchers.IO) {
            db.read { database ->
                val cursor = database.rawQuery(
                    """
                    SELECT sura_number, ayah_number, line_number, position, min_x, max_x, min_y, max_y
                    FROM glyphs WHERE page_number = ?
                    """.trimIndent(),
                    arrayOf(page.pageNumber.toString())
                )
                cursor.map { c ->
                    val sura = c.getIntColumn("sura_number")
                    val ayah = c.getIntColumn("ayah_number")
                    val verse = AyahNumber(page.quran, sura, ayah)!!
                    WordFrame(
                        line = c.getIntColumn("line_number"),
                        word = Word(verse, c.getIntColumn("position")),
                        minX = c.getIntColumn("min_x"),
                        maxX = c.getIntColumn("max_x"),
                        minY = c.getIntColumn("min_y"),
                        maxY = c.getIntColumn("max_y")
                    )
                }
            }
        }

    override suspend fun suraHeaders(page: Page): List<SuraHeaderLocation> =
        withContext(Dispatchers.IO) {
            db.read { database ->
                val cursor = database.rawQuery(
                    "SELECT sura_number, x, y, width, height FROM sura_headers WHERE page_number = ?",
                    arrayOf(page.pageNumber.toString())
                )
                cursor.map { c ->
                    val suraNumber = c.getIntColumn("sura_number")
                    val sura = Sura(page.quran, suraNumber)!!
                    SuraHeaderLocation(
                        sura = sura,
                        x = c.getIntColumn("x"),
                        y = c.getIntColumn("y"),
                        width = c.getIntColumn("width"),
                        height = c.getIntColumn("height")
                    )
                }
            }
        }

    override suspend fun ayahNumbers(page: Page): List<AyahNumberLocation> =
        withContext(Dispatchers.IO) {
            db.read { database ->
                val cursor = database.rawQuery(
                    "SELECT sura_number, ayah_number, x, y FROM ayah_markers WHERE page_number = ?",
                    arrayOf(page.pageNumber.toString())
                )
                cursor.map { c ->
                    val sura = c.getIntColumn("sura_number")
                    val ayah = c.getIntColumn("ayah_number")
                    val verse = AyahNumber(page.quran, sura, ayah)!!
                    AyahNumberLocation(
                        ayah = verse,
                        x = c.getIntColumn("x"),
                        y = c.getIntColumn("y")
                    )
                }
            }
        }
}
