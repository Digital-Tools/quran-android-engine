package com.quranengine.features.quranview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.ui.theme.QuranTheme

/** Verse-scoped actions, opened by long-pressing an ayah. */
@Composable
fun AyahMenuSheet(
    ayah: AyahNumber,
    actions: AyahMenuActions,
    anchorInRoot: Offset? = null,
    modifier: Modifier = Modifier,
) {
    ReaderMenuPopover(
        anchorInRoot = anchorInRoot,
        onDismiss = actions.onDismiss,
        modifier = modifier,
    ) {
        ReaderMenuTitle("${ayah.sura.suraNumber}. ${ayah.sura.englishName()} — Ayah ${ayah.ayah}")

        HorizontalDivider(color = QuranTheme.colors.text.copy(alpha = 0.08f))

        ReaderMenuItem(
            icon = Icons.Outlined.PlayArrow,
            label = "Play",
            subtitle = "this verse",
            onClick = {
                actions.onPlayFromHere(ayah)
                actions.onDismiss()
            },
        )
        ReaderMenuItem(
            icon = Icons.Outlined.Repeat,
            label = "Repeat",
            subtitle = "this verse",
            onClick = {
                actions.onRepeatVerse(ayah)
                actions.onDismiss()
            },
        )

        HorizontalDivider(
            color = QuranTheme.colors.text.copy(alpha = 0.08f),
            modifier = Modifier.padding(vertical = 4.dp),
        )

        ReaderMenuItem(
            label = "Highlight",
            customIcon = {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B8B)),
                )
            },
            onClick = {
                actions.onHighlight(ayah)
                actions.onDismiss()
            },
        )
        ReaderMenuItem(
            label = "Highlight",
            subtitle = "select color",
            customIcon = {
                Row(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFF8BC34A)),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(0xFFFFD54F)),
                    )
                }
            },
            onClick = {
                actions.onSelectHighlightColor(ayah)
                actions.onDismiss()
            },
        )
        ReaderMenuItem(
            icon = Icons.Outlined.NoteAdd,
            label = "Add Note",
            onClick = {
                actions.onAddNote(ayah)
                actions.onDismiss()
            },
        )

        HorizontalDivider(
            color = QuranTheme.colors.text.copy(alpha = 0.08f),
            modifier = Modifier.padding(vertical = 4.dp),
        )

        ReaderMenuItem(
            icon = Icons.Outlined.Language,
            label = "Translation/Tafseer",
            onClick = {
                actions.onTranslationTafseer(ayah)
                actions.onDismiss()
            },
        )
        ReaderMenuItem(
            icon = Icons.Outlined.ContentCopy,
            label = "Copy",
            onClick = {
                actions.onCopy(ayah)
                actions.onDismiss()
            },
        )
        ReaderMenuItem(
            icon = Icons.Outlined.Share,
            label = "Share...",
            onClick = {
                actions.onShare(ayah)
                actions.onDismiss()
            },
        )
    }
}
