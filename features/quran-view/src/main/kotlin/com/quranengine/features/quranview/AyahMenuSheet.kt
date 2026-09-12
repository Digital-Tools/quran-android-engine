package com.quranengine.features.quranview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.model.quranannotations.Note
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.ui.theme.QuranTheme

/** Maps Note.Color enum values to Compose colors, matching the iOS palette. */
private fun Note.Color.toComposeColor(): Color = when (this) {
    Note.Color.RED -> Color(0xFFFF6B8B)
    Note.Color.GREEN -> Color(0xFFC1EC71)
    Note.Color.BLUE -> Color(0xFFADD7FE)
    Note.Color.YELLOW -> Color(0xFFFDEC63)
    Note.Color.PURPLE -> Color(0xFFD8B1FE)
}

private val noteColorOrder = listOf(
    Note.Color.YELLOW,
    Note.Color.GREEN,
    Note.Color.BLUE,
    Note.Color.RED,
    Note.Color.PURPLE,
)

private enum class MenuMode { LIST, COLOR_PICKER }

/** What the existing note state is for the selected verse(s). */
enum class NoteState { NO_HIGHLIGHT, HIGHLIGHTED, NOTED }

/** Verse-scoped actions, opened by long-pressing an ayah. */
@Composable
fun AyahMenuSheet(
    ayahs: List<AyahNumber>,
    actions: AyahMenuActions,
    noteState: NoteState = NoteState.NO_HIGHLIGHT,
    highlightingColor: Note.Color = Note.Color.YELLOW,
    anchorInRoot: Offset? = null,
    modifier: Modifier = Modifier,
) {
    var menuMode by remember { mutableStateOf(MenuMode.LIST) }

    ReaderMenuPopover(
        anchorInRoot = anchorInRoot,
        onDismiss = actions.onDismiss,
        modifier = modifier,
    ) {
        AnimatedContent(
            targetState = menuMode,
            transitionSpec = {
                if (targetState == MenuMode.COLOR_PICKER) {
                    (scaleIn(initialScale = 2f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.8f) + fadeOut())
                } else {
                    (fadeIn()) togetherWith (fadeOut())
                }
            },
            label = "AyahMenuTransition",
        ) { mode ->
            when (mode) {
                MenuMode.LIST -> AyahMenuList(
                    ayahs = ayahs,
                    actions = actions,
                    noteState = noteState,
                    highlightingColor = highlightingColor,
                    onShowColorPicker = { menuMode = MenuMode.COLOR_PICKER },
                )
                MenuMode.COLOR_PICKER -> NoteColorPicker(
                    selectedColor = when (noteState) {
                        NoteState.NO_HIGHLIGHT -> null
                        else -> highlightingColor
                    },
                    onColorSelected = { color ->
                        actions.onSelectHighlightColor(ayahs, color)
                        actions.onDismiss()
                    },
                )
            }
        }
    }
}

