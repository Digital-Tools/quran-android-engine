package com.quranengine.embeddedhost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quranengine.app.MainViewModel
import com.quranengine.features.appstructure.AppRoute
import com.quranengine.features.appstructure.AppStructureScreen
import com.quranengine.features.appstructure.DeepLinkHandler
import com.quranengine.ui.theme.AppearanceMode
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranHostScreen(
    viewModel: MainViewModel,
    deepLinkHandler: DeepLinkHandler,
    initialDeepLinkUri: String?,
    forcedAppearanceMode: AppearanceMode?,
    showCloseButton: Boolean,
    onClose: (() -> Unit)?,
) {
    val themeStyle by viewModel.themeStyle.collectAsState()
    val storedAppearanceMode by viewModel.appearanceMode.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isHomeRoute = currentRoute == AppRoute.Home.route

    QuranTheme(
        themeStyle = themeStyle,
        appearanceMode = forcedAppearanceMode ?: storedAppearanceMode,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppStructureScreen(
                navController = navController,
                deepLinkHandler = deepLinkHandler,
                initialDeepLinkUri = initialDeepLinkUri,
            )

            if (showCloseButton && onClose != null && isHomeRoute) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = CircleShape,
                    color = QuranTheme.colors.secondaryBackground.copy(alpha = 0.92f),
                    contentColor = QuranTheme.mizanGold,
                ) {
                    IconButton(
                        onClick = onClose,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                        )
                    }
                }
            }
        }
    }
}
