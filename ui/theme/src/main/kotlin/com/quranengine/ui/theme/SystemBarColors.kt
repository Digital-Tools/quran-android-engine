package com.quranengine.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Paints Android status / navigation bars to match mushaf chrome (iOS-style fusion).
 * Disables contrast scrims that otherwise leave a see-through strip above/below content.
 */
@Composable
fun MatchSystemBarsToChrome(
    statusBarColor: Color,
    navigationBarColor: Color,
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(statusBarColor, navigationBarColor, view) {
        val window = view.context.findActivity()?.window ?: return@DisposableEffect onDispose { }
        val previousStatus = window.statusBarColor
        val previousNav = window.navigationBarColor
        val previousNavContrast = if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced
        } else {
            false
        }
        val previousStatusContrast = if (Build.VERSION.SDK_INT >= 29) {
            window.isStatusBarContrastEnforced
        } else {
            false
        }
        val controller = WindowCompat.getInsetsController(window, view)
        val previousLightStatus = controller.isAppearanceLightStatusBars
        val previousLightNav = controller.isAppearanceLightNavigationBars

        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        // Dark chrome → light icons; light chrome → dark icons.
        val lightStatusIcons = statusBarColor.luminance() > 0.5f
        val lightNavIcons = navigationBarColor.luminance() > 0.5f
        controller.isAppearanceLightStatusBars = lightStatusIcons
        controller.isAppearanceLightNavigationBars = lightNavIcons

        onDispose {
            window.statusBarColor = previousStatus
            window.navigationBarColor = previousNav
            if (Build.VERSION.SDK_INT >= 29) {
                window.isNavigationBarContrastEnforced = previousNavContrast
                window.isStatusBarContrastEnforced = previousStatusContrast
            }
            controller.isAppearanceLightStatusBars = previousLightStatus
            controller.isAppearanceLightNavigationBars = previousLightNav
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
