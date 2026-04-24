package com.quranengine.core.utilities.extensions

import java.io.File

// -- Path manipulation via java.io.File --

val String.lastPathComponent: String
    get() = File(this).name

val String.pathExtension: String
    get() = File(this).extension

val String.stringByDeletingLastPathComponent: String
    get() = File(this).parent ?: ""

val String.stringByDeletingPathExtension: String
    get() {
        val ext = pathExtension
        return if (ext.isEmpty()) this else dropLast(ext.length + 1)
    }

val String.pathComponents: List<String>
    get() {
        val file = File(this)
        val parts = mutableListOf<String>()
        if (startsWith(File.separator)) parts.add(File.separator)
        var current: File? = file
        val stack = mutableListOf<String>()
        while (current != null && current.name.isNotEmpty()) {
            stack.add(current.name)
            current = current.parentFile
        }
        parts.addAll(stack.asReversed())
        return parts
    }

fun String.stringByAppendingPath(component: String): String =
    File(this, component).path

fun String.stringByAppendingExtension(ext: String): String =
    "$this.$ext"

/**
 * Converts a byte offset into the corresponding string character index.
 * Uses UTF-8 encoding to map byte positions to character positions.
 */
fun String.byteOffsetToStringIndex(byteOffset: Int): Int {
    val bytes = toByteArray(Charsets.UTF_8)
    if (byteOffset >= bytes.size) return length
    val prefix = String(bytes, 0, byteOffset, Charsets.UTF_8)
    return prefix.length
}

// -- Regex helpers --

/**
 * Returns all ranges matching the given [pattern] as a list of [IntRange].
 */
fun String.ranges(pattern: String): List<IntRange> {
    return Regex(pattern).findAll(this).map { it.range }.toList()
}

/**
 * Replaces all occurrences matching [pattern] using a provider that receives each match.
 */
fun String.replacingOccurrences(
    matchingPattern: String,
    replacementProvider: (MatchResult) -> String,
): String {
    return Regex(matchingPattern).replace(this) { replacementProvider(it) }
}

/**
 * Replaces all matches of [pattern] using the [replace] function.
 * Returns the modified string.
 */
fun String.replaceMatches(
    pattern: String,
    replace: (MatchResult) -> String,
): String {
    return Regex(pattern).replace(this, replace)
}

/**
 * Replaces characters in the given sorted, non-overlapping [ranges] using [body].
 * Each range's substring is passed to [body] and the result is substituted in.
 */
fun String.replacing(
    sortedRanges: List<IntRange>,
    body: (String) -> String,
): String {
    if (sortedRanges.isEmpty()) return this
    val sb = StringBuilder()
    var lastEnd = 0
    for (range in sortedRanges) {
        sb.append(substring(lastEnd, range.first))
        sb.append(body(substring(range)))
        lastEnd = range.last + 1
    }
    sb.append(substring(lastEnd))
    return sb.toString()
}
