package com.quranengine.data.wordtext

import com.quranengine.data.sqlite.ReadOnlyDatabase
import com.quranengine.data.sqlite.firstOrNull
import com.quranengine.data.sqlite.getStringColumnOrNull
import com.quranengine.model.qurankit.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads word-level translation and transliteration from a pre-bundled SQLite database.
 * Port of Swift's GRDBWordTextPersistence.
 */
class SqliteWordTextPersistence(
    private val db: ReadOnlyDatabase
) : WordTextPersistence {

    override suspend fun translationForWord(word: Word): String? = withContext(Dispatchers.IO) {
        queryWord(word)?.first
    }

    override suspend fun transliterationForWord(word: Word): String? = withContext(Dispatchers.IO) {
        queryWord(word)?.second
    }

    /** Returns (translation, transliteration) pair or null if not found. */
    private fun queryWord(word: Word): Pair<String?, String?>? {
        return db.read { database ->
            val cursor = database.rawQuery(
                "SELECT translation, transliteration FROM words WHERE sura = ? AND ayah = ? AND word_position = ?",
                arrayOf(
                    word.verse.sura.suraNumber.toString(),
                    word.verse.ayah.toString(),
                    word.wordNumber.toString()
                )
            )
            cursor.firstOrNull { c ->
                Pair(
                    c.getStringColumnOrNull("translation"),
                    c.getStringColumnOrNull("transliteration")
                )
            }
        }
    }
}
