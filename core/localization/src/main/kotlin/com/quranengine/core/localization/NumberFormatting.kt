package com.quranengine.core.localization

import java.text.NumberFormat
import java.util.Locale

private const val LATIN_NUMBERS_SUFFIX = "@numbers=latn"

fun NumberFormat.format(number: Int): String = format(number.toLong())

fun NumberFormat.format(number: Float): String = format(number.toDouble())

object NumberFormatters {
    val shared: NumberFormat by lazy {
        NumberFormat.getInstance(Locale.getDefault().fixedLocaleNumbers())
    }

    val arabic: NumberFormat by lazy {
        NumberFormat.getInstance(Locale("ar", "SA"))
    }
}

fun Locale.fixedLocaleNumbers(): Locale {
    val identifier = toString()
    if (!identifier.endsWith(LATIN_NUMBERS_SUFFIX)) {
        return this
    }

    val languageTag = identifier
        .removeSuffix(LATIN_NUMBERS_SUFFIX)
        .replace('_', '-')

    return Locale.forLanguageTag(languageTag)
}

val fixedCurrentLocaleNumbers: Locale
    get() = Locale.getDefault().fixedLocaleNumbers()
