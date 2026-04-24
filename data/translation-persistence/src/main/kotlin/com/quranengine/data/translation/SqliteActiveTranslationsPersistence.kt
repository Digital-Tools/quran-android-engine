package com.quranengine.data.translation

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.quranengine.data.sqlite.firstOrNull
import com.quranengine.data.sqlite.getIntColumn
import com.quranengine.data.sqlite.getStringColumn
import com.quranengine.data.sqlite.getStringColumnOrNull
import com.quranengine.data.sqlite.map
import com.quranengine.model.qurantext.Translation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Read-write persistence for the active translations list.
 * Port of Swift's GRDBActiveTranslationsPersistence.
 *
 * Uses [SQLiteOpenHelper] to manage the translations.db file with migrations.
 */
class SqliteActiveTranslationsPersistence(
    context: Context,
    directory: String
) : ActiveTranslationsPersistence {

    private val helper = TranslationsOpenHelper(context, "$directory/translations.db")

    override suspend fun retrieveAll(): List<Translation> = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        cursor.map { c ->
            Translation(
                id = c.getIntColumn(COL_ID),
                displayName = c.getStringColumn(COL_NAME),
                translator = c.getStringColumnOrNull(COL_TRANSLATOR),
                translatorForeign = c.getStringColumnOrNull(COL_TRANSLATOR_FOREIGN),
                fileURL = c.getStringColumn(COL_FILE_URL),
                fileName = c.getStringColumn(COL_FILENAME),
                languageCode = c.getStringColumn(COL_LANGUAGE_CODE),
                version = c.getIntColumn(COL_VERSION),
                installedVersion = c.getStringColumnOrNull(COL_INSTALLED_VERSION)?.toIntOrNull()
            )
        }
    }

    override suspend fun insert(translation: Translation) = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.insertWithOnConflict(TABLE_NAME, null, translation.toContentValues(), SQLiteDatabase.CONFLICT_REPLACE)
        Unit
    }

    override suspend fun remove(translation: Translation) = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(translation.id.toString()))
        Unit
    }

    override suspend fun update(translation: Translation) = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.update(TABLE_NAME, translation.toContentValues(), "$COL_ID = ?", arrayOf(translation.id.toString()))
        Unit
    }

    private fun Translation.toContentValues(): ContentValues = ContentValues().apply {
        put(COL_ID, id)
        put(COL_NAME, displayName)
        put(COL_TRANSLATOR, translator)
        put(COL_TRANSLATOR_FOREIGN, translatorForeign)
        put(COL_FILE_URL, fileURL)
        put(COL_FILENAME, fileName)
        put(COL_LANGUAGE_CODE, languageCode)
        put(COL_VERSION, version)
        if (installedVersion != null) {
            put(COL_INSTALLED_VERSION, installedVersion)
        } else {
            putNull(COL_INSTALLED_VERSION)
        }
    }

    private class TranslationsOpenHelper(
        context: Context,
        path: String
    ) : SQLiteOpenHelper(context, path, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                    $COL_ID INTEGER PRIMARY KEY,
                    $COL_NAME TEXT NOT NULL,
                    $COL_TRANSLATOR TEXT,
                    $COL_TRANSLATOR_FOREIGN TEXT,
                    $COL_FILE_URL TEXT NOT NULL,
                    $COL_FILENAME TEXT NOT NULL,
                    $COL_LANGUAGE_CODE TEXT NOT NULL,
                    $COL_VERSION INTEGER NOT NULL,
                    $COL_INSTALLED_VERSION INTEGER
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Timber.i("Upgrading translations database from $oldVersion to $newVersion")
        }
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "translations"
        private const val COL_ID = "_ID"
        private const val COL_NAME = "name"
        private const val COL_TRANSLATOR = "translator"
        private const val COL_TRANSLATOR_FOREIGN = "translator_foreign"
        private const val COL_FILE_URL = "fileURL"
        private const val COL_FILENAME = "filename"
        private const val COL_LANGUAGE_CODE = "languageCode"
        private const val COL_VERSION = "version"
        private const val COL_INSTALLED_VERSION = "installedVersion"
    }
}
