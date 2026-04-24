package com.quranengine.features.appstructure

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Page
import com.quranengine.model.qurankit.Quarter
import com.quranengine.model.qurankit.Sura

/**
 * Navigation helper extensions that simplify common in-app navigation patterns.
 */

fun NavController.navigateToPage(pageNumber: Int) {
    navigate(AppRoute.QuranView(pageNumber).route)
}

fun NavController.navigateToPage(page: Page) {
    navigateToPage(page.pageNumber)
}

fun NavController.navigateToAyah(ayah: AyahNumber) {
    navigateToPage(ayah.page)
}

fun NavController.navigateToSura(sura: Sura) {
    navigateToPage(sura.page.pageNumber)
}

fun NavController.navigateToQuarter(quarter: Quarter) {
    navigateToPage(quarter.page)
}

fun NavController.navigateToSearch() {
    navigate(AppRoute.Search.route)
}

fun NavController.navigateToBookmarks() {
    navigate(AppRoute.Bookmarks.route)
}

fun NavController.navigateToSettings() {
    navigate(AppRoute.Settings.route)
}

fun NavController.navigateToTranslations() {
    navigate(AppRoute.TranslationsList.route)
}

fun NavController.navigateToReciters() {
    navigate(AppRoute.ReciterList.route)
}

fun NavController.navigateToAdvancedAudio(from: AyahNumber, to: AyahNumber) {
    navigate(
        AppRoute.AdvancedAudio(
            fromSura = from.sura.suraNumber,
            fromAyah = from.ayah,
            toSura = to.sura.suraNumber,
            toAyah = to.ayah,
        ).route
    )
}

/**
 * Navigate to a top-level tab destination, reusing existing back-stack state.
 */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Navigate to the route produced by a [DeepLinkHandler], if valid.
 * Returns `true` when navigation was performed.
 */
fun NavController.navigateFromDeepLink(handler: DeepLinkHandler, uriString: String): Boolean {
    val route = handler.handle(uriString) ?: return false
    navigate(route.route)
    return true
}
