package com.quranengine.features.appstructure

import android.net.Uri
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurankit.Sura
import javax.inject.Inject

/**
 * Converts deep link URIs with the `quranengine://` scheme into [AppRoute] destinations.
 *
 * Supported links:
 * - `quranengine://page/{pageNumber}`
 * - `quranengine://sura/{suraNumber}`
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
    }

    /**
     * Parse a [Uri] into the corresponding [AppRoute], or `null` if the URI is
     * unrecognised or malformed.
     */
    fun handle(uri: Uri): AppRoute? {
        if (uri.scheme != SCHEME) return null

        return when (uri.host) {
            "page" -> handlePage(uri)
            "sura" -> handleSura(uri)
            "search" -> AppRoute.Search
            "bookmarks" -> AppRoute.Bookmarks
            "settings" -> AppRoute.Settings
            "translations" -> AppRoute.TranslationsList
            "reciters" -> AppRoute.ReciterList
            else -> null
        }
    }

    /**
     * Convenience overload that accepts a raw URI string.
     */
    fun handle(uriString: String): AppRoute? {
        return try {
            handle(Uri.parse(uriString))
        } catch (_: Exception) {
            null
        }
    }

    // quranengine://page/{pageNumber}
    private fun handlePage(uri: Uri): AppRoute? {
        val pageNumber = uri.pathSegments?.firstOrNull()?.toIntOrNull() ?: return null
        if (pageNumber < 1 || pageNumber > quran.pages.size) return null
        return AppRoute.QuranView(pageNumber)
    }

    // quranengine://sura/{suraNumber}
    private fun handleSura(uri: Uri): AppRoute? {
        val suraNumber = uri.pathSegments?.firstOrNull()?.toIntOrNull() ?: return null
        val sura = Sura(quran, suraNumber) ?: return null
        return AppRoute.QuranView(sura.page.pageNumber)
    }
}
