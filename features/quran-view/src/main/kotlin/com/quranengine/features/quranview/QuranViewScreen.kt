package com.quranengine.features.quranview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.ui.audiobanner.AudioBannerView
import com.quranengine.ui.theme.QuranTheme
import com.quranengine.ui.theme.chromeBackground
import com.quranengine.ui.theme.themedBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranViewScreen(
    state: QuranViewState,
    modifier: Modifier = Modifier,
    selectedAyah: AyahNumber? = null,
    ayahMenuActions: AyahMenuActions = AyahMenuActions(),
    noteEditorAyah: AyahNumber? = null,
    transientMessage: String? = null,
    onTransientMessageShown: () -> Unit = {},
    onToggleBars: () -> Unit = {},
    onBack: () -> Unit = {},
    onToggleMode: () -> Unit = {},
    onToggleBookmark: () -> Unit = {},
    onAudioPlayPause: () -> Unit = {},
    onAudioForward: () -> Unit = {},
    onAudioBackward: () -> Unit = {},
    onAudioStop: () -> Unit = {},
    onSetPlaybackRate: (Float) -> Unit = {},
    onAudioBannerTap: () -> Unit = {},
    onDismissNoteEditor: () -> Unit = {},
    onSaveNote: (AyahNumber, String) -> Unit = { _, _ -> },
    pageContent: @Composable () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var menuAyah by remember { mutableStateOf(selectedAyah) }
    val view = LocalView.current
    val isPlaying = state.audioBannerState.isPlaying

    DisposableEffect(isPlaying) {
        view.keepScreenOn = isPlaying
        onDispose {
            view.keepScreenOn = false
        }
    }

    LaunchedEffect(selectedAyah) {
        menuAyah = selectedAyah
    }

    LaunchedEffect(transientMessage) {
        transientMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onTransientMessageShown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .themedBackground()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onToggleBars,
            ),
    ) {
        // Page content (pager)
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            pageContent()
        }

        // Top bar — full-bleed chrome like iOS mushaf nav (not a floating pill).
        AnimatedVisibility(
            visible = state.barsVisible,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QuranTheme.colors.chromeBackground())
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = QuranTheme.mizanGold
                    )
                }

                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = QuranTheme.colors.text,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        maxLines = 1,
                    )
                    if (state.subtitle.isNotEmpty()) {
                        Text(
                            text = state.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = QuranTheme.colors.secondaryText,
                            maxLines = 1,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.audioBannerState.isVisible) {
                        IconButton(onClick = onAudioStop) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close audio",
                                tint = QuranTheme.mizanGold
                            )
                        }
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (state.isCurrentPageBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (state.isCurrentPageBookmarked) "Remove page bookmark" else "Save page bookmark",
                            tint = if (state.isCurrentPageBookmarked) MaterialTheme.colorScheme.error else QuranTheme.mizanGold
                        )
                    }
                    IconButton(onClick = { menuAyah = state.firstVerse }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = QuranTheme.mizanGold
                        )
                    }
                }
            }
        }

        // Ayah context menu
        val currentMenuAyah = menuAyah
        if (currentMenuAyah != null) {
            AyahMenuSheet(
                ayah = currentMenuAyah,
                quranMode = state.quranMode,
                actions = ayahMenuActions.copy(
                    onDismiss = {
                        menuAyah = null
                        ayahMenuActions.onDismiss()
                    },
                ),
            )
        }

        val currentNoteEditorAyah = noteEditorAyah
        if (currentNoteEditorAyah != null) {
            NoteEditorSheet(
                ayah = currentNoteEditorAyah,
                onDismiss = onDismissNoteEditor,
                onSave = { note -> onSaveNote(currentNoteEditorAyah, note) },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (state.barsVisible) 88.dp else 16.dp),
        )

        // Audio banner at bottom
        AudioBannerView(
            state = state.audioBannerState.copy(isVisible = state.barsVisible),
            modifier = Modifier.align(Alignment.BottomCenter),
            onPlayPause = onAudioPlayPause,
            onForward = onAudioForward,
            onBackward = onAudioBackward,
            onStop = onAudioStop,
            onBannerTap = onAudioBannerTap,
            onSetPlaybackRate = onSetPlaybackRate,
        )
    }
}
