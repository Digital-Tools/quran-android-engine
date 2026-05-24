package com.quranengine.features.translations

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Translations") },
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
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (uiState.selectedItems.isNotEmpty()) {
                    item(key = "section-selected") {
                        Spacer(modifier = Modifier.height(8.dp))
                        NoorSection(
                            items = uiState.selectedItems,
                            title = "Selected",
                        ) { itemState ->
                            TranslationRow(
                                itemState = itemState,
                                isSelected = true,
                                showReorderHandle = true,
                                onClick = { onDeselect(itemState.translation) },
                                onDownloadClick = { onDownload(itemState.translation) },
                                onDeleteClick = { onDelete(itemState.translation) },
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (uiState.downloadedItems.isNotEmpty()) {
                    item(key = "section-downloaded") {
                        NoorSection(
                            items = uiState.downloadedItems,
                            title = "Downloaded",
                        ) { itemState ->
                            TranslationRow(
                                itemState = itemState,
                                isSelected = false,
                                showReorderHandle = false,
                                onClick = { onSelect(itemState.translation) },
                                onDownloadClick = { onDownload(itemState.translation) },
                                onDeleteClick = { onDelete(itemState.translation) },
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

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
                                isSelected = false,
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
}

@Composable
private fun TranslationRow(
    itemState: TranslationItemState,
    isSelected: Boolean,
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
            image = when {
                isSelected -> {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = QuranTheme.appIdentity,
                        )
                    }
                }
                showReorderHandle -> {
                    {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Reorder",
                            tint = QuranTheme.colors.secondaryText,
                        )
                    }
                }
                else -> null
            },
        )
        if (downloadState != DownloadState.DOWNLOADED) {
            DownloadButton(
                state = downloadState,
                progress = progress,
                onClick = when (downloadProgress) {
                    is TranslationItemState.DownloadProgress.NeedsUpgrade -> onDownloadClick
                    is TranslationItemState.DownloadProgress.NotDownloaded -> onDownloadClick
                    is TranslationItemState.DownloadProgress.Downloading -> { {} }
                    is TranslationItemState.DownloadProgress.Downloaded -> onDeleteClick
                },
                modifier = Modifier.padding(end = 16.dp),
            )
        } else {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = QuranTheme.colors.secondaryText,
                )
            }
        }
    }
}
