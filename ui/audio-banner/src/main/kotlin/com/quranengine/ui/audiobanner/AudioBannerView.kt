package com.quranengine.ui.audiobanner

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme
import com.quranengine.ui.theme.audioBannerBackground

@Composable
fun AudioBannerView(
    state: AudioBannerState,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit = {},
    onForward: () -> Unit = {},
    onBackward: () -> Unit = {},
    onStop: () -> Unit = {},
    onBannerTap: () -> Unit = {},
    onSetPlaybackRate: (Float) -> Unit = {},
) {
    val dockShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val dockColor = QuranTheme.colors.audioBannerBackground(QuranTheme.isDark)

    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        // Docked full-width bar: solid fill through the nav/gesture inset;
        // controls stay above that inset (same pattern as iOS audio banner).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 10.dp, shape = dockShape, clip = false)
                .clip(dockShape)
                .background(dockColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .clickable(onClick = onBannerTap)
                    // Raised controls: less top pad, more bottom pad (matches iOS).
                    .padding(start = 8.dp, end = 8.dp, top = 5.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(52.dp)
                ) {
                    if (state.isDownloading) {
                        CircularProgressIndicator(
                            progress = { state.progress.coerceIn(0f, 1f) },
                            modifier = Modifier.size(28.dp),
                            color = QuranTheme.appIdentity,
                            strokeWidth = 2.5.dp,
                        )
                    } else {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = QuranTheme.colors.text,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = QuranTheme.colors.text,
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

                IconButton(
                    onClick = onBannerTap,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More options",
                        tint = QuranTheme.colors.text,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
