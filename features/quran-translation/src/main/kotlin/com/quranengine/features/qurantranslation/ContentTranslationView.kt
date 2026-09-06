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
    onAyahTapped: (Int, Offset?) -> Unit = { _, _ -> },
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

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
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
                        onClick = { onAyahTapped(verseNum, verseAnchor) },
                        onLongClick = { onAyahTapped(verseNum, verseAnchor) },
                    )
            } else {
                highlightModifier.fillMaxWidth()
            }
            Box(modifier = clickableModifier) {
                when (item) {
                    is TranslationItem.PageHeader -> {
                        QuranPageHeader(
                            quarterName = "",
                            suraNames = "Page ${item.page}",
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    is TranslationItem.PageFooter -> {
                        QuranPageFooter(pageNumber = item.page.toString())
                    }
                    is TranslationItem.VerseSeparator -> {
                        QuranVerseSeparator(
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    is TranslationItem.SuraName -> {
                        QuranSuraName(
                            suraName = item.suraName,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    }
                    is TranslationItem.ArabicText -> {
                        QuranArabicText(
                            text = item.text,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    is TranslationItem.TranslatorName -> {
                        QuranTranslatorName(name = item.name)
                    }
                    is TranslationItem.TranslationReferenceVerse -> {
                        Text(
                            text = "See verse ${item.referenceVerse}",
                            style = MaterialTheme.typography.bodySmall,
                            color = QuranTheme.colors.secondaryText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    is TranslationItem.TranslationTextChunk -> {
                        QuranTranslationTextChunk(
                            text = item.text,
                            isArabic = item.isArabic,
                            showReadMore = item.showReadMore,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
