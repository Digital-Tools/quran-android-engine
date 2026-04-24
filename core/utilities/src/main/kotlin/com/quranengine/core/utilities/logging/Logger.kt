package com.quranengine.core.utilities.logging

import android.util.Log

/**
 * Simple logger that delegates to Android's [Log] utility.
 * Drop-in replacement if Timber is added later — just swap the implementation.
 */
object Logger {

    private const val TAG = "QuranEngine"

    fun verbose(message: String) = Log.v(TAG, message)
    fun debug(message: String) = Log.d(TAG, message)
    fun info(message: String) = Log.i(TAG, message)
    fun warning(message: String) = Log.w(TAG, message)
    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e(TAG, message, throwable) else Log.e(TAG, message)
    }
}
