package com.quranengine.ui.quran

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import com.quranengine.ui.theme.QuranFontFamilies
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranArabicText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = style.copy(
            fontFamily = QuranFontFamilies.quranText,
            fontSize = QuranTheme.fontSize.quranTextSize(),
            textAlign = TextAlign.Center,
            textDirection = TextDirection.Rtl,
        ),
        color = QuranTheme.colors.arabicText,
    )
}
