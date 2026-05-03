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
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(QuranTheme.colors.secondaryBackground)
                    .clickable(onClick = onBannerTap)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.Center,
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
                                style = MaterialTheme.typography.bodySmall,
                                color = QuranTheme.colors.secondaryText,
                                maxLines = 1,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = QuranTheme.appIdentity,
                            trackColor = QuranTheme.colors.text.copy(alpha = 0.1f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        )
                    }

                    // Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
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

                        // Play/Pause with a subtle background circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(50))
                                .background(QuranTheme.appIdentity.copy(alpha = 0.1f))
                                .clickable(onClick = onPlayPause),
                            contentAlignment = Alignment.Center
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
            modifier = Modifier
                .height(40.dp)
                .align(Alignment.Center),
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
