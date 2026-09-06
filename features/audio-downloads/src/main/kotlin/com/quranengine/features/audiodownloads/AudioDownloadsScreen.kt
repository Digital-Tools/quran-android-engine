package com.quranengine.features.audiodownloads

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.ui.components.DownloadButton
import com.quranengine.ui.components.DownloadState
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.components.NoorSection
import com.quranengine.ui.theme.QuranTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDownloadsScreen(
    uiState: AudioDownloadsUiState,
    onDownload: (AudioDownloadItem) -> Unit,
    onCancel: (AudioDownloadItem) -> Unit,
    onDelete: (AudioDownloadItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = QuranTheme.colors.background,
                    titleContentColor = QuranTheme.colors.text,
                    navigationIconContentColor = QuranTheme.colors.text,
                ),
            )
        },
        containerColor = QuranTheme.colors.background,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.downloaded.isNotEmpty()) {
                item(key = "section-downloaded") {
                    Spacer(modifier = Modifier.height(8.dp))
                    NoorSection(
                        items = uiState.downloaded,
                        title = "Downloaded",
                    ) { item ->
                        AudioDownloadRow(
                            item = item,
                            onDownload = onDownload,
                            onCancel = onCancel,
                            onDelete = onDelete,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (uiState.available.isNotEmpty()) {
                item(key = "section-available") {
                    NoorSection(
                        items = uiState.available,
                        title = "All Reciters",
                    ) { item ->
                        AudioDownloadRow(
                            item = item,
                            onDownload = onDownload,
                            onCancel = onCancel,
                            onDelete = onDelete,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AudioDownloadRow(
    item: AudioDownloadItem,
    onDownload: (AudioDownloadItem) -> Unit,
    onCancel: (AudioDownloadItem) -> Unit,
    onDelete: (AudioDownloadItem) -> Unit,
) {
    val downloadState = when {
        item.isDownloading && (item.progress ?: 0.0) < 0.001 -> DownloadState.PENDING
        item.isDownloading -> DownloadState.DOWNLOADING
        item.isDownloaded -> DownloadState.DOWNLOADED
        else -> DownloadState.NOT_DOWNLOADED
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        NoorListItem(
            title = item.displayName,
            subtitle = item.sizeLabel().ifBlank { null },
            modifier = Modifier.weight(1f),
        )
        if (item.canDelete && !item.isDownloading) {
            IconButton(onClick = { onDelete(item) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete audio",
                    tint = QuranTheme.colors.secondaryText,
                )
            }
        } else {
            DownloadButton(
                state = downloadState,
                progress = (item.progress ?: 0.0).toFloat(),
                onClick = {
                    when {
                        item.isDownloading -> onCancel(item)
                        !item.isDownloaded -> onDownload(item)
                    }
                },
                modifier = Modifier.padding(end = 16.dp),
            )
        }
    }
}
