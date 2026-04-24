package com.quranengine.features.translations

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.model.qurantext.Translation
import com.quranengine.ui.components.DownloadButton
import com.quranengine.ui.components.DownloadState
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.components.NoorSection
import com.quranengine.ui.theme.QuranTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationsListScreen(
    uiState: TranslationsListUiState,
    onRefresh: () -> Unit,
    onDownload: (Translation) -> Unit,
    onDelete: (Translation) -> Unit,
    onSelect: (Translation) -> Unit,
    onDeselect: (Translation) -> Unit,
    onMoveSelected: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Selected translations section
            if (uiState.selectedItems.isNotEmpty()) {
                item(key = "section-selected") {
                    NoorSection(
                        items = uiState.selectedItems,
                        title = "Selected",
                    ) { itemState ->
                        TranslationRow(
                            itemState = itemState,
                            showReorderHandle = true,
                            onClick = { onDeselect(itemState.translation) },
                            onDownloadClick = { onDownload(itemState.translation) },
                            onDeleteClick = { onDelete(itemState.translation) },
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Downloaded but not selected section
            if (uiState.downloadedItems.isNotEmpty()) {
                item(key = "section-downloaded") {
                    NoorSection(
                        items = uiState.downloadedItems,
                        title = "Downloaded",
                    ) { itemState ->
                        TranslationRow(
                            itemState = itemState,
                            showReorderHandle = false,
                            onClick = { onSelect(itemState.translation) },
                            onDownloadClick = { onDownload(itemState.translation) },
                            onDeleteClick = { onDelete(itemState.translation) },
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Available translations grouped by language
            val sortedLanguages = uiState.availableByLanguage.keys.sorted()
            for (language in sortedLanguages) {
                val languageItems = uiState.availableByLanguage[language] ?: continue
                item(key = "section-available-$language") {
                    NoorSection(
                        items = languageItems,
                        title = language,
                    ) { itemState ->
                        TranslationRow(
                            itemState = itemState,
                            showReorderHandle = false,
                            onClick = { onDownload(itemState.translation) },
                            onDownloadClick = { onDownload(itemState.translation) },
                            onDeleteClick = { onDelete(itemState.translation) },
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TranslationRow(
    itemState: TranslationItemState,
    showReorderHandle: Boolean,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val translation = itemState.translation
    val downloadProgress = itemState.downloadProgress

    val (downloadState, progress) = when (downloadProgress) {
        is TranslationItemState.DownloadProgress.NotDownloaded ->
            DownloadState.NOT_DOWNLOADED to 0f
        is TranslationItemState.DownloadProgress.Downloading ->
            DownloadState.DOWNLOADING to downloadProgress.progress
        is TranslationItemState.DownloadProgress.Downloaded ->
            DownloadState.DOWNLOADED to 1f
        is TranslationItemState.DownloadProgress.NeedsUpgrade ->
            DownloadState.NOT_DOWNLOADED to 0f
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        NoorListItem(
            title = translation.displayName,
            subtitle = translation.translatorDisplayName,
            rightSubtitle = translation.languageCode,
            onClick = onClick,
            modifier = Modifier.weight(1f),
            image = if (showReorderHandle) {
                {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Reorder",
                        tint = QuranTheme.colors.secondaryText,
                    )
                }
            } else {
                null
            },
        )
        DownloadButton(
            state = downloadState,
            progress = progress,
            onClick = when (downloadProgress) {
                is TranslationItemState.DownloadProgress.Downloaded -> onDeleteClick
                is TranslationItemState.DownloadProgress.NeedsUpgrade -> onDownloadClick
                is TranslationItemState.DownloadProgress.NotDownloaded -> onDownloadClick
                is TranslationItemState.DownloadProgress.Downloading -> { {} }
            },
            modifier = Modifier.padding(end = 16.dp),
        )
    }
}

