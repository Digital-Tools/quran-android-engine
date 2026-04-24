package com.quranengine.ui.theme

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.themedBackground(): Modifier {
    val colors = QuranTheme.colors
    return this.background(colors.background)
}

@Composable
fun Modifier.themedSecondaryBackground(): Modifier {
    val colors = QuranTheme.colors
    return this.background(colors.secondaryBackground)
}

@Composable
fun themedTextColor(): Color = QuranTheme.colors.text

@Composable
fun themedSecondaryTextColor(): Color = QuranTheme.colors.secondaryText
