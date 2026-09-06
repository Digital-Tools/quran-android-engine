package com.quranengine.ui.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranengine.ui.theme.QuranFontFamilies
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranPageHeader(
    quarterName: String,
    suraNames: String,
    modifier: Modifier = Modifier,
    decoratedSuraName: String = "",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = quarterName,
            style = MaterialTheme.typography.labelSmall,
            color = QuranTheme.colors.secondaryText,
        )
        Text(
            text = buildAnnotatedString {
                append(suraNames)
                if (decoratedSuraName.isNotEmpty()) {
                    append(" ")
                    // A single private-use glyph; only legible in the sura-names font.
                    withStyle(SpanStyle(fontFamily = QuranFontFamilies.suraNames, fontSize = 18.sp)) {
                        append(decoratedSuraName)
                    }
                }
            },
            style = MaterialTheme.typography.labelSmall,
            color = QuranTheme.colors.secondaryText,
        )
    }
}
