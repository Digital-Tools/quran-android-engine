package com.quranengine.ui.quran

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranengine.ui.theme.QuranFontFamilies
import com.quranengine.ui.theme.QuranTheme

private const val ARABIC_BESM_ALLAH = "\u0628\u0650\u0633\u0652\u0645\u0650 \u0671\u0644\u0644\u0651\u064E" +
    "\u0647\u0650 \u0671\u0644\u0631\u0651\u064E\u062D\u0652\u0645\u064E\u0640\u0670\u0646\u0650 " +
    "\u0671\u0644\u0631\u0651\u064E\u062D\u0650\u064A\u0645\u0650"

@Composable
fun QuranSuraName(
    suraName: String,
    modifier: Modifier = Modifier,
    showBasmala: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.quran_sura_header),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(QuranTheme.colors.text),
            )
            Text(
                text = suraName,
                style = MaterialTheme.typography.titleMedium,
                color = QuranTheme.colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 56.dp),
            )
        }

        if (showBasmala) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ARABIC_BESM_ALLAH,
                style = TextStyle(
                    fontFamily = QuranFontFamilies.quranText,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    textDirection = TextDirection.Rtl,
                ),
                color = QuranTheme.colors.arabicText,
            )
        }
    }
}
