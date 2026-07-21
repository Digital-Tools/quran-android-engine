package com.quranengine.features.appstructure

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quranengine.features.advancedaudio.AdvancedAudioOptionsViewModel
import com.quranengine.features.advancedaudio.AdvancedAudioOptionsScreen
import com.quranengine.features.audiobanner.AudioBannerViewModel
import com.quranengine.features.bookmarks.BookmarksScreen
import com.quranengine.features.bookmarks.BookmarksViewModel
import com.quranengine.features.home.HomeScreen
import com.quranengine.features.home.HomeViewModel
import com.quranengine.features.notes.NotesScreen
import com.quranengine.features.notes.NotesViewModel
import com.quranengine.features.quranview.QuranViewRoute
import com.quranengine.features.quranview.QuranViewViewModel
import com.quranengine.features.reciterlist.ReciterListViewModel
import com.quranengine.features.reciterlist.StandaloneReciterListScreen
import com.quranengine.features.search.SearchScreen
import com.quranengine.features.search.SearchViewModel
import com.quranengine.features.settings.SettingsScreen
import com.quranengine.features.settings.SettingsViewModel
import com.quranengine.features.translations.TranslationsListScreen
import com.quranengine.features.translations.TranslationsListViewModel
import com.quranengine.ui.theme.QuranTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Main app scaffold with bottom navigation and navigation host.
 *
 * Each screen obtains its own ViewModel via [hiltViewModel], removing the need
 * for callers to supply ViewModel instances or callback lambdas.
 */
