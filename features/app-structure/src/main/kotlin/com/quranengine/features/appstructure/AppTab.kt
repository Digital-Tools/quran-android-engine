package com.quranengine.features.appstructure

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppTab(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    HOME(
        route = AppRoute.Home.route,
        title = "Home",
        icon = Icons.Default.Home,
    ),
    BOOKMARKS(
        route = AppRoute.Bookmarks.route,
        title = "Bookmarks",
        icon = Icons.Default.Bookmark,
    ),
    SEARCH(
        route = AppRoute.Search.route,
        title = "Search",
        icon = Icons.Default.Search,
    ),
    SETTINGS(
        route = AppRoute.Settings.route,
        title = "Settings",
        icon = Icons.Default.Settings,
    ),
}
