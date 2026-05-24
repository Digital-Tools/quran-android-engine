package com.quranengine.features.quranview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = QuranTheme.colors.background,
        contentColor = QuranTheme.colors.text,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = "${ayah.sura.suraNumber}. ${ayah.sura.englishName()} — ${ayah.ayah}",
                style = MaterialTheme.typography.titleSmall,
                color = QuranTheme.colors.text.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )

            HorizontalDivider(
                color = QuranTheme.colors.text.copy(alpha = 0.12f),
                modifier = Modifier.padding(bottom = 4.dp),
            )

            AyahMenuItem(
                icon = Icons.Outlined.BookmarkAdd,
                label = "Bookmark",
                onClick = {
                    actions.onBookmarkPage(ayah)
                    actions.onDismiss()
                },
            )
            AyahMenuItem(
                icon = Icons.Outlined.PlayArrow,
                label = "Play from here",
                onClick = {
                    actions.onPlayFromHere(ayah)
                    actions.onDismiss()
                },
            )
            AyahMenuItem(
                icon = Icons.Outlined.Share,
                label = "Share",
                onClick = {
                    actions.onShare(ayah)
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
                icon = Icons.Outlined.NoteAdd,
                label = "Add Note",
                onClick = {
                    actions.onAddNote(ayah)
                    actions.onDismiss()
                },
            )
            AyahMenuItem(
                icon = Icons.Outlined.Translate,
                label = if (quranMode == QuranMode.ARABIC) "Show Translations" else "Show Arabic",
                onClick = {
                    actions.onToggleTranslations()
                    actions.onDismiss()
                },
            )
            AyahMenuItem(
                icon = Icons.Outlined.ManageSearch,
                label = "Manage Translations",
                onClick = {
                    actions.onManageTranslations()
                    actions.onDismiss()
                },
            )
        }
    }
}

@Composable
private fun AyahMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = QuranTheme.colors.text,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = QuranTheme.colors.text,
        )
    }
}
