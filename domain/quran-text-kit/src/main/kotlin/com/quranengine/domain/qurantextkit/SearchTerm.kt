package com.quranengine.domain.qurantextkit

import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurantext.SearchResult
import java.text.Normalizer
import java.util.regex.Pattern

private object SearchRegex {
    /** Match unicode category Separators (Z). */
    const val SPACE_REGEX = "\\p{Z}+"

    /** Match unicode categories Marks (M), Punctuation (P), Symbols (S), Control (C) and Arabic Tatweel character. */
    const val INVALID_SEARCH_REGEX = "[\\p{M}\\p{P}\\p{S}\\p{Cc}\u0640]"

    val ARABIC_SIMILARITY_REGEX = "[\u0627\u0623\u0621\u062a\u0629\u0647\u0649\u0626]"

    val ARABIC_SIMILARITY_REPLACEMENTS: Map<Char, String> = mapOf(
        // given: ا  match: آأإاﻯ
        '\u0627' to "\u0622\u0623\u0625\u0627\u0649",
        // given: ﺃ  match: ﺃﺀﺆﺋ
        '\u0623' to "\u0621\u0623\u0624\u0626",
        // given: ﺀ  match: ﺀﺃﺆ
        '\u0621' to "\u0621\u0623\u0624\u0626",
        // given: ﺕ  match: ﺕﺓ
        '\u062a' to "\u062a\u0629",
        // given: ﺓ  match: ﺓتﻫ
        '\u0629' to "\u0629\u062a\u0647",
        // given: ه  match: ةه
        '\u0647' to "\u0647\u0629",
        // given: ﻯ  match: ﻯي
        '\u0649' to "\u0649\u064a",
        // given: ئ  match: ئﻯي
        '\u0626' to "\u0626\u0649\u064a",
    )
}

class SearchTerm private constructor(
    val compactQuery: String,
    val persistenceQuery: String,
    private val queryRegex: Pattern,
) {
    companion object {
        operator fun invoke(value: String): SearchTerm? {
            val compact = value.trimmedWords()
            if (compact.isEmpty()) return null
            val persistence = compact.removeInvalidSearchCharacters()
            val regex = regexForArabicSimilarityCharacters(persistence) ?: return null
            return SearchTerm(compact, persistence, regex)
        }

        fun regexForArabicSimilarityCharacters(value: String): Pattern? {
            val cleaned = value.removeInvalidSearchCharacters()
            val sb = StringBuilder()
            for (char in cleaned) {
                val replacement = SearchRegex.ARABIC_SIMILARITY_REPLACEMENTS[char]
                if (replacement != null) {
                    sb.append("[$replacement]")
                } else {
                    sb.append(Pattern.quote(char.toString()))
                }
                sb.append(SearchRegex.INVALID_SEARCH_REGEX).append("*")
            }
            return try {
                Pattern.compile("($sb)", Pattern.CASE_INSENSITIVE)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun persistenceQueryReplacingArabicSimilarityCharactersWithUnderscore(): String =
        persistenceQuery.replace(Regex(SearchRegex.ARABIC_SIMILARITY_REGEX), "_")

    fun buildAutocompletions(searchResults: List<String>): List<String> {
        val result = mutableListOf<String>()
        val added = mutableSetOf<String>()
        for (searchResult in searchResults) {
            val textsToCheck = listOf(
                searchResult,
                Normalizer.normalize(searchResult, Normalizer.Form.NFKD)
            )
            for (text in textsToCheck) {
                val suffixes = text.caseInsensitiveComponents(queryRegex)
                for (suffixIndex in 1 until suffixes.size) {
                    val suffix = suffixes[suffixIndex]
                    // Include only first 5 words
                    val suffixWords = suffix.split(" ").take(5).joinToString(" ")
                    val trimmed = suffixWords.trim { it.isWhitespace() || it.isLetterOrDigit() }.let { trimmedOnly ->
                        if (trimmedOnly.isEmpty() && suffixWords != trimmedOnly) null
                        else suffixWords.trimNonAlphanumericEdges()
                    } ?: continue
                    if (trimmed.isEmpty() && suffixWords != trimmed) continue
                    val subrow = persistenceQuery + trimmed
                    if (added.add(subrow)) {
                        result.add(subrow)
                    }
                }
            }
        }
        return result
    }

    fun buildSearchResults(verses: List<Pair<AyahNumber, String>>): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        for ((verse, verseText) in verses) {
            val textsToCheck = listOf(
                verseText,
                Normalizer.normalize(verseText, Normalizer.Form.NFKD)
            )
            for (text in textsToCheck) {
                val ranges = text.findRanges(queryRegex)
                if (ranges.isNotEmpty()) {
                    results.add(SearchResult(text = text, ranges = ranges, ayah = verse))
                    break
                }
            }
        }
        return results
    }
}

// ---------------------------------------------------------------------------
// String extensions
// ---------------------------------------------------------------------------

internal fun String.removeInvalidSearchCharacters(): String {
    var cleaned = replace(Regex(SearchRegex.INVALID_SEARCH_REGEX), "")
        .replace(Regex(SearchRegex.SPACE_REGEX), " ")

    if (cleaned.length > 1000) {
        cleaned = cleaned.substring(0, 1000)
    }
    return cleaned.lowercase()
}

internal fun String.caseInsensitiveComponents(separator: Pattern): List<String> {
    val ranges = findRanges(separator)
    val components = mutableListOf<String>()
    var lowerBound = 0
    for (range in ranges) {
        components.add(substring(lowerBound, range.first))
        lowerBound = range.last + 1
    }
    components.add(substring(lowerBound))
    return components
}

internal fun String.findRanges(regex: Pattern): List<IntRange> {
    val matcher = regex.matcher(this)
    val ranges = mutableListOf<IntRange>()
    while (matcher.find()) {
        ranges.add(matcher.start()..matcher.end() - 1)
    }
    return ranges
}

internal fun String.containsArabic(): Boolean =
    Regex("\\p{InArabic}").containsMatchIn(this)

internal fun String.containsOnlyNumbers(): Boolean =
    removeInvalidSearchCharacters().matches(Regex("^[0-9]+$"))

internal fun String.trimmedWords(): String =
    split(" ").map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")

private fun String.trimNonAlphanumericEdges(): String {
    val start = indexOfFirst { it.isWhitespace() || it.isLetterOrDigit() }
    if (start == -1) return ""
    val end = indexOfLast { it.isWhitespace() || it.isLetterOrDigit() }
    return substring(start, end + 1)
}

/**
 * Returns a list with duplicates removed, preserving insertion order.
 */
internal fun <T> List<T>.orderedUnique(): List<T> {
    val seen = linkedSetOf<T>()
    return filter { seen.add(it) }
}
