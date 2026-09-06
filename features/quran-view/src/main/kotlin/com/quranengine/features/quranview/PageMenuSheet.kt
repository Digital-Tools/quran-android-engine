package com.quranengine.features.quranview

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.quranengine.model.qurantext.QuranMode
import com.quranengine.ui.theme.QuranTheme

/** Page-scoped actions, opened from the reader's top-bar overflow button. */
@Composable
fun PageMenuSheet(
    pageNumber: Int,
    isBookmarked: Boolean,
    quranMode: QuranMode,
    onToggleBookmark: () -> Unit,
    onToggleTranslations: () -> Unit,
    onManageTranslations: () -> Unit,
    onOpenPrayerTimes: () -> Unit,
    onDismiss: () -> Unit,
    anchorInRoot: Offset? = null,
    modifier: Modifier = Modifier,
) {
    ReaderMenuPopover(
        anchorInRoot = anchorInRoot,
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        ReaderMenuTitle("Page $pageNumber")

        HorizontalDivider(color = QuranTheme.colors.text.copy(alpha = 0.08f))

        ReaderMenuItem(
            icon = Icons.Outlined.Language,
            label = when (quranMode) {
                QuranMode.ARABIC -> "Show Translations"
                QuranMode.TRANSLATION -> "Show Arabic"
            },
            onClick = {
                onToggleTranslations()
                onDismiss()
            },
        )
        ReaderMenuItem(
            icon = Icons.Outlined.Tune,
            label = "Manage Translations",
            onClick = {
                onManageTranslations()
                onDismiss()
            },
        )

        HorizontalDivider(
            color = QuranTheme.colors.text.copy(alpha = 0.08f),
            modifier = Modifier.padding(vertical = 4.dp),
        )

        ReaderMenuItem(
            icon = if (isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
            label = if (isBookmarked) "Remove Bookmark" else "Bookmark Page",
            onClick = {
                onToggleBookmark()
                onDismiss()
            },
        )
        ReaderMenuItem(
            icon = Icons.Outlined.AccessTime,
            label = "Prayer Times",
            subtitle = "view schedule",
            onClick = {
                onOpenPrayerTimes()
                onDismiss()
            },
        )
    }
}
