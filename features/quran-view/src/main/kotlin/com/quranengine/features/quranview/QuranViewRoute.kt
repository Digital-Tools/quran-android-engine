package com.quranengine.features.quranview

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.quranengine.core.audioplayer.Runs
import com.quranengine.features.audiobanner.AudioBannerViewModel
import com.quranengine.features.qurancontent.ContentPageView
import com.quranengine.features.quranimage.ContentImageView
import com.quranengine.features.quranpages.PagingStrategy
import com.quranengine.features.quranpages.QuranPaginationView
import com.quranengine.features.qurantranslation.ContentTranslationView
import com.quranengine.features.qurantranslation.TranslationItemId
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.arrayTo
import com.quranengine.model.qurankit.lastayahfinder.JuzBasedLastAyahFinder
import com.quranengine.model.qurantext.QuranMode
import com.quranengine.ui.components.DataUnavailableView

@Composable
fun QuranViewRoute(
    viewModel: QuranViewViewModel,
    audioBannerViewModel: AudioBannerViewModel,
    onBack: () -> Unit,
    onNavigateToAdvancedAudio: (AyahNumber, AyahNumber) -> Unit,
    onNavigateToTranslations: () -> Unit = {},
    onOpenPrayerSheet: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val pageContentStates by viewModel.pageContentStates.collectAsState()
    val translationContentStates by viewModel.translationContentStates.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val audioError by audioBannerViewModel.error.collectAsState()
    val audioBannerState by audioBannerViewModel.audioBannerState.collectAsState()
    val playbackRate by audioBannerViewModel.playbackRate.collectAsState()
    val currentAyahProgress by audioBannerViewModel.currentAyahProgress.collectAsState()
    val currentPlaybackRange by audioBannerViewModel.playbackRange.collectAsState()
    val context = LocalContext.current
    val clipboardManager = remember(context) {
        context.getSystemService(ClipboardManager::class.java)
    }
    var noteEditorAyah by remember { mutableStateOf<AyahNumber?>(null) }
    var footnote by remember { mutableStateOf<TranslationFootnote?>(null) }

    // --- Multi-verse selection state ---
    var startAyah by remember { mutableStateOf<AyahNumber?>(null) }
    var selectedAyahs by remember { mutableStateOf<List<AyahNumber>>(emptyList()) }
    var selectionAnchor by remember { mutableStateOf<Offset?>(null) }
    val dismissAyahMenu = {
        startAyah = null
        selectedAyahs = emptyList()
        selectionAnchor = null
    }

    val pages = remember(state.totalPages) { (1..state.totalPages).toList() }
    // Translation rows only carry an in-sura verse number, so map it back through the
    // page's own verses instead of assuming the page's first sura.
    val pageVerses = remember(state.firstVerse, state.lastVerse) {
        val first = state.firstVerse
        val last = state.lastVerse
        if (first != null && last != null && last >= first) first.arrayTo(last) else emptyList()
    }
    val defaultPlaybackRange = state.firstVerse?.let { from ->
        from to JuzBasedLastAyahFinder().findLastAyah(from)
    }
    // A verse chosen from the ayah menu wins over the page-wide default, so the
    // banner keeps controlling whatever is actually queued.
    val activePlaybackRange = currentPlaybackRange ?: defaultPlaybackRange

    LaunchedEffect(currentAyahProgress) {
        viewModel.setReadingAyah(currentAyahProgress)
    }

    LaunchedEffect(audioError) {
        val error = audioError ?: return@LaunchedEffect
        viewModel.showMessage(error.localizedMessage ?: "Unable to play this verse.")
        audioBannerViewModel.dismissError()
    }

    // Compute note state for the current selection
    val noteState = if (selectedAyahs.isNotEmpty()) {
        viewModel.noteStateForVerses(selectedAyahs)
    } else {
        NoteState.NO_HIGHLIGHT
    }
    val highlightingColor = if (selectedAyahs.isNotEmpty()) {
        viewModel.highlightingColorForVerses(selectedAyahs)
    } else {
        com.quranengine.model.quranannotations.Note.Color.YELLOW
    }

    QuranViewScreen(
        state = state.copy(audioBannerState = audioBannerState.copy(playbackRate = playbackRate)),
        selectedAyahs = selectedAyahs,
        ayahMenuAnchor = selectionAnchor,
        modifier = modifier,
        transientMessage = userMessage,
        onTransientMessageShown = viewModel::clearUserMessage,
        noteState = noteState,
        highlightingColor = highlightingColor,
        ayahMenuActions = AyahMenuActions(
            onPlayFromHere = { ayahs ->
                // Reveal the dock so download/transport state is visible immediately.
                viewModel.setBarsVisible(true)
                audioBannerViewModel.play(ayahs.first(), ayahs.last())
                dismissAyahMenu()
            },
            onRepeatVerse = { ayahs ->
                viewModel.setBarsVisible(true)
                audioBannerViewModel.play(
                    from = ayahs.first(),
                    to = ayahs.last(),
                    verseRuns = Runs.INDEFINITE,
                )
                dismissAyahMenu()
            },
            onHighlight = { ayahs ->
                viewModel.highlightAyah(ayahs)
                dismissAyahMenu()
            },
            onSelectHighlightColor = { ayahs, color ->
                viewModel.highlightAyah(ayahs, color)
                dismissAyahMenu()
            },
            onAddNote = { ayahs ->
                noteEditorAyah = ayahs.first()
                dismissAyahMenu()
            },
            onDeleteNote = { ayahs ->
                viewModel.removeHighlight(ayahs)
                dismissAyahMenu()
            },
            onTranslationTafseer = { _ ->
                viewModel.toggleQuranMode()
                dismissAyahMenu()
            },
            onCopy = { ayahs ->
                viewModel.copyAyah(ayahs.first()) { text ->
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("Quran ayah", text))
                }
                dismissAyahMenu()
            },
            onShare = { ayahs ->
                viewModel.shareAyah(ayahs.first())
                dismissAyahMenu()
            },
            onDismiss = dismissAyahMenu,
        ),
        noteEditorAyah = noteEditorAyah,
        footnote = footnote,
        onDismissNoteEditor = { noteEditorAyah = null },
        onDismissFootnote = { footnote = null },
        onSaveNote = { ayah, note ->
            viewModel.saveNote(ayah, note)
            noteEditorAyah = null
        },
        onBack = onBack,
        onToggleBars = viewModel::toggleBars,
        onToggleMode = viewModel::toggleQuranMode,
        onToggleBookmark = viewModel::toggleCurrentPageBookmark,
        onAudioPlayPause = {
            activePlaybackRange?.let { (from, to) ->
                audioBannerViewModel.togglePlayPause(from, to)
            }
        },
        onAudioForward = audioBannerViewModel::stepForward,
        onAudioBackward = audioBannerViewModel::stepBackward,
        onAudioStop = audioBannerViewModel::stop,
        onSetPlaybackRate = audioBannerViewModel::setPlaybackRate,
        onAudioBannerTap = {
            activePlaybackRange?.let { (from, to) ->
                onNavigateToAdvancedAudio(from, to)
            }
        },
        onOpenPrayerSheet = onOpenPrayerSheet,
        onManageTranslations = onNavigateToTranslations,
        pageContent = {
            QuranPaginationView(
                pagingStrategy = if (state.twoPagesEnabled) {
                    PagingStrategy.DOUBLE_PAGE
                } else {
                    PagingStrategy.SINGLE_PAGE
                },
                pages = pages,
                selectedPages = state.visiblePages,
                onPagesChanged = viewModel::setVisiblePages,
                modifier = Modifier,
            ) { page ->
                if (state.quranMode == QuranMode.ARABIC) {
                    val contentState = pageContentStates[page] ?: com.quranengine.features.qurancontent.ContentState.Loading
                    ContentPageView(state = contentState) { content ->
                        ContentImageView(
                            state = content,
                            modifier = Modifier,
                            selectedAyahs = selectedAyahs,
                            onTap = viewModel::toggleBars,
                            onAyahSelectionStarted = { ayah, pressInRoot ->
                                startAyah = ayah
                                selectedAyahs = listOf(ayah)
                                selectionAnchor = pressInRoot
                            },
                            onAyahSelectionChanged = { ayah ->
                                val anchor = startAyah ?: return@ContentImageView
                                val start = if (ayah < anchor) ayah else anchor
                                val end = if (ayah < anchor) anchor else ayah
                                selectedAyahs = start.arrayTo(end)
                            },
                            onAyahSelectionEnded = {
                                // Menu stays open; user taps an action to dismiss
                            },
                        )
                    }
                } else {
                    val contentState = translationContentStates[page] ?: com.quranengine.features.qurancontent.ContentState.Loading
                    ContentPageView(state = contentState) { content ->
                        if (content.placeholderTitle != null) {
                            DataUnavailableView(
                                title = content.placeholderTitle,
                                message = content.placeholderMessage,
                            )
                        } else {
                            val highlightedVerses = selectedAyahs.map { it.ayah }.toSet()
                                .ifEmpty {
                                    currentAyahProgress?.ayah?.ayah?.let { setOf(it) } ?: emptySet()
                                }
                            ContentTranslationView(
                                items = content.items,
                                selectedVerses = highlightedVerses,
                                scrollToItemId = highlightedVerses.firstOrNull()?.let {
                                    TranslationItemId.ArabicText(it)
                                },
                                onTap = viewModel::toggleBars,
                                onAyahLongPressed = { verse, pressInRoot ->
                                    val ayah = pageVerses.firstOrNull { it.ayah == verse }
                                    if (ayah != null) {
                                        startAyah = ayah
                                        selectedAyahs = listOf(ayah)
                                        selectionAnchor = pressInRoot
                                    }
                                },
                                onFootnoteClick = { index, text ->
                                    footnote = TranslationFootnote(index = index, text = text)
                                },
                            )
                        }
                    }
                }
            }
        },
    )
}
