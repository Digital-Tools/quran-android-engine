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
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.quranengine.model.quranannotations.Note
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.ui.audiobanner.AudioBannerView
import com.quranengine.ui.theme.MatchSystemBarsToChrome
import com.quranengine.ui.theme.QuranTheme
import com.quranengine.ui.theme.audioBannerBackground
import com.quranengine.ui.theme.chromeBackground
import com.quranengine.ui.theme.themedBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranViewScreen(
    state: QuranViewState,
    modifier: Modifier = Modifier,
    selectedAyahs: List<AyahNumber> = emptyList(),
    noteState: NoteState = NoteState.NO_HIGHLIGHT,
    highlightingColor: Note.Color = Note.Color.YELLOW,
    ayahMenuAnchor: Offset? = null,
    ayahMenuActions: AyahMenuActions = AyahMenuActions(),
    noteEditorAyah: AyahNumber? = null,
    footnote: TranslationFootnote? = null,
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
    onOpenPrayerSheet: () -> Unit = {},
    onManageTranslations: () -> Unit = {},
    onDismissNoteEditor: () -> Unit = {},
    onDismissFootnote: () -> Unit = {},
    onSaveNote: (AyahNumber, String) -> Unit = { _, _ -> },
    pageContent: @Composable () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showPageMenu by remember { mutableStateOf(false) }
    var pageMenuAnchor by remember { mutableStateOf<Offset?>(null) }
    var showPrayerTimesSheet by remember { mutableStateOf(false) }
    val view = LocalView.current
    val isPlaying = state.audioBannerState.isPlaying
    val chrome = QuranTheme.colors.chromeBackground()
    val dock = QuranTheme.colors.audioBannerBackground(QuranTheme.isDark)
    val pageBg = QuranTheme.colors.background

    // Fuse status / nav bars with mushaf chrome (same idea as iOS opaque bars).
    MatchSystemBarsToChrome(
        statusBarColor = if (state.barsVisible) chrome else pageBg,
        navigationBarColor = if (state.barsVisible) dock else pageBg,
    )

    DisposableEffect(isPlaying) {
        view.keepScreenOn = isPlaying
        onDispose {
            view.keepScreenOn = false
        }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QuranTheme.colors.chromeBackground())
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = QuranTheme.mizanGold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (state.isCurrentPageBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (state.isCurrentPageBookmarked) "Remove page bookmark" else "Save page bookmark",
                            tint = if (state.isCurrentPageBookmarked) QuranTheme.bookmark else QuranTheme.mizanGold
                        )
                    }
                    IconButton(
                        onClick = { showPageMenu = true },
                        modifier = Modifier.onGloballyPositioned { coords ->
                            pageMenuAnchor = coords.localToRoot(
                                Offset(coords.size.width / 2f, coords.size.height.toFloat()),
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = QuranTheme.mizanGold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 108.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
            }
        }

        if (footnote != null) {
            TranslationFootnoteSheet(footnote = footnote, onDismiss = onDismissFootnote)
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

        if (showPrayerTimesSheet) {
            PrayerTimesModalBottomSheet(
                onDismissRequest = { showPrayerTimesSheet = false },
            )
        }

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

        // Menus sit above the audio dock so their rows stay tappable.
        if (selectedAyahs.isNotEmpty()) {
            AyahMenuSheet(
                ayahs = selectedAyahs,
                anchorInRoot = ayahMenuAnchor,
                actions = ayahMenuActions,
                noteState = noteState,
                highlightingColor = highlightingColor,
            )
        }

        if (showPageMenu) {
            PageMenuSheet(
                pageNumber = state.currentPage,
                isBookmarked = state.isCurrentPageBookmarked,
                quranMode = state.quranMode,
                anchorInRoot = pageMenuAnchor,
                onToggleBookmark = onToggleBookmark,
                onToggleTranslations = onToggleMode,
                onManageTranslations = onManageTranslations,
                onOpenPrayerTimes = onOpenPrayerSheet,
                onDismiss = { showPageMenu = false },
            )
        }
    }
}
