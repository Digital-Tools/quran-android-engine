package com.quranengine.core.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegated property that reads/writes a [PreferenceKey] from [Preferences].
 *
 * Usage:
 * ```
 * var fontSize by Preference(PreferenceKey("font_size", 16), preferences)
 * ```
 */
public class Preference<T>(
    private val key: PreferenceKey<T>,
    private val preferences: Preferences,
) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        preferences.valueForKey(key)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        preferences.setValue(value, key)
    }

    /** Observable flow that emits the current value on subscription and on every change. */
    public val flow: Flow<T>
        get() = preferences.notifications
            .filter { it == key.key }
            .map { preferences.valueForKey(key) }
            .onStart { emit(preferences.valueForKey(key)) }
}

/**
 * Delegated property that applies a [PreferenceTransformer] on top of a raw [PreferenceKey].
 *
 * Usage:
 * ```
 * var theme by TransformedPreference(
 *     PreferenceKey("theme", "light"),
 *     preferences,
 *     PreferenceTransformer(rawToValue = Theme::fromString, valueToRaw = Theme::toString)
 * )
 * ```
 */
public class TransformedPreference<Raw, T>(
    private val key: PreferenceKey<Raw>,
    private val preferences: Preferences,
    private val transformer: PreferenceTransformer<Raw, T>,
) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        transformer.rawToValue(preferences.valueForKey(key))

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        preferences.setValue(transformer.valueToRaw(value), key)
    }

    /** Observable flow that emits the transformed value on subscription and on every change. */
    public val flow: Flow<T>
        get() = preferences.notifications
            .filter { it == key.key }
            .map { transformer.rawToValue(preferences.valueForKey(key)) }
            .onStart { emit(transformer.rawToValue(preferences.valueForKey(key))) }
}
