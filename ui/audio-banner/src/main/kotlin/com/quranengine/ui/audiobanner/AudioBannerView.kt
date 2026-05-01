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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

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
    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(QuranTheme.colors.secondaryBackground)
                .clickable(onClick = onBannerTap),
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = QuranTheme.appIdentity,
                trackColor = QuranTheme.colors.secondaryBackground,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = QuranTheme.colors.text,
                        maxLines = 1,
                    )
                    if (state.subtitle.isNotEmpty()) {
                        Text(
                            text = state.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = QuranTheme.colors.secondaryText,
                            maxLines = 1,
                        )
                    }
                }

                // Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    PlaybackSpeedMenu(
                        playbackRate = state.playbackRate,
                        onSetPlaybackRate = onSetPlaybackRate,
                    )

                    IconButton(
                        onClick = onBackward,
                        enabled = state.canGoBackward,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = if (state.canGoBackward) QuranTheme.colors.text else QuranTheme.colors.secondaryText,
                        )
                    }

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = QuranTheme.appIdentity,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    IconButton(
                        onClick = onForward,
                        enabled = state.canGoForward,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = if (state.canGoForward) QuranTheme.colors.text else QuranTheme.colors.secondaryText,
                        )
                    }

                    IconButton(
                        onClick = onStop,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop",
                            tint = QuranTheme.colors.secondaryText,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

private val playbackSpeedValues = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)

@Composable
private fun PlaybackSpeedMenu(
    playbackRate: Float,
    onSetPlaybackRate: (Float) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = QuranTheme.colors.text,
            ),
        ) {
            Text(
                text = formatPlaybackSpeed(playbackRate),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            playbackSpeedValues.forEach { rate ->
                DropdownMenuItem(
                    text = { Text(formatPlaybackSpeed(rate)) },
                    onClick = {
                        expanded = false
                        onSetPlaybackRate(rate)
                    },
                )
            }
        }
    }
}

private fun formatPlaybackSpeed(rate: Float): String {
    val rounded = kotlin.math.round(rate * 100f) / 100f
    return if (rounded % 1f == 0f) {
        "${rounded.toInt()}x"
    } else {
        "${rounded}x"
    }
}
