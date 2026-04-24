package com.quranengine.features.appstructure

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Bookmarks : AppRoute("bookmarks")
    data object Search : AppRoute("search")
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
