package com.quranengine.ui.quran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranFontFamilies
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranTranslationTextChunk(
    text: String,
    modifier: Modifier = Modifier,
    isArabic: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    showReadMore: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }

    val textStyle = if (isArabic) {
        MaterialTheme.typography.bodyLarge.copy(
            fontFamily = QuranFontFamilies.arabicTafseer,
            textAlign = TextAlign.Start,
            textDirection = TextDirection.Rtl,
        )
    } else {
        MaterialTheme.typography.bodyMedium
    }

    val effectiveMaxLines = if (expanded || !showReadMore) Int.MAX_VALUE else maxLines

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = text,
            style = textStyle,
            color = if (isArabic) QuranTheme.colors.arabicText else QuranTheme.colors.text,
            maxLines = effectiveMaxLines,
            overflow = if (showReadMore && !expanded) TextOverflow.Ellipsis else TextOverflow.Clip,
        )

        if (showReadMore && !expanded) {
            Text(
                text = "Read more",
                style = MaterialTheme.typography.bodySmall,
                color = QuranTheme.mizanGold,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(top = 4.dp),
            )
        }
    }
}
