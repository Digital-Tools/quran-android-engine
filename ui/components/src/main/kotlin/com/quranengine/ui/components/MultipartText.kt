package com.quranengine.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.quranengine.ui.theme.QuranColors
import com.quranengine.ui.theme.QuranFontFamilies
import com.quranengine.ui.theme.QuranTheme

sealed class MultipartTextPart {
    data class Plain(val text: String) : MultipartTextPart()
    data class Highlighted(
        val text: String,
        val color: Color = Color.Unspecified,
        val fontWeight: FontWeight = FontWeight.Bold,
    ) : MultipartTextPart()
    data class SuraName(val text: String) : MultipartTextPart()
    data class VerseText(val text: String, val fontSize: TextUnit = TextUnit.Unspecified) : MultipartTextPart()
}

@Composable
fun MultipartText(
    parts: List<MultipartTextPart>,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val textColor = QuranTheme.colors.text

    val annotatedString = buildAnnotatedString {
        for (part in parts) {
            when (part) {
                is MultipartTextPart.Plain -> {
                    append(part.text)
                }
                is MultipartTextPart.Highlighted -> {
                    val highlightColor = if (part.color == Color.Unspecified) {
                        QuranColors.searchHighlight
                    } else {
                        part.color
                    }
                    pushStyle(SpanStyle(fontWeight = part.fontWeight, background = highlightColor))
                    append(part.text)
                    pop()
                }
                is MultipartTextPart.SuraName -> {
                    pushStyle(SpanStyle(fontFamily = QuranFontFamilies.suraNames))
                    append(part.text)
                    pop()
                }
                is MultipartTextPart.VerseText -> {
                    pushStyle(SpanStyle(
                        fontFamily = QuranFontFamilies.quranText,
                        fontSize = part.fontSize,
                    ))
                    append(part.text)
                    pop()
                }
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = textColor,
        textAlign = textAlign,
    )
}