@Composable
fun AppStructureScreen(
    navController: NavHostController = rememberNavController(),
    deepLinkHandler: DeepLinkHandler? = null,
    initialDeepLinkUri: String? = null,
    incomingDeepLinks: Flow<String>? = null,
) {
    val audioBannerViewModel: AudioBannerViewModel = hiltViewModel()

    LaunchedEffect(deepLinkHandler, initialDeepLinkUri) {
        if (deepLinkHandler == null || initialDeepLinkUri == null) return@LaunchedEffect
        navController.navigateFromDeepLink(deepLinkHandler, initialDeepLinkUri)
    }
    LaunchedEffect(deepLinkHandler, incomingDeepLinks) {
        if (deepLinkHandler == null || incomingDeepLinks == null) return@LaunchedEffect
        incomingDeepLinks.collectLatest { uriString ->
            navController.navigateFromDeepLink(deepLinkHandler, uriString)
        }
    }

    Scaffold(
        containerColor = QuranTheme.colors.background,
        bottomBar = {
            AppBottomBar(navController = navController)
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppRoute.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                val viewType by viewModel.viewType.collectAsState()
                val sortOrder by viewModel.sortOrder.collectAsState()
                val lastPages by viewModel.lastPages.collectAsState()

                HomeScreen(
                    viewType = viewType,
                    sortOrder = sortOrder,
                    lastPages = lastPages,
                    suras = viewModel.suras,
                    quarters = viewModel.quarters,
                    onViewTypeChange = viewModel::setViewType,
                    onToggleSortOrder = viewModel::toggleSortOrder,
                    onSelectPage = { page ->
                        navController.navigate(AppRoute.QuranView(page.pageNumber).route)
                    },
                    onSelectSura = { sura ->
                        navController.navigateToSura(sura)
                    },
                    onSelectQuarter = { quarter ->
                        navController.navigateToQuarter(quarter.quarter)
                    },
                )
            }

            composable(AppRoute.Bookmarks.route) {
                val viewModel: BookmarksViewModel = hiltViewModel()
                BookmarksScreen(
                    viewModel = viewModel,
                    onNavigateToPage = { bookmark ->
                        navController.navigate(AppRoute.QuranView(bookmark.page.pageNumber).route)
                    },
                    onNavigateToNotes = {
                        navController.navigate(AppRoute.Notes.route)
                    },
                )
            }

            composable(AppRoute.Notes.route) {
                val viewModel: NotesViewModel = hiltViewModel()
                NotesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToAyah = { ayah ->
                        navController.navigateToAyah(ayah)
                    },
                )
            }

            composable(
                route = AppRoute.Search.ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(AppRoute.Search.QUERY_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->
                val viewModel: SearchViewModel = hiltViewModel()
                val initialQuery = backStackEntry.arguments?.getString(AppRoute.Search.QUERY_ARG)
                SearchScreen(
                    viewModel = viewModel,
                    initialQuery = initialQuery,
                    onNavigateToAyah = { ayah ->
                        navController.navigateToAyah(ayah)
                    },
                )
            }

            composable(AppRoute.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToReciters = {
                        navController.navigate(AppRoute.ReciterList.route)
                    },
                    onNavigateToTranslations = {
                        navController.navigate(AppRoute.TranslationsList.route)
                    },
                )
            }

            composable(
                route = AppRoute.QuranView.ROUTE_PATTERN,
                arguments = listOf(
                    navArgument("page") { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val viewModel: QuranViewViewModel = hiltViewModel(backStackEntry)
                QuranViewRoute(
                    viewModel = viewModel,
                    audioBannerViewModel = audioBannerViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToAdvancedAudio = { from, to ->
                        navController.navigateToAdvancedAudio(from, to)
                    },
                    onNavigateToTranslations = {
                        navController.navigate(AppRoute.TranslationsList.route)
                    },
                )
            }

            composable(AppRoute.ReciterList.route) {
                val viewModel: ReciterListViewModel = hiltViewModel()
                StandaloneReciterListScreen(
                    viewModel = viewModel,
                    onDone = { navController.popBackStack() },
                )
            }

            composable(AppRoute.TranslationsList.route) {
                val viewModel: TranslationsListViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                TranslationsListScreen(
                    uiState = uiState,
                    onRefresh = viewModel::refresh,
                    onDownload = viewModel::download,
                    onDelete = viewModel::delete,
                    onSelect = viewModel::select,
                    onDeselect = viewModel::deselect,
                    onMoveSelected = viewModel::moveSelectedTranslation,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = AppRoute.AdvancedAudio.ROUTE_PATTERN,
                arguments = listOf(
                    navArgument("fromSura") { type = NavType.IntType },
                    navArgument("fromAyah") { type = NavType.IntType },
                    navArgument("toSura") { type = NavType.IntType },
                    navArgument("toAyah") { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val viewModel: AdvancedAudioOptionsViewModel = hiltViewModel(backStackEntry)
                val dismissed by viewModel.dismissed.collectAsState()
                val playRequest by viewModel.playRequest.collectAsState()

                LaunchedEffect(dismissed) {
                    if (dismissed) {
                        navController.popBackStack()
                    }
                }
                LaunchedEffect(playRequest) {
                    playRequest?.let { request ->
                        audioBannerViewModel.setPlaybackRate(request.playbackRate)
                        audioBannerViewModel.play(
                            from = request.start,
                            to = request.end,
                            verseRuns = request.verseRuns,
                            listRuns = request.listRuns,
                        )
                        navController.popBackStack()
                    }
                }

                AdvancedAudioOptionsScreen(
                    viewModel = viewModel,
                    onNavigateToReciterList = {
                        navController.navigate(AppRoute.ReciterList.route)
                    },
                )
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom bar on tab destinations
    val isTabDestination = AppTab.entries.any { tab ->
        currentDestination.matchesTabRoute(tab.route)
    }
    if (!isTabDestination && currentDestination != null) return

    NavigationBar(
        containerColor = QuranTheme.colors.background,
        contentColor = QuranTheme.colors.text,
    ) {
        AppTab.entries.forEach { tab ->
            val selected = currentDestination.matchesTabRoute(tab.route)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                    )
                },
                label = { Text(text = tab.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = QuranTheme.appIdentity,
                    selectedTextColor = QuranTheme.appIdentity,
                    indicatorColor = QuranTheme.appIdentity.copy(alpha = 0.14f),
                    unselectedIconColor = QuranTheme.colors.secondaryText,
                    unselectedTextColor = QuranTheme.colors.secondaryText,
                ),
            )
        }
    }
}

private fun androidx.navigation.NavDestination?.matchesTabRoute(route: String): Boolean {
    return this?.hierarchy?.any { destination ->
        val destinationRoute = destination.route ?: return@any false
        destinationRoute == route || destinationRoute.substringBefore("?") == route
    } == true
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = QuranTheme.colors.secondaryText,
        )
    }
}
