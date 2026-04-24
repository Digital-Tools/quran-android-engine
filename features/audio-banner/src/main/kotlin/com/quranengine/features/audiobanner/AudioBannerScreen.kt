package com.quranengine.features.audiobanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.quranengine.ui.audiobanner.AudioBannerView

@Composable
fun AudioBannerScreen(
    viewModel: AudioBannerViewModel,
    onNavigateToReciterList: () -> Unit,
    onNavigateToAdvancedAudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bannerState by viewModel.audioBannerState.collectAsState()

    AudioBannerView(
        state = bannerState,
        modifier = modifier,
        onPlayPause = {
            if (bannerState.isPlaying) {
                viewModel.pause()
            } else {
                viewModel.resume()
            }
        },
        onForward = viewModel::stepForward,
        onBackward = viewModel::stepBackward,
        onStop = viewModel::stop,
        onBannerTap = onNavigateToAdvancedAudio,
    )
}
