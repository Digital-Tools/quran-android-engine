package com.quranengine.ui.quran

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranengine.ui.theme.QuranFontFamilies
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranSuraName(
    suraName: String,
    modifier: Modifier = Modifier,
    showBasmala: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Sura name with decorative font
        Text(
            text = suraName,
            style = TextStyle(
                fontFamily = QuranFontFamilies.suraNames,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                textDirection = TextDirection.Rtl,
            ),
            color = QuranTheme.colors.text,
        )

        if (showBasmala) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\u0628\u0650\u0633\u0652\u0645\u0650 \u0671\u0644\u0644\u0651\u064E\u0647\u0650 \u0671\u0644\u0631\u0651\u064E\u062D\u0652\u0645\u064E\u0640\u0670\u0646\u0650 \u0671\u0644\u0631\u0651\u064E\u062D\u0650\u064A\u0645\u0650",
                style = TextStyle(
                    fontFamily = QuranFontFamilies.quranText,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    textDirection = TextDirection.Rtl,
                ),
                color = QuranTheme.colors.text,
            )
        }
    }
}
