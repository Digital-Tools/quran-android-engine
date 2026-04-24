package com.quranengine.data.versetext

import com.quranengine.data.sqlite.ReadOnlyDatabase
import com.quranengine.data.sqlite.PersistenceError
import com.quranengine.data.sqlite.firstOrNull
import com.quranengine.data.sqlite.getStringColumn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads the text version from the properties table.
 * Port of Swift's GRDBDatabaseVersionPersistence.
 */
class SqliteDatabaseVersionPersistence(
    private val db: ReadOnlyDatabase
) : DatabaseVersionPersistence {

    override suspend fun getTextVersion(): Int = withContext(Dispatchers.IO) {
        db.read { database ->
            val cursor = database.rawQuery(
                "SELECT value FROM properties WHERE property = ?",
                arrayOf("text_version")
            )
            val value = cursor.firstOrNull { it.getStringColumn("value") }
                ?: throw PersistenceError.General("Cannot find text_version in properties table")
            value.toIntOrNull()
                ?: throw PersistenceError.General("text_version is not a valid integer: $value")
        }
    }
}
