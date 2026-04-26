package com.quranengine.features.appstructure

import android.net.Uri
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurankit.Sura
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * Converts deep link URIs with the `quranengine://` scheme into [AppRoute] destinations.
 *
 * Supported links:
 * - `quranengine://page/{pageNumber}`
 * - `quranengine://sura/{suraNumber}`
 * - `quranengine://search`
 * - `quranengine://search?q={query}`
 * - `quranengine://bookmarks`
 * - `quranengine://settings`
 * - `quranengine://translations`
 * - `quranengine://reciters`
 */
class DeepLinkHandler @Inject constructor(
    private val quran: Quran,
) {
    companion object {
        const val SCHEME = "quranengine"
        private const val SEARCH_QUERY_PARAM = "q"
    }

    /**
     * Parse a [Uri] into the corresponding [AppRoute], or `null` if the URI is
     * unrecognised or malformed.
     */
    fun handle(uri: Uri): AppRoute? {
        return handle(uri.toString())
    }

    /**
     * Convenience overload that accepts a raw URI string.
     */
    fun handle(uriString: String): AppRoute? {
        return try {
            val uri = URI(uriString)
            if (uri.scheme != SCHEME) return null

            when (uri.host) {
                "page" -> handlePage(uri)
                "sura" -> handleSura(uri)
                "search" -> AppRoute.Search(
                    parseQuery(uri.rawQuery)[SEARCH_QUERY_PARAM]
                        ?: parseQuery(uri.rawQuery)[AppRoute.Search.QUERY_ARG]
                )
                "bookmarks" -> AppRoute.Bookmarks
                "settings" -> AppRoute.Settings
                "translations" -> AppRoute.TranslationsList
                "reciters" -> AppRoute.ReciterList
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    // quranengine://page/{pageNumber}
    private fun handlePage(uri: URI): AppRoute? {
        val pageNumber = uri.pathSegments().firstOrNull()?.toIntOrNull() ?: return null
        if (pageNumber < 1 || pageNumber > quran.pages.size) return null
        return AppRoute.QuranView(pageNumber)
    }

    // quranengine://sura/{suraNumber}
    private fun handleSura(uri: URI): AppRoute? {
        val suraNumber = uri.pathSegments().firstOrNull()?.toIntOrNull() ?: return null
        val sura = Sura(quran, suraNumber) ?: return null
        return AppRoute.QuranView(sura.page.pageNumber)
    }

    private fun URI.pathSegments(): List<String> {
        return path
            ?.trim('/')
            ?.takeIf { it.isNotEmpty() }
            ?.split('/')
            ?: emptyList()
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()

        return rawQuery.split('&')
            .mapNotNull { entry ->
                if (entry.isBlank()) return@mapNotNull null
                val separatorIndex = entry.indexOf('=')
                val key = if (separatorIndex >= 0) entry.substring(0, separatorIndex) else entry
                val value = if (separatorIndex >= 0) entry.substring(separatorIndex + 1) else ""
                key.takeIf { it.isNotBlank() }?.let {
                    it to URLDecoder.decode(value, StandardCharsets.UTF_8)
                }
            }
            .toMap()
    }
}
