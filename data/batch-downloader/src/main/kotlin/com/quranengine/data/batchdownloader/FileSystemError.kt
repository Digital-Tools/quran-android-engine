package com.quranengine.data.batchdownloader

import java.io.IOException

sealed class FileSystemError : Exception() {
    data object NoDiskSpace : FileSystemError() {
        private fun readResolve(): Any = NoDiskSpace
        override val message: String get() = "No disk space available"
    }

    data class Unknown(override val cause: Throwable) : FileSystemError() {
        override val message: String get() = cause.message ?: "Unknown file system error"
    }

    companion object {
        fun from(error: Throwable): FileSystemError {
            return if (error is IOException && error.message?.contains("No space left") == true) {
                NoDiskSpace
            } else {
                Unknown(error)
            }
        }
    }
}
