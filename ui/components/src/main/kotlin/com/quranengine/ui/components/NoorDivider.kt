package com.quranengine.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

@Composable
fun NoorDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 0.5.dp,
        color = QuranTheme.colors.secondaryText.copy(alpha = 0.2f),
    )
}
