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
    pageContent: @Composable () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

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
        Box(modifier = Modifier.fillMaxSize()) {
            pageContent()
        }

        // Top bar
        AnimatedVisibility(
            visible = state.barsVisible,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.title,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (state.subtitle.isNotEmpty()) {
                            Text(
                                text = state.subtitle,
                                style = MaterialTheme.typography.bodySmall,
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
                    IconButton(onClick = { /* menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = QuranTheme.colors.background.copy(alpha = 0.95f),
                    titleContentColor = QuranTheme.colors.text,
                    navigationIconContentColor = QuranTheme.colors.text,
                    actionIconContentColor = QuranTheme.colors.text,
                ),
            )
        }

        // Ayah long-press context menu
        if (selectedAyah != null) {
            AyahMenuSheet(
                ayah = selectedAyah,
                actions = ayahMenuActions,
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
        AnimatedVisibility(
            visible = state.barsVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            AudioBannerView(
                state = state.audioBannerState,
                onPlayPause = onAudioPlayPause,
                onForward = onAudioForward,
                onBackward = onAudioBackward,
                onStop = onAudioStop,
                onBannerTap = onAudioBannerTap,
                onSetPlaybackRate = onSetPlaybackRate,
            )
        }
    }
}
