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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            tonalElevation = 12.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Info Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.title,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        if (state.subtitle.isNotEmpty()) {
                            Text(
                                text = state.subtitle,
                                color = androidx.compose.ui.graphics.Color.Gray,
                                fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                            )
                        }
                    }
                    IconButton(onClick = onBannerTap) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }

                // Controls Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Stop Button
                    IconButton(onClick = onStop) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(androidx.compose.ui.graphics.Color(0xFF00D4FF), shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = androidx.compose.ui.graphics.Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Speed Button
                    PlaybackSpeedMenu(
                        playbackRate = state.playbackRate,
                        onSetPlaybackRate = onSetPlaybackRate,
                    )

                    // Previous
                    IconButton(onClick = onBackward, enabled = state.canGoBackward) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = if (state.canGoBackward) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Play/Pause (Large)
                    IconButton(onClick = onPlayPause) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = androidx.compose.ui.graphics.Color(0xFF00D4FF),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Next
                    IconButton(onClick = onForward, enabled = state.canGoForward) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = if (state.canGoForward) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Gray,
                            modifier = Modifier.size(32.dp)
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
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier
                .height(40.dp)
                .align(Alignment.Center)
                .background(androidx.compose.ui.graphics.Color(0xFF2C2C2E), RoundedCornerShape(20.dp)),
            colors = ButtonDefaults.textButtonColors(
                contentColor = androidx.compose.ui.graphics.Color.White,
            ),
        ) {
            Text(
                text = formatPlaybackSpeed(playbackRate),
                fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
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
