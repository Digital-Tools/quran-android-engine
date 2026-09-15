package com.quranengine.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import com.quranengine.domain.readingservice.ReadingResourcesService
import com.quranengine.domain.readingservice.displaySubtitle
import com.quranengine.domain.readingservice.displayTitle
import com.quranengine.model.qurankit.Reading
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.components.NoorSection
import com.quranengine.ui.theme.QuranTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafPickerScreen(
    uiState: MushafPickerUiState,
    onSelect: (Reading) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mushaf") },
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
            item(key = "section-mushafs") {
                Spacer(modifier = Modifier.height(8.dp))
                NoorSection(
                    items = uiState.items,
                    title = "Mushafs",
                ) { item ->
                    MushafRow(item = item, onSelect = onSelect)
                }
            }
        }
    }
}

@Composable
private fun MushafRow(
    item: MushafPickerItem,
    onSelect: (Reading) -> Unit,
) {
    val rowModifier = if (item.isActive) {
        Modifier
    } else {
        Modifier.clickable { onSelect(item.reading) }
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = rowModifier) {
        NoorListItem(
            title = item.reading.displayTitle,
            subtitle = item.reading.displaySubtitle,
            modifier = Modifier.weight(1f),
        )
        MushafTrailingState(item = item, onSelect = onSelect)
    }
}

@Composable
private fun MushafTrailingState(
    item: MushafPickerItem,
    onSelect: (Reading) -> Unit,
) {
    when (val status = item.status) {
        is ReadingResourcesService.ResourceStatus.Downloading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(24.dp).padding(end = 16.dp),
            ) {
                CircularProgressIndicator(
                    progress = { status.progress.toFloat() },
                    modifier = Modifier.size(24.dp),
                    color = QuranTheme.mizanGold,
                    strokeWidth = 2.dp,
                )
            }
        }
        is ReadingResourcesService.ResourceStatus.Error -> {
            IconButton(onClick = { onSelect(item.reading) }, modifier = Modifier.padding(end = 4.dp)) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry download",
                    tint = QuranTheme.colors.secondaryText,
                )
            }
        }
        ReadingResourcesService.ResourceStatus.Ready, null -> {
            if (item.isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = QuranTheme.mizanGold,
                    modifier = Modifier.padding(end = 16.dp),
                )
            }
        }
    }
}
