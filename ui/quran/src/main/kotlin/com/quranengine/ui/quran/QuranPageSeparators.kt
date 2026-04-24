package com.quranengine.ui.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

enum class PageSide { START, END }

@Composable
fun PageSideSeparator(
    side: PageSide,
    modifier: Modifier = Modifier,
) {
    val colors = QuranTheme.colors
    val gradientColors = when (side) {
        PageSide.START -> listOf(colors.pageSeparatorLine, colors.pageSeparatorBackground, Color.Transparent)
        PageSide.END -> listOf(Color.Transparent, colors.pageSeparatorBackground, colors.pageSeparatorLine)
    }

    Row(modifier = modifier.width(12.dp).fillMaxHeight()) {
        // Decorative line
        if (side == PageSide.START) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.pageSeparatorLine)
            )
        }

        // Gradient
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(gradientColors)
                )
        )

        // Decorative line
        if (side == PageSide.END) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colors.pageSeparatorLine)
            )
        }
    }
}

@Composable
fun PageMiddleSeparator(modifier: Modifier = Modifier) {
    val colors = QuranTheme.colors
    Box(
        modifier = modifier
            .width(2.dp)
            .fillMaxHeight()
            .background(colors.pageSeparatorLine)
    )
}
