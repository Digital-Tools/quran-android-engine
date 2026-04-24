package com.quranengine.data.versetext

import com.quranengine.data.sqlite.ReadOnlyDatabase
import com.quranengine.data.sqlite.PersistenceError
import com.quranengine.data.sqlite.firstOrNull
import com.quranengine.data.sqlite.getIntColumn
import com.quranengine.data.sqlite.getStringColumn
import com.quranengine.data.sqlite.getStringColumnOrNull
import com.quranengine.data.sqlite.map
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Quran
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads translation text from a downloaded translation SQLite database.
 * Port of Swift's GRDBTranslationVerseTextPersistence.
 *
 * The text column may contain a string or an integer reference (1-based verse index).
 */
class SqliteTranslationVerseTextPersistence(
    private val db: ReadOnlyDatabase,
    private val fileName: String
) : TranslationVerseTextPersistence {

    override suspend fun textForVerse(verse: AyahNumber): TranslationTextPersistenceModel =
        withContext(Dispatchers.IO) {
            db.read { database ->
                val sura = verse.sura.suraNumber
                val ayah = verse.ayah
                val cursor = database.rawQuery(
                    """
                    SELECT text FROM $TEXT_TABLE
                    WHERE (ayah = ? OR ayah = ?) AND (sura = ? OR sura = ?)
                    """.trimIndent(),
                    arrayOf(ayah.toString(), ayah.toString(), sura.toString(), sura.toString())
                )
                cursor.firstOrNull { parseText(it.getStringColumnOrNull("text"), verse.quran) }
                    ?: throw PersistenceError.General("Cannot find any records for verse '$verse'")
            }
        }

    override suspend fun textForVerses(verses: List<AyahNumber>): Map<AyahNumber, TranslationTextPersistenceModel> =
        withContext(Dispatchers.IO) {
            db.read { database ->
                val result = mutableMapOf<AyahNumber, TranslationTextPersistenceModel>()
                for (verse in verses) {
                    val sura = verse.sura.suraNumber
                    val ayah = verse.ayah
                    val cursor = database.rawQuery(
                        """
                        SELECT text FROM $TEXT_TABLE
                        WHERE (ayah = ? OR ayah = ?) AND (sura = ? OR sura = ?)
                        """.trimIndent(),
                        arrayOf(ayah.toString(), ayah.toString(), sura.toString(), sura.toString())
                    )
                    cursor.firstOrNull { parseText(it.getStringColumnOrNull("text"), verse.quran) }
                        ?.let { result[verse] = it }
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

    /**
     * Parses the text column which may be a string or an integer verse reference.
     * Integer references are 1-based indices into [Quran.verses].
     */
    private fun parseText(value: String?, quran: Quran): TranslationTextPersistenceModel {
        if (value == null) {
            throw PersistenceError.General("Text for verse is null. File: $fileName")
        }
        // If the value is a positive integer within the verse count, treat it as a reference
        val verseId = value.toIntOrNull()
        if (verseId != null && verseId > 0 && verseId <= quran.verses.size) {
            val referencedVerse = quran.verses[verseId - 1]
            return TranslationTextPersistenceModel.Reference(referencedVerse)
        }
        return TranslationTextPersistenceModel.Text(value)
    }

    companion object {
        private const val TEXT_TABLE = "verses"
        private const val SEARCH_TABLE = "verses"
    }
}
