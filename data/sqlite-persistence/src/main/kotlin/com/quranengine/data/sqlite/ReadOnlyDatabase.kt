package com.quranengine.data.sqlite

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.quranengine.core.utilities.concurrency.ManagedCriticalState
import timber.log.Timber
import java.io.File

/**
 * A connection pool for read-only SQLite databases.
 * Port of Swift's DatabaseConnectionPool using GRDB.
 */
class ReadOnlyDatabase private constructor(private val filePath: String) {

    private val lock = ManagedCriticalState(DatabaseState())

    private data class DatabaseState(
        var database: SQLiteDatabase? = null
    )

    fun <T> read(block: (SQLiteDatabase) -> T): T {
        val db = getDatabase()
        return try {
            block(db)
        } catch (e: Exception) {
            Timber.e(e, "Error executing query on $filePath")
            throw PersistenceError.Query(e)
        }
    }

    private fun getDatabase(): SQLiteDatabase {
        return lock.withCriticalRegion { state ->
            val db = state.database ?: run {
                val newDb = try {
                    SQLiteDatabase.openDatabase(filePath, null, SQLiteDatabase.OPEN_READONLY)
                } catch (e: Exception) {
                    Timber.e(e, "Cannot open sqlite file $filePath")
                    throw PersistenceError.OpenDatabase(e, filePath)
                }
                state.database = newDb
                newDb
            }
            state to db
        }
    }

    fun close() {
        lock.withCriticalRegion { state ->
            state.database?.close()
            state.database = null
            state to Unit
        }
    }

    companion object {
        fun openFile(file: File): ReadOnlyDatabase = ReadOnlyDatabase(file.absolutePath)
        fun openPath(path: String): ReadOnlyDatabase = ReadOnlyDatabase(path)
    }
}
