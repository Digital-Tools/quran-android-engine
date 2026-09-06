package com.quranengine.features.quranview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurantext.QuranMode
import com.quranengine.ui.theme.QuranTheme
import com.quranengine.ui.theme.chromeBackground

@Composable
fun AyahMenuSheet(
    ayah: AyahNumber,
    actions: AyahMenuActions,
    @Suppress("UNUSED_PARAMETER") quranMode: QuranMode = QuranMode.ARABIC,
    anchorInRoot: Offset? = null,
    modifier: Modifier = Modifier,
) {
    val menuColor = QuranTheme.colors.chromeBackground()
    val density = LocalDensity.current
    val cardWidth = 300.dp
    val estimatedHeight = 300.dp

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions.onDismiss,
                ),
        )

        val (xOffset, yOffset) = if (anchorInRoot != null) {
            val tapX = with(density) { anchorInRoot.x.toDp() }
            val tapY = with(density) { anchorInRoot.y.toDp() }
            val maxLeft = (maxWidth - cardWidth - 16.dp).coerceAtLeast(16.dp)
            val left = (tapX - cardWidth / 2).coerceIn(16.dp, maxLeft)
            val belowY = tapY + 18.dp
            val aboveY = (tapY - estimatedHeight - 12.dp).coerceAtLeast(16.dp)
            val top = if (belowY + estimatedHeight < maxHeight - 72.dp) belowY else aboveY
            left to top
        } else {
            val left = ((maxWidth - cardWidth) / 2).coerceAtLeast(16.dp)
            val top = (maxHeight - estimatedHeight - 96.dp).coerceAtLeast(16.dp)
            left to top
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = xOffset, y = yOffset)
                .width(cardWidth)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = menuColor,
            contentColor = QuranTheme.colors.text,
            tonalElevation = 0.dp,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 6.dp),
            ) {
                Text(
                    text = "${ayah.sura.suraNumber}. ${ayah.sura.englishName()} — Ayah ${ayah.ayah}",
                    style = MaterialTheme.typography.titleSmall,
                    color = QuranTheme.colors.text.copy(alpha = 0.55f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )

                HorizontalDivider(color = QuranTheme.colors.text.copy(alpha = 0.08f))

                AyahMenuItem(
                    icon = Icons.Outlined.PlayArrow,
                    label = "Play",
                    subtitle = "this verse",
                    onClick = {
                        actions.onPlayFromHere(ayah)
                        actions.onDismiss()
                    },
                )
                AyahMenuItem(
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

                AyahMenuItem(
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
                AyahMenuItem(
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
                    modifier = Modifier.padding(vertical = 4.dp),
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
            .padding(horizontal = 16.dp, vertical = 9.dp),
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
        Spacer(modifier = Modifier.width(14.dp))
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
