package com.quranengine.ui.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranPageHeader(
    quarterName: String,
    suraNames: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = suraNames,
            style = MaterialTheme.typography.labelSmall,
            color = QuranTheme.colors.secondaryText,
        )
        Text(
            text = quarterName,
            style = MaterialTheme.typography.labelSmall,
            color = QuranTheme.colors.secondaryText,
        )
    }
}
