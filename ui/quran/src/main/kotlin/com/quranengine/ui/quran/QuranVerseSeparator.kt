package com.quranengine.ui.quran

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranVerseSeparator(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 0.5.dp,
        color = QuranTheme.colors.secondaryBackground,
    )
}
