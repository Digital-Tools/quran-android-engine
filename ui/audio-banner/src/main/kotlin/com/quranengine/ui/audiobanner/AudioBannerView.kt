package com.quranengine.ui.audiobanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme
import com.quranengine.ui.theme.audioBannerBackground

private val SpeedChipShape = RoundedCornerShape(percent = 50)
private val SupportedPlaybackRates = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

private fun formatPlaybackRate(rate: Float): String {
    val text = if (rate == rate.toLong().toFloat()) {
        rate.toLong().toString()
    } else {
        rate.toString().trimEnd('0').trimEnd('.')
    }
    return "${text}x"
}

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
    val showTransport = state.isPlaying || state.isPaused

    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 10.dp, shape = dockShape, clip = false)
                .clip(dockShape)
                .background(dockColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 8.dp, end = 8.dp, top = 5.dp, bottom = 12.dp),
            ) {
                when {
                    state.isDownloading -> DownloadingContent(
                        progress = state.progress,
                        onCancel = onStop,
                    )
                    showTransport -> PlayingContent(
                        isPlaying = state.isPlaying,
                        playbackRate = state.playbackRate,
                        onStop = onStop,
                        onPlayPause = onPlayPause,
                        onBackward = onBackward,
                        onForward = onForward,
                        onMore = onBannerTap,
                        onSetPlaybackRate = onSetPlaybackRate,
                    )
                    else -> ReadyToPlayContent(
                        title = state.title,
                        subtitle = state.subtitle,
                        onPlay = onPlayPause,
                        onTitleTap = onBannerTap,
                        onMore = onBannerTap,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayingContent(
    isPlaying: Boolean,
    playbackRate: Float,
    onStop: () -> Unit,
    onPlayPause: () -> Unit,
    onBackward: () -> Unit,
    onForward: () -> Unit,
    onMore: () -> Unit,
    onSetPlaybackRate: (Float) -> Unit,
) {
    val gold = QuranTheme.mizanGold

    // Equal-width side wings keep rewind/pause/forward on the true dock center.
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TransportIconButton(onClick = onStop) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = gold,
                    modifier = Modifier.size(26.dp),
                )
            }
            Box(modifier = Modifier.weight(1f))
            SpeedChip(
                rate = playbackRate,
                onSetPlaybackRate = onSetPlaybackRate,
            )
            Box(modifier = Modifier.weight(1f))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TransportIconButton(onClick = onBackward) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Previous",
                    tint = gold,
                    modifier = Modifier.size(28.dp),
                )
            }
            TransportIconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = gold,
                    modifier = Modifier.size(30.dp),
                )
            }
            TransportIconButton(onClick = onForward) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Next",
                    tint = gold,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f))
            TransportIconButton(onClick = onMore) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "More options",
                    tint = gold,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun ReadyToPlayContent(
    title: String,
    subtitle: String,
    onPlay: () -> Unit,
    onTitleTap: () -> Unit,
    onMore: () -> Unit,
) {
    val gold = QuranTheme.mizanGold

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransportIconButton(onClick = onPlay) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = gold,
                modifier = Modifier.size(30.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clickable(onClick = onTitleTap),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = QuranTheme.colors.text,
                maxLines = 1,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = QuranTheme.colors.secondaryText,
                    maxLines = 1,
                )
            }
        }

        TransportIconButton(onClick = onMore) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "More options",
                tint = gold,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun DownloadingContent(
    progress: Float,
    onCancel: () -> Unit,
) {
    val gold = QuranTheme.mizanGold

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransportIconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel download",
                tint = gold,
                modifier = Modifier.size(26.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = gold,
                trackColor = gold.copy(alpha = 0.2f),
            )
            Text(
                text = "Downloading…",
                style = MaterialTheme.typography.bodyMedium,
                color = QuranTheme.colors.text,
            )
        }
    }
}

@Composable
private fun SpeedChip(
    rate: Float,
    onSetPlaybackRate: (Float) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val gold = QuranTheme.mizanGold

    Box {
        Text(
            text = formatPlaybackRate(rate),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = gold,
            maxLines = 1,
            modifier = Modifier
                .clip(SpeedChipShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SupportedPlaybackRates.forEach { value ->
                DropdownMenuItem(
                    text = { Text(formatPlaybackRate(value)) },
                    onClick = {
                        expanded = false
                        onSetPlaybackRate(value)
                    },
                )
            }
        }
    }
}

@Composable
private fun TransportIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
    ) {
        content()
    }
}
