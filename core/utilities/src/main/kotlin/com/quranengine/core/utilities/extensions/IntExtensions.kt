package com.quranengine.core.utilities.extensions

/**
 * Formats this integer as a zero-padded 3-digit string.
 * For example: 5 → "005", 42 → "042", 123 → "123".
 */
fun Int.as3DigitString(): String = "%03d".format(this)
