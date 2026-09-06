package com.quranengine.ui.quran

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranTranslatorName(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "- $name",
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Start,
        color = QuranTheme.colors.secondaryText,
    )
}
