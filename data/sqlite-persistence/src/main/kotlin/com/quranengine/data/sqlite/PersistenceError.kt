package com.quranengine.data.sqlite

sealed class PersistenceError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class General(message: String) : PersistenceError(message)
    class OpenDatabase(cause: Throwable, filePath: String) : PersistenceError("Failed to open database: $filePath", cause)
    class Query(cause: Throwable) : PersistenceError("Query error", cause)
    class Unknown(cause: Throwable) : PersistenceError("Unknown error", cause)
    class BadFile(cause: Throwable? = null) : PersistenceError("Bad database file", cause)

    companion object {
        fun generalError(error: Throwable, info: String): PersistenceError =
            General("error: $error, info: $info")
    }
}
