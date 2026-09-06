package com.quranengine.ui.quran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.quranengine.ui.theme.QuranFontFamilies
import com.quranengine.ui.theme.QuranTheme

@Composable
fun QuranTranslationTextChunk(
    text: String,
    modifier: Modifier = Modifier,
    isArabic: Boolean = false,
    readMoreAt: Int? = null,
    readMoreLabel: String = "",
    quranRanges: List<IntRange> = emptyList(),
    footnoteRanges: List<IntRange> = emptyList(),
    onFootnoteClick: (Int) -> Unit = {},
) {
    var expanded by rememberSaveable(text) { mutableStateOf(false) }
    val truncated = readMoreAt != null && !expanded

    // Cutting at `readMoreAt` keeps a prefix, so the annotation offsets stay valid; spans past
    // the cut are simply not visible.
    val visible = if (truncated) text.substring(0, readMoreAt) else text
    val visibleQuranRanges = quranRanges.filter { it.last < visible.length }
    val visibleFootnoteRanges = footnoteRanges.filter { it.last < visible.length }

    val textStyle = if (isArabic) {
        MaterialTheme.typography.bodyLarge.copy(
            fontFamily = QuranFontFamilies.arabicTafseer,
            textAlign = TextAlign.Right,
            textDirection = TextDirection.Rtl,
        )
    } else {
        MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Start,
            textDirection = TextDirection.Ltr,
        )
    }

    val gold = QuranTheme.mizanGold
    val annotated = remember(visible, visibleQuranRanges, visibleFootnoteRanges, gold) {
        annotate(visible, visibleQuranRanges, visibleFootnoteRanges, gold)
    }

    // Resolving the tap against the laid-out text keeps the surrounding long-press (which opens
    // the ayah menu) working; a link annotation would swallow the whole gesture.
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val footnoteTaps = if (visibleFootnoteRanges.isEmpty()) {
        Modifier
    } else {
        Modifier.pointerInput(visibleFootnoteRanges) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val up = waitForUpOrCancellation() ?: return@awaitEachGesture
                if (up.uptimeMillis - down.uptimeMillis > viewConfiguration.longPressTimeoutMillis) {
                    return@awaitEachGesture
                }
                val offset = layout?.getOffsetForPosition(up.position) ?: return@awaitEachGesture
                val index = visibleFootnoteRanges.indexOfFirst { offset in it }
                if (index >= 0) {
                    up.consume()
                    onFootnoteClick(index)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = annotated,
            modifier = Modifier
                .fillMaxWidth()
                .then(footnoteTaps),
            style = textStyle,
            color = if (isArabic) QuranTheme.colors.arabicText else QuranTheme.colors.text,
            onTextLayout = { layout = it },
        )

        if (truncated) {
            Text(
                text = readMoreLabel,
                style = MaterialTheme.typography.bodySmall,
                color = gold,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(top = 4.dp),
            )
        }
    }
}

/** Quran quotes are tinted gold and footnote markers raised, mirroring iOS. */
private fun annotate(
    text: String,
    quranRanges: List<IntRange>,
    footnoteRanges: List<IntRange>,
    gold: androidx.compose.ui.graphics.Color,
): AnnotatedString {
    if (quranRanges.isEmpty() && footnoteRanges.isEmpty()) {
        return AnnotatedString(text)
    }
    return buildAnnotatedString {
        append(text)
        for (range in quranRanges) {
            val end = (range.last + 1).coerceAtMost(text.length)
            if (range.first in text.indices && end > range.first) {
                addStyle(SpanStyle(color = gold), range.first, end)
            }
        }
        for (range in footnoteRanges) {
            val end = (range.last + 1).coerceAtMost(text.length)
            if (range.first in text.indices && end > range.first) {
                addStyle(
                    SpanStyle(
                        color = gold,
                        fontSize = 0.75.em,
                        baselineShift = BaselineShift.Superscript,
                    ),
                    range.first,
                    end,
                )
            }
        }
    }
}
