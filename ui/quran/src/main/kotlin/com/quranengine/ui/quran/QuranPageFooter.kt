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
fun QuranPageFooter(
    pageNumber: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = pageNumber,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        color = QuranTheme.colors.secondaryText,
        textAlign = TextAlign.Center,
    )
}
