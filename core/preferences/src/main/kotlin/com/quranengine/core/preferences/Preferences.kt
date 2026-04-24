package com.quranengine.core.preferences

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

public class Preferences(private val sharedPreferences: SharedPreferences) {

    private val notificationsSubject = MutableSharedFlow<String>(extraBufferCapacity = 64)

    public val notifications: Flow<String> = notificationsSubject.asSharedFlow()

    @Suppress("UNCHECKED_CAST")
    public fun <T> valueForKey(key: PreferenceKey<T>): T {
        val prefs = sharedPreferences
        if (!prefs.contains(key.key)) return key.defaultValue

        val value: Any? = when (key.defaultValue) {
            is Int -> prefs.getInt(key.key, key.defaultValue)
            is Long -> prefs.getLong(key.key, key.defaultValue)
            is Float -> prefs.getFloat(key.key, key.defaultValue)
            is Boolean -> prefs.getBoolean(key.key, key.defaultValue)
            is String -> prefs.getString(key.key, key.defaultValue)
            is Set<*> -> prefs.getStringSet(key.key, key.defaultValue as Set<String>)
            else -> return key.defaultValue
        }
        return (value as? T) ?: key.defaultValue
    }

    public fun <T> setValue(value: T?, key: PreferenceKey<T>) {
        val editor = sharedPreferences.edit()
        if (value == null) {
            editor.remove(key.key)
        } else {
            @Suppress("UNCHECKED_CAST")
            when (value) {
                is Int -> editor.putInt(key.key, value)
                is Long -> editor.putLong(key.key, value)
                is Float -> editor.putFloat(key.key, value)
                is Boolean -> editor.putBoolean(key.key, value)
                is String -> editor.putString(key.key, value)
                is Set<*> -> editor.putStringSet(key.key, value as Set<String>)
                else -> error("Unsupported preference type: ${value::class}")
            }
        }
        editor.apply()
        notificationsSubject.tryEmit(key.key)
    }

    public fun removeValueForKey(key: PreferenceKey<*>) {
        sharedPreferences.edit().remove(key.key).apply()
        notificationsSubject.tryEmit(key.key)
    }
}
