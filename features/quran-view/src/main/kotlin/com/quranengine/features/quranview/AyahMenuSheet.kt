package com.quranengine.features.quranview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurantext.QuranMode
import com.quranengine.ui.theme.QuranTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahMenuSheet(
    ayah: AyahNumber,
    actions: AyahMenuActions,
    quranMode: QuranMode = QuranMode.ARABIC,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = actions.onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = QuranTheme.colors.secondaryBackground,
        contentColor = QuranTheme.colors.text,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            // Header: Surah name & Ayah number
            Text(
                text = "${ayah.sura.suraNumber}. ${ayah.sura.englishName()} — Ayah ${ayah.ayah}",
                style = MaterialTheme.typography.titleSmall,
                color = QuranTheme.colors.text.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            HorizontalDivider(
                color = QuranTheme.colors.text.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 4.dp),
            )

            // Group 1: Playback Controls
            AyahMenuItem(
                icon = Icons.Outlined.PlayArrow,
                label = "Play",
                subtitle = "to the end of Juz'",
                onClick = {
                    actions.onPlayFromHere(ayah)
                    actions.onDismiss()
                },
            )
            AyahMenuItem(
                icon = Icons.Outlined.Repeat,
                label = "Repeat",
                subtitle = "selected verse",
                onClick = {
                    actions.onRepeatVerse(ayah)
                    actions.onDismiss()
                },
            )

            HorizontalDivider(
                color = QuranTheme.colors.text.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 6.dp),
            )

            // Group 2: Highlights & Annotations
            AyahMenuItem(
                label = "Highlight",
                customIcon = {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF6B8B)), // Rose/Pink highlight
                    )
                },
                onClick = {
                    actions.onHighlight(ayah)
                    actions.onDismiss()
                },
            )
            AyahMenuItem(
                label = "Highlight",
                subtitle = "select color",
                customIcon = {
                    // Split dual color dot (Green/Yellow) matching iOS design
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
            AyahMenuItem(
                icon = Icons.Outlined.NoteAdd,
                label = "Add Note",
                onClick = {
                    actions.onAddNote(ayah)
                    actions.onDismiss()
                },
            )

            HorizontalDivider(
                color = QuranTheme.colors.text.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 6.dp),
            )

            // Group 3: Tools & Actions
            AyahMenuItem(
                icon = Icons.Outlined.AccessTime,
                label = "Prayer Times",
                subtitle = "view schedule",
                onClick = {
                    actions.onOpenPrayerTimes()
                    actions.onDismiss()
                },
            )
            AyahMenuItem(
                icon = Icons.Outlined.Language,
                label = "Translation/Tafseer",
                onClick = {
                    actions.onTranslationTafseer(ayah)
                    actions.onDismiss()
                },
            )
            AyahMenuItem(
                icon = Icons.Outlined.ContentCopy,
                label = "Copy",
                onClick = {
                    actions.onCopy(ayah)
                    actions.onDismiss()
                },
            )
            AyahMenuItem(
                icon = Icons.Outlined.Share,
                label = "Share...",
                onClick = {
                    actions.onShare(ayah)
                    actions.onDismiss()
                },
            )
        }
    }
}

@Composable
private fun AyahMenuItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    customIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp),
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (customIcon != null) {
                customIcon()
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = QuranTheme.colors.text,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = QuranTheme.colors.text,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuranTheme.colors.text.copy(alpha = 0.5f),
                )
            }
        }
    }
}
