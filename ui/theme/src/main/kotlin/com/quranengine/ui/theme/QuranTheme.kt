package com.quranengine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeStyle = staticCompositionLocalOf { ThemeStyle.PAPER }
val LocalAppearanceMode = staticCompositionLocalOf { AppearanceMode.AUTO }
val LocalThemeColors = staticCompositionLocalOf { ThemeStyle.PAPER.colors(isDark = false) }
val LocalQuranFontSize = staticCompositionLocalOf { QuranFontSize.LARGE }
private val LocalIsDarkTheme = staticCompositionLocalOf { false }

object QuranTheme {
    val colors: ThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeColors.current

    val themeStyle: ThemeStyle
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeStyle.current

    val fontSize: QuranFontSize
        @Composable
        @ReadOnlyComposable
        get() = LocalQuranFontSize.current

    val appIdentity: Color
        @Composable
        @ReadOnlyComposable
        get() = if (LocalIsDarkTheme.current) QuranColors.appIdentityDark else QuranColors.appIdentityLight

    val mizanGold: Color
        @Composable
        @ReadOnlyComposable
        get() = if (LocalIsDarkTheme.current) QuranColors.mizanGoldDark else QuranColors.mizanGoldLight

    val pageMarkerTint: Color
        @Composable
        @ReadOnlyComposable
        get() = if (LocalIsDarkTheme.current) QuranColors.pageMarkerDark else QuranColors.pageMarkerLight

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIsDarkTheme.current
}

@Composable
fun QuranTheme(
    themeStyle: ThemeStyle = LocalThemeStyle.current,
    appearanceMode: AppearanceMode = LocalAppearanceMode.current,
    fontSize: QuranFontSize = LocalQuranFontSize.current,
    content: @Composable () -> Unit,
) {
    val isDark = when (appearanceMode) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.AUTO -> {
            // Quiet theme is always dark mode
            if (themeStyle == ThemeStyle.QUIET) true else isSystemInDarkTheme()
        }
    }

    val themeColors = themeStyle.colors(isDark)

    // Map to Material3 color scheme
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = QuranColors.appIdentityDark,
            onPrimary = themeColors.text,
            background = themeColors.background,
            onBackground = themeColors.text,
            surface = themeColors.background,
            onSurface = themeColors.text,
            surfaceVariant = themeColors.secondaryBackground,
            onSurfaceVariant = themeColors.secondaryText,
        )
    } else {
        lightColorScheme(
            primary = QuranColors.appIdentityLight,
            onPrimary = themeColors.background,
            background = themeColors.background,
            onBackground = themeColors.text,
            surface = themeColors.background,
            onSurface = themeColors.text,
            surfaceVariant = themeColors.secondaryBackground,
            onSurfaceVariant = themeColors.secondaryText,
        )
    }

    CompositionLocalProvider(
        LocalThemeStyle provides themeStyle,
        LocalAppearanceMode provides appearanceMode,
        LocalThemeColors provides themeColors,
        LocalQuranFontSize provides fontSize,
        LocalIsDarkTheme provides isDark,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
