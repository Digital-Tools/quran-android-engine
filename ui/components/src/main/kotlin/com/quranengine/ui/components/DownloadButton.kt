package com.quranengine.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.ui.theme.QuranTheme

enum class DownloadState {
    NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED, PENDING
}

@Composable
fun DownloadButton(
    state: DownloadState,
    progress: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val identity = QuranTheme.mizanGold

    IconButton(onClick = onClick, modifier = modifier.size(32.dp)) {
        when (state) {
            DownloadState.NOT_DOWNLOADED -> {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Download",
                    tint = identity,
                )
            }
            DownloadState.DOWNLOADING -> {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(28.dp),
                        color = identity,
                        strokeWidth = 2.dp,
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = identity,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            DownloadState.DOWNLOADED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Downloaded",
                    tint = identity,
                )
            }
            DownloadState.PENDING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = identity,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}
