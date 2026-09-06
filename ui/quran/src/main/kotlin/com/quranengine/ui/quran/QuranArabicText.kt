package com.quranengine.ui.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranFontFamilies
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranArabicText(
    text: String,
    modifier: Modifier = Modifier,
    ayahLabel: String? = null,
    style: TextStyle = TextStyle.Default,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 5.dp),
    ) {
        if (ayahLabel != null) {
            Text(
                text = ayahLabel,
                style = MaterialTheme.typography.labelMedium,
                color = QuranTheme.colors.secondaryText,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(QuranTheme.colors.secondaryBackground)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = style.copy(
                fontFamily = QuranFontFamilies.quranText,
                fontSize = QuranTheme.fontSize.quranTextSize(),
                textAlign = TextAlign.Right,
                textDirection = TextDirection.Rtl,
            ),
            color = QuranTheme.colors.arabicText,
        )
    }
}
