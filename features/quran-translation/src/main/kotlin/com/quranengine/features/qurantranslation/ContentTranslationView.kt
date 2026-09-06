package com.quranengine.features.qurantranslation

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.quranengine.ui.quran.*
import com.quranengine.ui.theme.QuranColors
import com.quranengine.ui.theme.QuranTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentTranslationView(
    items: List<TranslationItem>,
    modifier: Modifier = Modifier,
    scrollToItemId: TranslationItemId? = null,
    selectedVerse: Int? = null,
    onTap: () -> Unit = {},
    onAyahLongPressed: (Int, Offset?) -> Unit = { _, _ -> },
    onFootnoteClick: (index: Int, text: String) -> Unit = { _, _ -> },
) {
    val listState = rememberLazyListState()

    // Scroll to target item when requested
    LaunchedEffect(scrollToItemId) {
        if (scrollToItemId != null) {
            val index = items.indexOfFirst { it.id == scrollToItemId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    // The top bar and audio banner float over the page, so without this the header would sit
    // behind the status bar and the footer under the navigation bar. Mirrors the safe-area plus
    // spacing that ContentDimension.readableInsets gives the iOS list.
    val systemBars = WindowInsets.systemBars.asPaddingValues()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = systemBars.calculateTopPadding() + 12.dp,
            bottom = systemBars.calculateBottomPadding() + 24.dp,
        ),
    ) {
        itemsIndexed(
            items = items,
            key = { index, item -> "$index-${item.id.hashCode()}" },
        ) { _, item ->
            val bgModifier = if (item.highlightColor != null) {
                Modifier.background(item.highlightColor!!)
            } else {
                Modifier
            }

            val verseNum = item.id.ayah
            val isSelected = verseNum != null && verseNum == selectedVerse
            val highlightModifier = if (isSelected) {
                Modifier.background(QuranColors.wordHighlight)
            } else {
                bgModifier
            }
            var verseAnchor by remember(item.id) { mutableStateOf<Offset?>(null) }
            val clickableModifier = if (verseNum != null) {
                highlightModifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        verseAnchor = coords.localToRoot(
                            Offset(coords.size.width / 2f, coords.size.height.toFloat()),
                        )
                    }
                    .combinedClickable(
                        onClick = { onTap() },
                        onLongClick = { onAyahLongPressed(verseNum, verseAnchor) },
                    )
            } else {
                highlightModifier.fillMaxWidth()
            }
            Box(modifier = clickableModifier) {
                when (item) {
                    is TranslationItem.PageHeader -> {
                        QuranPageHeader(
                            quarterName = item.quarterName,
                            suraNames = item.suraNames,
                            decoratedSuraName = item.decoratedSuraName,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    is TranslationItem.PageFooter -> {
                        QuranPageFooter(pageNumber = item.page.toString())
                    }
                    is TranslationItem.VerseSeparator -> {
                        QuranVerseSeparator()
                    }
                    is TranslationItem.SuraName -> {
                        QuranSuraName(
                            suraName = item.suraName,
                            showBasmala = item.showBasmala,
                        )
                    }
                    is TranslationItem.ArabicText -> {
                        QuranArabicText(
                            text = item.text,
                            ayahLabel = item.ayahLabel,
                        )
                    }
                    is TranslationItem.TranslatorName -> {
                        QuranTranslatorName(name = item.name)
                    }
                    is TranslationItem.TranslationReferenceVerse -> {
                        Text(
                            text = "See ayah ${item.referenceVerse}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = QuranTheme.colors.secondaryText,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp),
                        )
                    }
                    is TranslationItem.TranslationTextChunk -> {
                        QuranTranslationTextChunk(
                            text = item.text,
                            isArabic = item.isArabic,
                            readMoreAt = item.readMoreAt,
                            readMoreLabel = item.readMoreLabel,
                            quranRanges = item.quranRanges,
                            footnoteRanges = item.footnoteRanges,
                            onFootnoteClick = { index ->
                                item.footnotes.getOrNull(index)?.let { onFootnoteClick(index, it) }
                            },
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }
        }
    }
}
