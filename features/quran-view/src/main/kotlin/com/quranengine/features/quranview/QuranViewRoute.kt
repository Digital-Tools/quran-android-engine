package com.quranengine.features.quranview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.quranengine.features.audiobanner.AudioBannerViewModel
import com.quranengine.features.qurancontent.ContentPageView
import com.quranengine.features.quranimage.ContentImageView
import com.quranengine.features.quranpages.PagingStrategy
import com.quranengine.features.quranpages.QuranPaginationView
import com.quranengine.features.qurantranslation.ContentTranslationView
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurantext.QuranMode
import com.quranengine.ui.components.DataUnavailableView

@Composable
fun QuranViewRoute(
    viewModel: QuranViewViewModel,
    audioBannerViewModel: AudioBannerViewModel,
    onBack: () -> Unit,
    onNavigateToAdvancedAudio: (AyahNumber, AyahNumber) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val pageContentStates by viewModel.pageContentStates.collectAsState()
    val translationContentStates by viewModel.translationContentStates.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val audioBannerState by audioBannerViewModel.audioBannerState.collectAsState()
    val currentAyah by audioBannerViewModel.currentAyah.collectAsState()
    val currentPlaybackRange by audioBannerViewModel.playbackRange.collectAsState()
    val pages = remember(state.totalPages) { (1..state.totalPages).toList() }
    val pagePlaybackRange = state.firstVerse?.let { from ->
        state.lastVerse?.let { to -> from to to }
    }
    val advancedAudioRange = currentPlaybackRange ?: pagePlaybackRange

    LaunchedEffect(currentAyah) {
        viewModel.setReadingAyah(currentAyah)
    }

    QuranViewScreen(
        state = state.copy(audioBannerState = audioBannerState),
        modifier = modifier,
        transientMessage = userMessage,
        onTransientMessageShown = viewModel::clearUserMessage,
        ayahMenuActions = AyahMenuActions(
            onBookmarkPage = viewModel::addBookmarkForAyah,
        ),
        onBack = onBack,
        onToggleBars = viewModel::toggleBars,
        onToggleMode = viewModel::toggleQuranMode,
        onToggleBookmark = viewModel::toggleCurrentPageBookmark,
        onAudioPlayPause = {
            pagePlaybackRange?.let { (from, to) ->
                audioBannerViewModel.togglePlayPause(from, to)
            }
        },
        onAudioForward = audioBannerViewModel::stepForward,
        onAudioBackward = audioBannerViewModel::stepBackward,
        onAudioStop = audioBannerViewModel::stop,
        onAudioBannerTap = {
            advancedAudioRange?.let { (from, to) ->
                onNavigateToAdvancedAudio(from, to)
            }
        },
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
                            ContentTranslationView(items = content.items)
                        }
                    }
                }
            }
        },
    )
}
