package com.quranengine.features.reciterlist

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.components.NoorSection
import com.quranengine.ui.theme.QuranTheme

@Composable
fun ReciterListScreen(
    viewModel: ReciterListViewModel,
    onReciterSelected: (Reciter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    ReciterListContent(
        state = state,
        localizedName = { viewModel.localizedName(it) },
        onReciterClick = { reciter ->
            viewModel.selectReciter(reciter)
            onReciterSelected(reciter)
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandaloneReciterListScreen(
    viewModel: ReciterListViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reciters") },
                actions = {
                    TextButton(onClick = onDone) {
                        Text("Done", color = QuranTheme.appIdentity)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = QuranTheme.colors.background,
                    titleContentColor = QuranTheme.colors.text,
                ),
            )
        },
        containerColor = QuranTheme.colors.background,
        modifier = modifier,
    ) { innerPadding ->
        ReciterListContent(
            state = state,
            localizedName = { viewModel.localizedName(it) },
            onReciterClick = {
                viewModel.selectReciter(it)
                onDone()
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun ReciterListContent(
    state: ReciterListState,
    localizedName: (Reciter) -> String,
    onReciterClick: (Reciter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (state.recent.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ReciterSection(
                    title = "Recent",
                    reciters = state.recent,
                    selectedReciter = state.selectedReciter,
                    localizedName = localizedName,
                    onReciterClick = onReciterClick,
                )
            }
        }

        if (state.downloaded.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ReciterSection(
                    title = "Downloaded",
                    reciters = state.downloaded,
                    selectedReciter = state.selectedReciter,
                    localizedName = localizedName,
                    onReciterClick = onReciterClick,
                )
            }
        }

        if (state.english.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ReciterSection(
                    title = "English",
                    reciters = state.english,
                    selectedReciter = state.selectedReciter,
                    localizedName = localizedName,
                    onReciterClick = onReciterClick,
                )
            }
        }

        if (state.arabic.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ReciterSection(
                    title = "Arabic",
                    reciters = state.arabic,
                    selectedReciter = state.selectedReciter,
                    localizedName = localizedName,
                    onReciterClick = onReciterClick,
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ReciterSection(
    title: String,
    reciters: List<Reciter>,
    selectedReciter: Reciter?,
    localizedName: (Reciter) -> String,
    onReciterClick: (Reciter) -> Unit,
) {
    NoorSection(
        items = reciters,
        title = title,
    ) { reciter ->
        val isSelected = reciter.id == selectedReciter?.id
        NoorListItem(
            title = localizedName(reciter),
            image = if (isSelected) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = QuranTheme.appIdentity,
                    )
                }
            } else {
                null
            },
            onClick = { onReciterClick(reciter) },
        )
    }
}