@Composable
private fun AyahMenuList(
    ayahs: List<AyahNumber>,
    actions: AyahMenuActions,
    noteState: NoteState,
    highlightingColor: Note.Color,
    onShowColorPicker: () -> Unit,
) {
    val firstAyah = ayahs.first()
    val title = if (ayahs.size > 1) {
        "${firstAyah.sura.suraNumber}. ${firstAyah.sura.englishName()} — Ayah ${firstAyah.ayah}–${ayahs.last().ayah}"
    } else {
        "${firstAyah.sura.suraNumber}. ${firstAyah.sura.englishName()} — Ayah ${firstAyah.ayah}"
    }
    val verseLabel = if (ayahs.size > 1) "selected verses" else "this verse"

    ReaderMenuTitle(title)

    HorizontalDivider(color = QuranTheme.colors.text.copy(alpha = 0.08f))

    // --- Play / Repeat ---
    ReaderMenuItem(
        icon = Icons.Outlined.PlayArrow,
        label = "Play",
        subtitle = verseLabel,
        onClick = {
            actions.onPlayFromHere(ayahs)
            actions.onDismiss()
        },
    )
    ReaderMenuItem(
        icon = Icons.Outlined.Repeat,
        label = "Repeat",
        subtitle = verseLabel,
        onClick = {
            actions.onRepeatVerse(ayahs)
            actions.onDismiss()
        },
    )

    HorizontalDivider(
        color = QuranTheme.colors.text.copy(alpha = 0.08f),
        modifier = Modifier.padding(vertical = 4.dp),
    )

    // --- Highlight (quick, last-used color) ---
    if (noteState == NoteState.NO_HIGHLIGHT) {
        ReaderMenuItem(
            label = "Highlight",
            customIcon = {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(highlightingColor.toComposeColor()),
                )
            },
            onClick = {
                actions.onHighlight(ayahs)
                actions.onDismiss()
            },
        )
    }

    // --- Highlight — select color ---
    ReaderMenuItem(
        label = "Highlight",
        subtitle = "select color",
        customIcon = { TriColorCircle() },
        onClick = onShowColorPicker,
    )

    // --- Notes ---
    when (noteState) {
        NoteState.NO_HIGHLIGHT, NoteState.HIGHLIGHTED -> {
            ReaderMenuItem(
                icon = Icons.Outlined.NoteAdd,
                label = "Add Note",
                onClick = {
                    actions.onAddNote(ayahs)
                    actions.onDismiss()
                },
            )
        }
        NoteState.NOTED -> {
            ReaderMenuItem(
                icon = Icons.Outlined.Edit,
                label = "Edit Note",
                onClick = {
                    actions.onAddNote(ayahs)
                    actions.onDismiss()
                },
            )
        }
    }

    if (noteState != NoteState.NO_HIGHLIGHT) {
        ReaderMenuItem(
            icon = Icons.Outlined.Delete,
            label = if (noteState == NoteState.NOTED) "Delete Note" else "Delete Highlight",
            iconTint = Color(0xFFEF5350),
            onClick = {
                actions.onDeleteNote(ayahs)
                actions.onDismiss()
            },
        )
    }

    HorizontalDivider(
        color = QuranTheme.colors.text.copy(alpha = 0.08f),
        modifier = Modifier.padding(vertical = 4.dp),
    )

    // --- Translation / Copy / Share ---
    ReaderMenuItem(
        icon = Icons.Outlined.Language,
        label = "Translation/Tafseer",
        onClick = {
            actions.onTranslationTafseer(ayahs)
            actions.onDismiss()
        },
    )
    ReaderMenuItem(
        icon = Icons.Outlined.ContentCopy,
        label = "Copy",
        onClick = {
            actions.onCopy(ayahs)
            actions.onDismiss()
        },
    )
    ReaderMenuItem(
        icon = Icons.Outlined.Share,
        label = "Share...",
        onClick = {
            actions.onShare(ayahs)
            actions.onDismiss()
        },
    )
}

/** Tri-color circle icon for "select color" row, mirroring iOS IconCircles. */
@Composable
private fun TriColorCircle() {
    Row(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Note.Color.GREEN.toComposeColor()),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Note.Color.YELLOW.toComposeColor()),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Note.Color.PURPLE.toComposeColor()),
        )
    }
}

/** 5-circle color picker, matching iOS NoteCircles. */
@Composable
private fun NoteColorPicker(
    selectedColor: Note.Color?,
    onColorSelected: (Note.Color) -> Unit,
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        noteColorOrder.forEach { color ->
            val isSelected = color == selectedColor
            val composeColor = color.toComposeColor()
            Box(
                modifier = Modifier
                    .size(if (isSelected) 36.dp else 32.dp)
                    .clip(CircleShape)
                    .background(composeColor)
                    .then(
                        if (isSelected) {
                            Modifier.drawBehind {
                                drawCircle(
                                    color = Color.White,
                                    radius = size.minDimension / 4f,
                                    center = center,
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onColorSelected(color) },
            )
        }
    }
}
