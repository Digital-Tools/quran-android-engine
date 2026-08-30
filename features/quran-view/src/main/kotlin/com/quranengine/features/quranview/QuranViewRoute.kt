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
import androidx.compose.ui.platform.LocalContext
import com.quranengine.features.audiobanner.AudioBannerViewModel
import com.quranengine.features.qurancontent.ContentPageView
import com.quranengine.features.quranimage.ContentImageView
import com.quranengine.features.quranpages.PagingStrategy
import com.quranengine.features.quranpages.QuranPaginationView
import com.quranengine.features.qurantranslation.ContentTranslationView
import com.quranengine.model.qurankit.AyahNumber
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
    val audioBannerState by audioBannerViewModel.audioBannerState.collectAsState()
    val playbackRate by audioBannerViewModel.playbackRate.collectAsState()
    val currentAyahProgress by audioBannerViewModel.currentAyahProgress.collectAsState()
    val currentPlaybackRange by audioBannerViewModel.playbackRange.collectAsState()
    val context = LocalContext.current
    val clipboardManager = remember(context) {
        context.getSystemService(ClipboardManager::class.java)
    }
    var noteEditorAyah by remember { mutableStateOf<AyahNumber?>(null) }
    var selectedAyahForMenu by remember { mutableStateOf<AyahNumber?>(null) }
    val pages = remember(state.totalPages) { (1..state.totalPages).toList() }
    val defaultPlaybackRange = state.firstVerse?.let { from ->
        from to JuzBasedLastAyahFinder().findLastAyah(from)
    }
    val advancedAudioRange = currentPlaybackRange ?: defaultPlaybackRange

    LaunchedEffect(currentAyahProgress) {
        viewModel.setReadingAyah(currentAyahProgress)
    }

    QuranViewScreen(
        state = state.copy(audioBannerState = audioBannerState.copy(playbackRate = playbackRate)),
        selectedAyah = selectedAyahForMenu,
        modifier = modifier,
        transientMessage = userMessage,
        onTransientMessageShown = viewModel::clearUserMessage,
        ayahMenuActions = AyahMenuActions(
            onPlayFromHere = { ayah ->
                audioBannerViewModel.play(ayah, JuzBasedLastAyahFinder().findLastAyah(ayah))
                selectedAyahForMenu = null
            },
            onRepeatVerse = { ayah ->
                onNavigateToAdvancedAudio(ayah, ayah)
                selectedAyahForMenu = null
            },
            onHighlight = { ayah ->
                viewModel.addBookmarkForAyah(ayah)
                selectedAyahForMenu = null
            },
            onSelectHighlightColor = { ayah ->
                viewModel.addBookmarkForAyah(ayah)
                selectedAyahForMenu = null
            },
            onAddNote = { ayah ->
                noteEditorAyah = ayah
                selectedAyahForMenu = null
            },
            onTranslationTafseer = {
                viewModel.toggleQuranMode()
                selectedAyahForMenu = null
            },
            onCopy = { ayah ->
                viewModel.copyAyah(ayah) { text ->
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("Quran ayah", text))
                }
                selectedAyahForMenu = null
            },
            onShare = { ayah ->
                viewModel.shareAyah(ayah)
                selectedAyahForMenu = null
            },
            onBookmarkPage = { ayah ->
                viewModel.addBookmarkForAyah(ayah)
                selectedAyahForMenu = null
            },
            onToggleTranslations = {
                viewModel.toggleQuranMode()
                selectedAyahForMenu = null
            },
            onManageTranslations = onNavigateToTranslations,
            onDismiss = {
                selectedAyahForMenu = null
            },
        ),
        noteEditorAyah = noteEditorAyah,
        onDismissNoteEditor = { noteEditorAyah = null },
        onSaveNote = { ayah, note ->
            viewModel.saveNote(ayah, note)
            noteEditorAyah = null
        },
        onBack = onBack,
        onToggleBars = viewModel::toggleBars,
        onToggleMode = viewModel::toggleQuranMode,
        onToggleBookmark = viewModel::toggleCurrentPageBookmark,
        onAudioPlayPause = {
            defaultPlaybackRange?.let { (from, to) ->
                audioBannerViewModel.togglePlayPause(from, to)
            }
        },
        onAudioForward = audioBannerViewModel::stepForward,
        onAudioBackward = audioBannerViewModel::stepBackward,
        onAudioStop = audioBannerViewModel::stop,
        onSetPlaybackRate = audioBannerViewModel::setPlaybackRate,
        onAudioBannerTap = {
            advancedAudioRange?.let { (from, to) ->
                onNavigateToAdvancedAudio(from, to)
            }
        },
        onOpenPrayerSheet = onOpenPrayerSheet,
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
                            onAyahTapped = {
                                selectedAyahForMenu = state.firstVerse
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
                            ContentTranslationView(
                                items = content.items,
                                onAyahTapped = { verse ->
                                    val first = state.firstVerse
                                    if (first != null) {
                                        selectedAyahForMenu = AyahNumber(first.sura, verse) ?: first
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
    )
}
