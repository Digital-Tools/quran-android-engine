package com.quranengine.data.sqlite

import android.database.Cursor

fun Cursor.getStringColumn(name: String): String = getString(getColumnIndexOrThrow(name))
fun Cursor.getStringColumnOrNull(name: String): String? {
    val index = getColumnIndexOrThrow(name)
    return if (isNull(index)) null else getString(index)
}
fun Cursor.getIntColumn(name: String): Int = getInt(getColumnIndexOrThrow(name))
fun Cursor.getLongColumn(name: String): Long = getLong(getColumnIndexOrThrow(name))

inline fun <T> Cursor.map(transform: (Cursor) -> T): List<T> {
    val result = mutableListOf<T>()
    use { cursor ->
        while (cursor.moveToNext()) {
            result.add(transform(cursor))
        }
    }
    return result
}

inline fun <T> Cursor.firstOrNull(transform: (Cursor) -> T): T? {
    use { cursor ->
        return if (cursor.moveToFirst()) transform(cursor) else null
    }
}
