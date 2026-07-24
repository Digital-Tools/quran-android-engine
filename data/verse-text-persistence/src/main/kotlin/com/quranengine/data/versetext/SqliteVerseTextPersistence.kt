package com.quranengine.data.versetext

import com.quranengine.data.sqlite.ReadOnlyDatabase
import com.quranengine.data.sqlite.PersistenceError
import com.quranengine.data.sqlite.firstOrNull
import com.quranengine.data.sqlite.getIntColumn
import com.quranengine.data.sqlite.getStringColumn
import com.quranengine.data.sqlite.map
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Quran
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads Quran arabic text from a pre-bundled SQLite database.
 * Port of Swift's GRDBQuranVerseTextPersistence.
 */
class SqliteVerseTextPersistence(
    private val db: ReadOnlyDatabase,
    private val textTable: String = "arabic_text"
) : VerseTextPersistence {

    enum class Mode(val tableName: String) {
        Arabic("arabic_text"),
        Share("share_text")
    }

    constructor(db: ReadOnlyDatabase, mode: Mode) : this(db, mode.tableName)

    override suspend fun textForVerse(verse: AyahNumber): String = withContext(Dispatchers.IO) {
        db.read { database ->
            val sura = verse.sura.suraNumber
            val ayah = verse.ayah
            // Try to search using integer and string ayah/sura (needed by some translation sqlite files)
            val cursor = database.rawQuery(
                """
                SELECT text FROM $textTable
                WHERE (ayah = ? OR ayah = ?) AND (sura = ? OR sura = ?)
                """.trimIndent(),
                arrayOf(ayah, ayah.toString(), sura, sura.toString())
            )
            cursor.firstOrNull { it.getStringColumn("text") }
                ?: throw PersistenceError.General("Cannot find any records for verse '$verse'")
        }
    }

    override suspend fun textForVerses(verses: List<AyahNumber>): Map<AyahNumber, String> =
        withContext(Dispatchers.IO) {
            db.read { database ->
                val result = mutableMapOf<AyahNumber, String>()
                for (verse in verses) {
                    val sura = verse.sura.suraNumber
                    val ayah = verse.ayah
                    val cursor = database.rawQuery(
                        """
                        SELECT text FROM $textTable
                        WHERE (ayah = ? OR ayah = ?) AND (sura = ? OR sura = ?)
                        """.trimIndent(),
                        arrayOf(ayah, ayah.toString(), sura, sura.toString())
                    )
                    cursor.firstOrNull { it.getStringColumn("text") }?.let {
                        result[verse] = it
                    }
                }
                result
            }
        }

    override suspend fun autocomplete(term: String): List<String> = withContext(Dispatchers.IO) {
        db.read { database ->
            val cursor = database.rawQuery(
                "SELECT text FROM $SEARCH_TABLE WHERE text MATCH ? || '*' LIMIT 100",
                arrayOf(term)
            )
            cursor.map { it.getStringColumn("text") }
        }
    }

    override suspend fun search(term: String, quran: Quran): List<SearchResult> =
        withContext(Dispatchers.IO) {
            db.read { database ->
                val cursor = database.rawQuery(
                    "SELECT text, sura, ayah FROM $SEARCH_TABLE WHERE text LIKE '%' || ? || '%'",
                    arrayOf(term)
                )
                cursor.map { c ->
                    val text = c.getStringColumn("text")
                    val sura = c.getIntColumn("sura")
                    val ayah = c.getIntColumn("ayah")
                    val verse = AyahNumber(quran, sura, ayah)
                        ?: throw PersistenceError.General("Invalid verse sura=$sura ayah=$ayah")
                    SearchResult(verse, text)
                }
            }
        }

    companion object {
        private const val SEARCH_TABLE = "verses"
    }
}
