package com.quranengine.core.utilities.extensions

import java.text.BreakIterator
import java.util.Locale

enum class ChunkStrategy {
    PARAGRAPH,
    SENTENCE,
    WORD,
}

/**
 * Splits a string into chunks of approximately [maxLength] characters,
 * breaking at natural boundaries defined by [strategy].
 *
 * Uses [java.text.BreakIterator] for locale-aware boundary detection.
 */
fun String.chunked(
    maxLength: Int,
    strategy: ChunkStrategy = ChunkStrategy.SENTENCE,
    locale: Locale = Locale.getDefault(),
): List<String> {
    if (length <= maxLength) return listOf(this)

    // For paragraphs, split on double-newlines first, then fall back to sentence chunking
    if (strategy == ChunkStrategy.PARAGRAPH) {
        return chunkByParagraphs(maxLength, locale)
    }

    val breakIterator = when (strategy) {
        ChunkStrategy.PARAGRAPH -> error("unreachable")
        ChunkStrategy.SENTENCE -> BreakIterator.getSentenceInstance(locale)
        ChunkStrategy.WORD -> BreakIterator.getWordInstance(locale)
    }

    breakIterator.setText(this)

    val chunks = mutableListOf<String>()
    var chunkStart = 0
    var lastBreak = 0
    var boundary = breakIterator.next()

    while (boundary != BreakIterator.DONE) {
        if (boundary - chunkStart > maxLength) {
            if (lastBreak > chunkStart) {
                chunks.add(substring(chunkStart, lastBreak).trim())
                chunkStart = lastBreak
            } else {
                // Single segment exceeds maxLength — force split at boundary
                chunks.add(substring(chunkStart, boundary).trim())
                chunkStart = boundary
            }
        }
        lastBreak = boundary
        boundary = breakIterator.next()
    }

    if (chunkStart < length) {
        val remainder = substring(chunkStart).trim()
        if (remainder.isNotEmpty()) chunks.add(remainder)
    }

    return chunks
}

private fun String.chunkByParagraphs(maxLength: Int, locale: Locale): List<String> {
    val paragraphs = split(Regex("\\n\\s*\\n"))
    val chunks = mutableListOf<String>()
    val current = StringBuilder()

    for (paragraph in paragraphs) {
        val trimmed = paragraph.trim()
        if (trimmed.isEmpty()) continue

        if (current.length + trimmed.length + 1 > maxLength && current.isNotEmpty()) {
            chunks.add(current.toString().trim())
            current.clear()
        }

        if (trimmed.length > maxLength) {
            if (current.isNotEmpty()) {
                chunks.add(current.toString().trim())
                current.clear()
            }
            // Fall back to sentence chunking for oversized paragraphs
            chunks.addAll(trimmed.chunked(maxLength, ChunkStrategy.SENTENCE, locale))
        } else {
            if (current.isNotEmpty()) current.append("\n\n")
            current.append(trimmed)
        }
    }

    if (current.isNotEmpty()) {
        chunks.add(current.toString().trim())
    }

    return chunks
}
