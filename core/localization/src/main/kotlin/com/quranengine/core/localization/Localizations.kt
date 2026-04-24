package com.quranengine.core.localization

import java.util.Locale

enum class Language(val code: String) {
    ARABIC("ar"),
    ENGLISH("en"),
}

enum class Table(val value: String) {
    LOCALIZABLE("Localizable"),
    ANDROID("Android"),
    SURAS("Suras"),
    READERS("Readers"),
}

/**
 * Abstraction for string localization.
 *
 * On Android, concrete implementations can delegate to `Context.getString()`
 * when a Context is available, or use a map-based fallback for pure-library usage.
 */
interface Localizer {
    fun l(key: String, table: Table = Table.LOCALIZABLE, language: Language? = null): String

    fun lFormat(
        key: String,
        table: Table = Table.LOCALIZABLE,
        language: Language? = null,
        vararg arguments: Any,
    ): String

    fun lAndroid(key: String, language: Language? = null): String =
        l(key, table = Table.ANDROID, language = language)
}

/**
 * Map-based localizer for use without Android Context.
 *
 * Strings are supplied per (table, language) pair. When [language] is null the
 * localizer tries the [defaultLanguage] first, then falls back to [Language.ENGLISH].
 */
class MapLocalizer(
    private val defaultLanguage: Language = Language.ENGLISH,
    private val strings: Map<Pair<Table, Language>, Map<String, String>> = emptyMap(),
) : Localizer {

    override fun l(key: String, table: Table, language: Language?): String {
        val preferredLanguage = language ?: defaultLanguage
        return sequenceOf(preferredLanguage, Language.ENGLISH)
            .distinct()
            .mapNotNull { candidate -> strings[table to candidate]?.get(key) }
            .firstOrNull()
            ?: key
    }

    override fun lFormat(
        key: String,
        table: Table,
        language: Language?,
        vararg arguments: Any,
    ): String {
        val pattern = l(key, table, language)
        return if (arguments.isEmpty()) {
            pattern
        } else {
            String.format(Locale.getDefault().fixedLocaleNumbers(), pattern, *arguments)
        }
    }
}
