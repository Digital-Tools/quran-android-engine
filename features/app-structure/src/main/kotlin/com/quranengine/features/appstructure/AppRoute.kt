package com.quranengine.features.appstructure

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Bookmarks : AppRoute("bookmarks")
    data class Search(val query: String? = null) : AppRoute(
        if (query.isNullOrBlank()) {
            BASE_ROUTE
        } else {
            "$BASE_ROUTE?$QUERY_ARG=${encodeQuery(query)}"
        }
    ) {
        companion object {
            const val BASE_ROUTE = "search"
            const val QUERY_ARG = "query"
            const val ROUTE_PATTERN = "$BASE_ROUTE?$QUERY_ARG={$QUERY_ARG}"

            private fun encodeQuery(query: String): String {
                return URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "%20")
            }
        }
    }
    data object Settings : AppRoute("settings")
    data class QuranView(val page: Int) : AppRoute("quran/$page") {
        companion object {
            const val ROUTE_PATTERN = "quran/{page}"
        }
    }
    data object ReciterList : AppRoute("reciter_list")
    data object TranslationsList : AppRoute("translations")
    data class AdvancedAudio(
        val fromSura: Int,
        val fromAyah: Int,
        val toSura: Int,
        val toAyah: Int,
    ) : AppRoute("advanced_audio/$fromSura/$fromAyah/$toSura/$toAyah") {
        companion object {
            const val ROUTE_PATTERN = "advanced_audio/{fromSura}/{fromAyah}/{toSura}/{toAyah}"
        }
    }
}
