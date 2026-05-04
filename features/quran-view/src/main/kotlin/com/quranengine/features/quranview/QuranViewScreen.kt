package com.quranengine.features.quranview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.ui.audiobanner.AudioBannerView
import com.quranengine.ui.theme.QuranTheme
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
                .padding(
                    top = if (state.barsVisible) 64.dp else 0.dp,
                    bottom = if (state.barsVisible) 136.dp else 0.dp,
                ),
        ) {
            pageContent()
        }

        // Top bar
        AnimatedVisibility(
            visible = state.barsVisible,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.title,
                            fontSize = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        )
                        if (state.subtitle.isNotEmpty()) {
                            Text(
                                text = state.subtitle,
                                fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
                                color = androidx.compose.ui.graphics.Color.Gray,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAudioPlayPause) {
                        Icon(
                            imageVector = if (state.audioBannerState.isPlaying) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = if (state.audioBannerState.isPlaying) {
                                "Pause audio"
                            } else {
                                "Play audio"
                            },
                        )
                    }
                    IconButton(onClick = onToggleMode) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = if (state.quranMode == com.quranengine.model.qurantext.QuranMode.ARABIC) {
                                "Show translation"
                            } else {
                                "Show Arabic"
                            },
                        )
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (state.isCurrentPageBookmarked) {
                                Icons.Default.Bookmark
                            } else {
                                Icons.Outlined.BookmarkBorder
                            },
                            contentDescription = if (state.isCurrentPageBookmarked) {
                                "Remove page bookmark"
                            } else {
                                "Save page bookmark"
                            },
                        )
                    }
                    IconButton(onClick = { menuAyah = state.firstVerse }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF121212),
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
                    actionIconContentColor = androidx.compose.ui.graphics.Color.White,
                ),
            )
        }

        // Ayah context menu
        val currentMenuAyah = menuAyah
        if (currentMenuAyah != null) {
            AyahMenuSheet(
                ayah = currentMenuAyah,
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
            state = state.audioBannerState.copy(isVisible = state.barsVisible && state.audioBannerState.isVisible),
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
