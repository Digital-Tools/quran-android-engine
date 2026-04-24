package com.quranengine.core.system

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Simple key-value secure storage interface.
 * Replaces iOS KeychainAccess for Android.
 */
interface SecureStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun contains(key: String): Boolean
}

/**
 * Default implementation backed by [SharedPreferences].
 *
 * For production use with sensitive data, consider wrapping with
 * `EncryptedSharedPreferences` from the AndroidX Security library.
 */
class DefaultSecureStorage(context: Context) : SecureStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    override fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    override fun contains(key: String): Boolean = prefs.contains(key)

    private companion object {
        const val PREFS_NAME = "quranengine_secure_storage"
    }
}
